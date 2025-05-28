package com.sportcenter.shift_manager.repository;

import com.sportcenter.shift_manager.model.Puesto;
import com.sportcenter.shift_manager.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PuestoRepository extends JpaRepository<Puesto, Long> {
    Optional<Puesto> findByNombre(String nombre);

    // Nuevos métodos para filtrar por Usuario
    Optional<Puesto> findByUsuarioAndNombre(Usuario usuario, String nombre);
    List<Puesto> findByUsuario(Usuario usuario);
}