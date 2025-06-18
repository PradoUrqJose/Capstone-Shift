package com.sportcenter.shift_manager.controller;

import com.sportcenter.shift_manager.dto.ResumenMensualDTO;
import com.sportcenter.shift_manager.dto.TurnoDTO;
import com.sportcenter.shift_manager.dto.TurnoMasaDTO;
import com.sportcenter.shift_manager.model.Turno;
import com.sportcenter.shift_manager.service.TurnoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {
    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @PostMapping
    public ResponseEntity<TurnoDTO> saveTurno(@Valid @RequestBody Turno turno,
                                              @RequestHeader("Authorization") String token) {
        TurnoDTO savedTurno = turnoService.saveTurno(turno, token);
        return ResponseEntity.ok(savedTurno);
    }

    @PostMapping("/masa")
    public ResponseEntity<List<TurnoDTO>> crearTurnosMasa(
            @Valid @RequestBody TurnoMasaDTO dto,
            @RequestHeader("Authorization") String token) {
        List<TurnoDTO> turnosCreados = turnoService.crearTurnosMasa(dto, token);
        return ResponseEntity.ok(turnosCreados);
    }

    @GetMapping("/{colaboradorId}")
    public ResponseEntity<List<TurnoDTO>> getTurnosByColaborador(@PathVariable Long colaboradorId,
                                                                 @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(turnoService.getTurnosByColaboradorId(colaboradorId, token));
    }

    @GetMapping
    public ResponseEntity<List<TurnoDTO>> getTurnosPorSemana(@RequestParam("fecha") String fecha,
                                                             @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(turnoService.getTurnosPorSemanaDTO(fecha, token));
    }

    @GetMapping("/mensual/{colaboradorId}")
    public ResponseEntity<List<TurnoDTO>> getTurnosMensualesPorColaborador(
            @PathVariable Long colaboradorId,
            @RequestParam("mes") int mes,
            @RequestParam("anio") int anio,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(turnoService.getTurnosMensualesPorColaborador(colaboradorId, mes, anio, token));
    }

    @GetMapping("/mensual")
    public ResponseEntity<List<TurnoDTO>> getTurnosMensuales(
            @RequestParam("mes") int mes,
            @RequestParam("anio") int anio,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(turnoService.getTurnosMensuales(mes, anio, token));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TurnoDTO> updateTurno(@PathVariable Long id, @Valid @RequestBody Turno turno,
                                                @RequestHeader("Authorization") String token) {
        TurnoDTO updatedTurno = turnoService.updateTurno(id, turno, token);
        return ResponseEntity.ok(updatedTurno);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTurno(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        turnoService.deleteTurno(id, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/semanas-del-mes")
    public ResponseEntity<List<List<String>>> getSemanasDelMes(
            @RequestParam("mes") int mes,
            @RequestParam("anio") int anio,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(turnoService.calcularSemanasDelMes(mes, anio, token));
    }

    @GetMapping("/semanal-estricto")
    public ResponseEntity<List<TurnoDTO>> getTurnosPorSemanaEstricta(
            @RequestParam("mes") int mes,
            @RequestParam("anio") int anio,
            @RequestParam("semana") int numeroSemana,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(turnoService.getTurnosPorSemanaEstricta(mes, anio, numeroSemana, token));
    }

    @GetMapping("/colab-tienda-fecha")
    public ResponseEntity<List<TurnoDTO>> getColaboradoresPorTiendaYRangoFechas(
            @RequestParam("tiendaId") Long tiendaId,
            @RequestParam("fechaInicio") String fechaInicio,
            @RequestParam("fechaFin") String fechaFin,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(turnoService.getColaboradoresPorTiendaYRangoFechas(tiendaId, fechaInicio, fechaFin, token));
    }

    @GetMapping("/reporte")
    public ResponseEntity<List<TurnoDTO>> getHorasTrabajadasPorColaboradores(
            @RequestParam("fechaInicio") String fechaInicio,
            @RequestParam("fechaFin") String fechaFin,
            @RequestParam(value = "colaboradores", required = false) String colaboradores,
            @RequestHeader("Authorization") String token) {
        List<Long> colaboradoresIds = (colaboradores != null && !colaboradores.isEmpty())
                ? Arrays.stream(colaboradores.split(",")).map(Long::parseLong).toList()
                : new ArrayList<>();
        return ResponseEntity.ok(turnoService.getHorasTrabajadasPorColaboradores(colaboradoresIds, fechaInicio, fechaFin, token));
    }

    @GetMapping("/reporte/feriados")
    public ResponseEntity<List<TurnoDTO>> getTurnosEnFeriados(
            @RequestParam("fechaInicio") String fechaInicio,
            @RequestParam("fechaFin") String fechaFin,
            @RequestParam(value = "colaboradores", required = false) String colaboradores,
            @RequestHeader("Authorization") String token) {
        List<Long> colaboradoresIds = (colaboradores != null && !colaboradores.isEmpty())
                ? Arrays.stream(colaboradores.split(",")).map(Long::parseLong).toList()
                : new ArrayList<>();
        return ResponseEntity.ok(turnoService.getTurnosEnFeriados(colaboradoresIds, fechaInicio, fechaFin, token));
    }

    @GetMapping("/resumen-mensual")
    public ResponseEntity<List<ResumenMensualDTO>> getResumenMensual(
            @RequestParam("mes") int mes,
            @RequestParam("anio") int anio,
            @RequestParam(value = "colaboradores", required = false) String colaboradores,
            @RequestHeader("Authorization") String token) {
        List<Long> colaboradoresIds = (colaboradores != null && !colaboradores.isEmpty())
                ? Arrays.stream(colaboradores.split(",")).map(Long::parseLong).toList()
                : new ArrayList<>();
        return ResponseEntity.ok(turnoService.getResumenMensualPorColaboradores(colaboradoresIds, mes, anio, token));
    }
}