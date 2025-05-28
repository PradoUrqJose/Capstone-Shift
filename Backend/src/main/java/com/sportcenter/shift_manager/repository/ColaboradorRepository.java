package com.sportcenter.shift_manager.repository;

import com.sportcenter.shift_manager.model.Colaborador;
import com.sportcenter.shift_manager.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {
    List<Colaborador> findByEmpresaId(Long empresaId);

    List<Colaborador> findByHabilitado(boolean habilitado);
    Optional<Colaborador> findByEmail(String email);
    Optional<Colaborador> findByDni(String dni);
    Optional<Colaborador> findByNombreAndApellido(String nombre, String apellido);

    // Nuevos métodos para filtrar por Usuario
    List<Colaborador> findByUsuario(Usuario usuario);
    List<Colaborador> findByUsuarioAndEmpresaId(Usuario usuario, Long empresaId);
    List<Colaborador> findByUsuarioAndHabilitado(Usuario usuario, boolean habilitado);
    Optional<Colaborador> findByUsuarioAndEmail(Usuario usuario, String email);
    Optional<Colaborador> findByUsuarioAndDni(Usuario usuario, String dni);
    Optional<Colaborador> findByUsuarioAndNombreAndApellido(Usuario usuario, String nombre, String apellido);
}