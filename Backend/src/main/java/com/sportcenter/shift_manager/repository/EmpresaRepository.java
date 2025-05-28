package com.sportcenter.shift_manager.repository;

import com.sportcenter.shift_manager.model.Empresa;
import com.sportcenter.shift_manager.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByNombre(String nombre);
    Optional<Empresa> findByRuc(String ruc);
    List<Empresa> findByHabilitada(boolean habilitada);

    // Nuevos métodos para filtrar por Usuario
    Optional<Empresa> findByUsuarioAndNombre(Usuario usuario, String nombre);
    Optional<Empresa> findByUsuarioAndRuc(Usuario usuario, String ruc);
    List<Empresa> findByUsuarioAndHabilitada(Usuario usuario, boolean habilitada);
    List<Empresa> findByUsuario(Usuario usuario);
}