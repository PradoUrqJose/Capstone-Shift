package com.sportcenter.shift_manager.repository;

import com.sportcenter.shift_manager.model.Turno;
import com.sportcenter.shift_manager.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    List<Turno> findByColaborador_Id(Long colaboradorId);
    List<Turno> findByFechaBetween(LocalDate startDate, LocalDate endDate);

    List<Turno> findByColaborador_IdAndFechaBetween(Long colaboradorId, LocalDate startDate, LocalDate endDate);

    List<Turno> findByTienda_IdAndFechaBetweenOrderByFechaAsc(
            Long tiendaId,
            LocalDate fechaInicio,
            LocalDate fechaFin);

    List<Turno> findByColaborador_IdInAndFechaBetween(List<Long> colaboradores, LocalDate inicio, LocalDate fin);

    // Nuevos métodos para filtrar por Usuario
    List<Turno> findByUsuario(Usuario usuario);
    List<Turno> findByUsuarioAndColaborador_Id(Usuario usuario, Long colaboradorId);
    List<Turno> findByUsuarioAndFechaBetween(Usuario usuario, LocalDate startDate, LocalDate endDate);
    List<Turno> findByUsuarioAndColaborador_IdAndFechaBetween(Usuario usuario, Long colaboradorId, LocalDate startDate, LocalDate endDate);
    List<Turno> findByUsuarioAndTienda_IdAndFechaBetweenOrderByFechaAsc(
            Usuario usuario,
            Long tiendaId,
            LocalDate fechaInicio,
            LocalDate fechaFin);
    List<Turno> findByUsuarioAndColaborador_IdInAndFechaBetween(Usuario usuario, List<Long> colaboradores, LocalDate inicio, LocalDate fin);
}