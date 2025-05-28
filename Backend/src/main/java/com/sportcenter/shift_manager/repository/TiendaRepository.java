package com.sportcenter.shift_manager.repository;

import com.sportcenter.shift_manager.model.Tienda;
import com.sportcenter.shift_manager.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TiendaRepository extends JpaRepository<Tienda, Long> {
    Optional<Tienda> findByNombre(String nombre);

    // Nuevos métodos para filtrar por Usuario
    Optional<Tienda> findByUsuarioAndNombre(Usuario usuario, String nombre);
    List<Tienda> findByUsuario(Usuario usuario);
}