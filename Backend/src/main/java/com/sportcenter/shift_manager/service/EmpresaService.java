package com.sportcenter.shift_manager.service;

import com.sportcenter.shift_manager.config.JwtUtil;
import com.sportcenter.shift_manager.dto.EmpresaDTO;
import com.sportcenter.shift_manager.exception.ResourceNotFoundException;
import com.sportcenter.shift_manager.model.Colaborador;
import com.sportcenter.shift_manager.model.Empresa;
import com.sportcenter.shift_manager.model.Usuario;
import com.sportcenter.shift_manager.repository.ColaboradorRepository;
import com.sportcenter.shift_manager.repository.EmpresaRepository;
import com.sportcenter.shift_manager.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpresaService {
    private final EmpresaRepository empresaRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    public EmpresaService(EmpresaRepository empresaRepository, ColaboradorRepository colaboradorRepository,
                          UsuarioRepository usuarioRepository, JwtUtil jwtUtil) {
        this.empresaRepository = empresaRepository;
        this.colaboradorRepository = colaboradorRepository;
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
    public Empresa saveEmpresa(Empresa empresa, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        if (empresaRepository.findByUsuarioAndNombre(usuario, empresa.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una empresa con el nombre: " + empresa.getNombre());
        }
        if (empresaRepository.findByUsuarioAndRuc(usuario, empresa.getRuc()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una empresa con el RUC: " + empresa.getRuc());
        }
        empresa.setUsuario(usuario);
        return empresaRepository.save(empresa);
    }

    public List<EmpresaDTO> getAllEmpresas(String token) {
        Usuario usuario = getUsuarioFromToken(token);
        return empresaRepository.findByUsuario(usuario)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public int getNumeroDeEmpleados(Long id, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con ID " + id + " no encontrada"));
        if (!empresa.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para acceder a esta empresa");
        }
        return empresa.getNumeroDeEmpleados();
    }

    @Transactional
    public Empresa toggleHabilitacionEmpresa(Long id, boolean habilitada, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con ID " + id + " no encontrada"));
        if (!empresa.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para modificar esta empresa");
        }
        empresa.setHabilitada(habilitada);
        return empresaRepository.save(empresa);
    }

    public List<EmpresaDTO> getEmpresasPorHabilitacion(boolean habilitada, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        return empresaRepository.findByUsuarioAndHabilitada(usuario, habilitada)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public Empresa updateEmpresa(Long id, Empresa empresaDetails, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con ID " + id + " no encontrada"));
        if (!empresa.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para modificar esta empresa");
        }

        if (!empresa.getNombre().equals(empresaDetails.getNombre()) &&
                empresaRepository.findByUsuarioAndNombre(usuario, empresaDetails.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una empresa con el nombre: " + empresaDetails.getNombre());
        }
        if (!empresa.getRuc().equals(empresaDetails.getRuc()) &&
                empresaRepository.findByUsuarioAndRuc(usuario, empresaDetails.getRuc()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una empresa con el RUC: " + empresaDetails.getRuc());
        }

        empresa.setNombre(empresaDetails.getNombre());
        empresa.setRuc(empresaDetails.getRuc());
        empresa.setHabilitada(empresaDetails.isHabilitada());
        return empresaRepository.save(empresa);
    }

    @Transactional
    public void deleteEmpresa(Long id, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con ID " + id + " no encontrada"));
        if (!empresa.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para eliminar esta empresa");
        }

        List<Colaborador> colaboradores = colaboradorRepository.findByUsuarioAndEmpresaId(usuario, id);
        for (Colaborador colaborador : colaboradores) {
            colaborador.setEmpresa(null);
            colaboradorRepository.save(colaborador);
        }

        empresaRepository.delete(empresa);
    }

    public EmpresaDTO convertToDTO(Empresa empresa) {
        return new EmpresaDTO(
                empresa.getId(),
                empresa.getNombre(),
                empresa.getRuc(),
                empresa.getNumeroDeEmpleados(),
                empresa.isHabilitada()
        );
    }
}