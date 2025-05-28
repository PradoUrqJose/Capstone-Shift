package com.sportcenter.shift_manager.service;

import com.sportcenter.shift_manager.config.JwtUtil;
import com.sportcenter.shift_manager.dto.TiendaDTO;
import com.sportcenter.shift_manager.exception.ResourceNotFoundException;
import com.sportcenter.shift_manager.model.Tienda;
import com.sportcenter.shift_manager.model.Usuario;
import com.sportcenter.shift_manager.repository.TiendaRepository;
import com.sportcenter.shift_manager.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TiendaService {
    private final TiendaRepository tiendaRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    public TiendaService(TiendaRepository tiendaRepository, UsuarioRepository usuarioRepository, JwtUtil jwtUtil) {
        this.tiendaRepository = tiendaRepository;
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
    public TiendaDTO saveTienda(TiendaDTO tiendaDTO, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        tiendaRepository.findByUsuarioAndNombre(usuario, tiendaDTO.getNombre())
                .ifPresent(t -> {
                    throw new IllegalArgumentException("Ya existe una tienda con el nombre: " + tiendaDTO.getNombre());
                });

        Tienda tienda = new Tienda();
        tienda.setNombre(tiendaDTO.getNombre());
        tienda.setDireccion(tiendaDTO.getDireccion());
        tienda.setUsuario(usuario);
        Tienda savedTienda = tiendaRepository.save(tienda);
        return convertToDTO(savedTienda);
    }

    public List<TiendaDTO> getAllTiendas(String token) {
        Usuario usuario = getUsuarioFromToken(token);
        return tiendaRepository.findByUsuario(usuario)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TiendaDTO getTiendaById(Long id, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Tienda tienda = tiendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tienda con ID " + id + " no encontrada"));
        if (!tienda.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para acceder a esta tienda");
        }
        return convertToDTO(tienda);
    }

    @Transactional
    public TiendaDTO updateTienda(Long id, TiendaDTO tiendaDTO, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Tienda tienda = tiendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tienda con ID " + id + " no encontrada"));
        if (!tienda.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para modificar esta tienda");
        }

        tiendaRepository.findByUsuarioAndNombre(usuario, tiendaDTO.getNombre())
                .filter(t -> !t.getId().equals(id))
                .ifPresent(t -> {
                    throw new IllegalArgumentException("Ya existe una tienda con el nombre: " + tiendaDTO.getNombre());
                });

        tienda.setNombre(tiendaDTO.getNombre());
        tienda.setDireccion(tiendaDTO.getDireccion());
        Tienda updatedTienda = tiendaRepository.save(tienda);
        return convertToDTO(updatedTienda);
    }

    @Transactional
    public void deleteTienda(Long id, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Tienda tienda = tiendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tienda con ID " + id + " no encontrada"));
        if (!tienda.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para eliminar esta tienda");
        }
        tiendaRepository.delete(tienda);
    }

    private TiendaDTO convertToDTO(Tienda tienda) {
        return new TiendaDTO(tienda.getId(), tienda.getNombre(), tienda.getDireccion());
    }
}