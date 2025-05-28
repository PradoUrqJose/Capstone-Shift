package com.sportcenter.shift_manager.service;

import com.sportcenter.shift_manager.config.JwtUtil;
import com.sportcenter.shift_manager.dto.PuestoDTO;
import com.sportcenter.shift_manager.exception.ResourceNotFoundException;
import com.sportcenter.shift_manager.model.Puesto;
import com.sportcenter.shift_manager.model.Usuario;
import com.sportcenter.shift_manager.repository.PuestoRepository;
import com.sportcenter.shift_manager.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PuestoService {
    private final PuestoRepository puestoRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    public PuestoService(PuestoRepository puestoRepository, UsuarioRepository usuarioRepository, JwtUtil jwtUtil) {
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
    public PuestoDTO savePuesto(PuestoDTO puestoDTO, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        puestoRepository.findByUsuarioAndNombre(usuario, puestoDTO.getNombre())
                .ifPresent(p -> {
                    throw new IllegalArgumentException("Ya existe un puesto con el nombre: " + puestoDTO.getNombre());
                });

        Puesto puesto = new Puesto();
        puesto.setNombre(puestoDTO.getNombre());
        puesto.setDescripcion(puestoDTO.getDescripcion());
        puesto.setUsuario(usuario);

        Puesto savedPuesto = puestoRepository.save(puesto);
        return convertToDTO(savedPuesto);
    }

    public List<PuestoDTO> getAllPuestos(String token) {
        Usuario usuario = getUsuarioFromToken(token);
        return puestoRepository.findByUsuario(usuario)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PuestoDTO getPuestoById(Long id, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Puesto puesto = puestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto con ID " + id + " no encontrado"));
        if (!puesto.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para acceder a este puesto");
        }
        return convertToDTO(puesto);
    }

    @Transactional
    public PuestoDTO updatePuesto(Long id, PuestoDTO puestoDTO, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Puesto puesto = puestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto con ID " + id + " no encontrado"));
        if (!puesto.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para modificar este puesto");
        }

        puestoRepository.findByUsuarioAndNombre(usuario, puestoDTO.getNombre())
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> {
                    throw new IllegalArgumentException("Ya existe un puesto con el nombre: " + puestoDTO.getNombre());
                });

        puesto.setNombre(puestoDTO.getNombre());
        puesto.setDescripcion(puestoDTO.getDescripcion());

        Puesto updatedPuesto = puestoRepository.save(puesto);
        return convertToDTO(updatedPuesto);
    }

    @Transactional
    public void deletePuesto(Long id, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Puesto puesto = puestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto con ID " + id + " no encontrado"));
        if (!puesto.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para eliminar este puesto");
        }
        puestoRepository.delete(puesto);
    }

    private PuestoDTO convertToDTO(Puesto puesto) {
        return new PuestoDTO(
                puesto.getId(),
                puesto.getNombre(),
                puesto.getDescripcion()
        );
    }
}