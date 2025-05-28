package com.sportcenter.shift_manager.service;

import com.sportcenter.shift_manager.config.JwtUtil;
import com.sportcenter.shift_manager.dto.ColaboradorDTO;
import com.sportcenter.shift_manager.exception.ResourceNotFoundException;
import com.sportcenter.shift_manager.model.Colaborador;
import com.sportcenter.shift_manager.model.Empresa;
import com.sportcenter.shift_manager.model.Puesto;
import com.sportcenter.shift_manager.model.Usuario;
import com.sportcenter.shift_manager.repository.ColaboradorRepository;
import com.sportcenter.shift_manager.repository.EmpresaRepository;
import com.sportcenter.shift_manager.repository.PuestoRepository;
import com.sportcenter.shift_manager.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ColaboradorService {
    private final ColaboradorRepository colaboradorRepository;
    private final EmpresaRepository empresaRepository;
    private final CloudinaryService cloudinaryService;
    private final PuestoRepository puestoRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    public ColaboradorService(ColaboradorRepository colaboradorRepository, EmpresaRepository empresaRepository,
                              CloudinaryService cloudinaryService, PuestoRepository puestoRepository,
                              UsuarioRepository usuarioRepository, JwtUtil jwtUtil) {
        this.colaboradorRepository = colaboradorRepository;
        this.empresaRepository = empresaRepository;
        this.cloudinaryService = cloudinaryService;
        this.puestoRepository = puestoRepository;
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
    }

    private String getUsernameFromToken(String token) {
        return jwtUtil.extractUsername(token.replace("Bearer ", ""));
    }

    private Usuario getUsuarioFromToken(String token) {
        String username = getUsernameFromToken(token);
        Usuario usuario = usuarioRepository.findByUsername(username);
        if (usuario == null) {
            throw new ResourceNotFoundException("Usuario no encontrado: " + username);
        }
        return usuario;
    }

    @Transactional
    public Colaborador saveColaborador(ColaboradorDTO colaboradorDTO, MultipartFile file, String token) throws IOException {
        Usuario usuario = getUsuarioFromToken(token);

        // Validar existencia de empresa
        Empresa empresa = empresaRepository.findById(colaboradorDTO.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con ID " + colaboradorDTO.getEmpresaId() + " no encontrada"));
        if (!empresa.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para asignar esta empresa");
        }

        // Validar duplicados de email, DNI y Nombre + Apellido
        if (colaboradorDTO.getEmail() != null && colaboradorRepository.findByUsuarioAndEmail(usuario, colaboradorDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un colaborador con el email: " + colaboradorDTO.getEmail());
        }
        if (colaboradorRepository.findByUsuarioAndDni(usuario, colaboradorDTO.getDni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un colaborador con el DNI: " + colaboradorDTO.getDni());
        }
        if (colaboradorRepository.findByUsuarioAndNombreAndApellido(usuario, colaboradorDTO.getNombre(), colaboradorDTO.getApellido()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un colaborador con el nombre y apellido: "
                    + colaboradorDTO.getNombre() + " " + colaboradorDTO.getApellido());
        }

        // Crear nuevo colaborador
        Colaborador colaborador = new Colaborador();
        colaborador.setNombre(colaboradorDTO.getNombre());
        colaborador.setApellido(colaboradorDTO.getApellido());
        colaborador.setDni(colaboradorDTO.getDni());
        colaborador.setTelefono(colaboradorDTO.getTelefono());
        colaborador.setEmail(colaboradorDTO.getEmail());
        colaborador.setHabilitado(colaboradorDTO.isHabilitado());
        colaborador.setEmpresa(empresa);
        colaborador.setFechaNacimiento(colaboradorDTO.getFechaNacimiento());
        colaborador.setUsuario(usuario);

        // Asignar puesto si se proporciona puestoId
        if (colaboradorDTO.getPuestoId() != null) {
            Puesto puesto = puestoRepository.findById(colaboradorDTO.getPuestoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Puesto con ID " + colaboradorDTO.getPuestoId() + " no encontrado"));
            if (!puesto.getUsuario().getId().equals(usuario.getId())) {
                throw new ResourceNotFoundException("No tienes permiso para asignar este puesto");
            }
            colaborador.setPuesto(puesto);
        }

        // Subir imagen a Cloudinary
        if (file != null && !file.isEmpty()) {
            validarImagen(file);
            String imageUrl = cloudinaryService.uploadImage(file);
            colaborador.setFotoUrl(imageUrl);
        }

        return colaboradorRepository.save(colaborador);
    }

    private void validarImagen(MultipartFile file) {
        if (file.getSize() > 1048576) { // 1 MB
            throw new RuntimeException("La foto debe ser menor a 1 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen.");
        }
    }

    public List<ColaboradorDTO> getAllColaboradores(String token) {
        Usuario usuario = getUsuarioFromToken(token);
        return colaboradorRepository.findByUsuario(usuario)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ColaboradorDTO> getColaboradoresByEmpresa(Long empresaId, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con ID " + empresaId + " no encontrada"));
        if (!empresa.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para acceder a esta empresa");
        }
        return colaboradorRepository.findByUsuarioAndEmpresaId(usuario, empresaId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public Colaborador getColaboradorById(Long id, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Colaborador colaborador = colaboradorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador con ID " + id + " no encontrado"));
        if (!colaborador.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para acceder a este colaborador");
        }
        return colaborador;
    }

    @Transactional
    public Colaborador updateColaborador(Long id, ColaboradorDTO colaboradorDTO, MultipartFile file, String token) throws IOException {
        Usuario usuario = getUsuarioFromToken(token);
        Colaborador colaborador = colaboradorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador con ID " + id + " no encontrado"));
        if (!colaborador.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para modificar este colaborador");
        }

        Empresa nuevaEmpresa = empresaRepository.findById(colaboradorDTO.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con ID " + colaboradorDTO.getEmpresaId() + " no encontrada"));
        if (!nuevaEmpresa.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para asignar esta empresa");
        }

        colaboradorRepository.findByUsuarioAndEmail(usuario, colaboradorDTO.getEmail())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Ya existe un colaborador con el email: " + colaboradorDTO.getEmail());
                });

        colaboradorRepository.findByUsuarioAndDni(usuario, colaboradorDTO.getDni())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Ya existe un colaborador con el DNI: " + colaboradorDTO.getDni());
                });

        colaboradorRepository.findByUsuarioAndNombreAndApellido(usuario, colaboradorDTO.getNombre(), colaboradorDTO.getApellido())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Ya existe un colaborador con el nombre y apellido: "
                            + colaboradorDTO.getNombre() + " " + colaboradorDTO.getApellido());
                });

        colaborador.setNombre(colaboradorDTO.getNombre());
        colaborador.setApellido(colaboradorDTO.getApellido());
        colaborador.setDni(colaboradorDTO.getDni());
        colaborador.setTelefono(colaboradorDTO.getTelefono());
        colaborador.setEmail(colaboradorDTO.getEmail());
        colaborador.setEmpresa(nuevaEmpresa);
        colaborador.setHabilitado(colaboradorDTO.isHabilitado());
        colaborador.setFechaNacimiento(colaboradorDTO.getFechaNacimiento());

        if (colaboradorDTO.getPuestoId() != null) {
            Puesto puesto = puestoRepository.findById(colaboradorDTO.getPuestoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Puesto con ID " + colaboradorDTO.getPuestoId() + " no encontrado"));
            if (!puesto.getUsuario().getId().equals(usuario.getId())) {
                throw new ResourceNotFoundException("No tienes permiso para asignar este puesto");
            }
            colaborador.setPuesto(puesto);
        } else {
            colaborador.setPuesto(null);
        }

        if (file != null && !file.isEmpty()) {
            validarImagen(file);
            if (colaborador.getFotoUrl() != null) {
                String publicId = getPublicIdFromUrl(colaborador.getFotoUrl());
                cloudinaryService.deleteImage(publicId);
            }
            String imageUrl = cloudinaryService.uploadImage(file);
            colaborador.setFotoUrl(imageUrl);
        }

        return colaboradorRepository.save(colaborador);
    }

    private String getPublicIdFromUrl(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
    }

    @Transactional
    public void deleteColaborador(Long id, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Colaborador colaborador = colaboradorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador con ID " + id + " no encontrado"));
        if (!colaborador.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para eliminar este colaborador");
        }
        colaboradorRepository.delete(colaborador);
    }

    @Transactional
    public Colaborador toggleHabilitacionColaborador(Long id, boolean habilitado, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Colaborador colaborador = colaboradorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador con ID " + id + " no encontrado"));
        if (!colaborador.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para modificar este colaborador");
        }
        colaborador.setHabilitado(habilitado);
        return colaboradorRepository.save(colaborador);
    }

    public List<ColaboradorDTO> getColaboradoresPorHabilitacion(boolean habilitado, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        return colaboradorRepository.findByUsuarioAndHabilitado(usuario, habilitado)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public ColaboradorDTO convertToDTO(Colaborador colaborador) {
        return new ColaboradorDTO(
                colaborador.getId(),
                colaborador.getNombre(),
                colaborador.getApellido(),
                colaborador.getDni(),
                colaborador.getTelefono(),
                colaborador.getEmail(),
                colaborador.getEmpresa() != null ? colaborador.getEmpresa().getId() : null,
                colaborador.getEmpresa() != null ? colaborador.getEmpresa().getNombre() : "N/A",
                colaborador.getFotoUrl(),
                colaborador.isHabilitado(),
                colaborador.getFechaNacimiento(),
                colaborador.getPuesto() != null ? colaborador.getPuesto().getId() : null,
                colaborador.getPuesto() != null ? colaborador.getPuesto().getNombre() : null
        );
    }
}