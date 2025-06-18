package com.sportcenter.shift_manager.service;

import com.sportcenter.shift_manager.config.JwtUtil;
import com.sportcenter.shift_manager.dto.ResumenMensualDTO;
import com.sportcenter.shift_manager.dto.TurnoDTO;
import com.sportcenter.shift_manager.dto.TurnoMasaDTO;
import com.sportcenter.shift_manager.exception.ResourceNotFoundException;
import com.sportcenter.shift_manager.model.Colaborador;
import com.sportcenter.shift_manager.model.Empresa;
import com.sportcenter.shift_manager.model.Tienda;
import com.sportcenter.shift_manager.model.Turno;
import com.sportcenter.shift_manager.model.Usuario;
import com.sportcenter.shift_manager.repository.ColaboradorRepository;
import com.sportcenter.shift_manager.repository.TiendaRepository;
import com.sportcenter.shift_manager.repository.TurnoRepository;
import com.sportcenter.shift_manager.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TurnoService {
    private final TurnoRepository turnoRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final TiendaRepository tiendaRepository;
    private final FeriadoService feriadoService;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    public TurnoService(TurnoRepository turnoRepository, ColaboradorRepository colaboradorRepository,
                        TiendaRepository tiendaRepository, FeriadoService feriadoService,
                        UsuarioRepository usuarioRepository, JwtUtil jwtUtil) {
        this.turnoRepository = turnoRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.tiendaRepository = tiendaRepository;
        this.feriadoService = feriadoService;
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
    public TurnoDTO saveTurno(Turno turno, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        if (turno.getColaborador() == null || turno.getColaborador().getId() == null) {
            throw new IllegalArgumentException("El colaborador debe estar especificado en el turno");
        }
        if (turno.getTienda() == null || turno.getTienda().getId() == null) {
            throw new IllegalArgumentException("La tienda debe estar especificada en el turno");
        }
        if (turno.getFecha() == null || turno.getHoraEntrada() == null || turno.getHoraSalida() == null) {
            throw new IllegalArgumentException("Fecha y horas son obligatorias");
        }
        if (!turno.getHoraSalida().isAfter(turno.getHoraEntrada())) {
            throw new IllegalArgumentException("La hora de salida debe ser posterior a la hora de entrada");
        }

        Colaborador colaborador = colaboradorRepository.findById(turno.getColaborador().getId())
                .orElseThrow(() -> new ResourceNotFoundException("El colaborador con ID " + turno.getColaborador().getId() + " no existe"));
        if (!colaborador.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para asignar este colaborador");
        }

        Tienda tienda = tiendaRepository.findById(turno.getTienda().getId())
                .orElseThrow(() -> new ResourceNotFoundException("La tienda con ID " + turno.getTienda().getId() + " no existe"));
        if (!tienda.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para asignar esta tienda");
        }

        if (colaborador.getEmpresa() == null) {
            throw new IllegalArgumentException("El colaborador no tiene una empresa asignada");
        }
        Empresa empresa = colaborador.getEmpresa();
        if (!empresa.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para asignar esta empresa");
        }

        turno.setColaborador(colaborador);
        turno.setEmpresa(empresa);
        turno.setTienda(tienda);
        turno.setUsuario(usuario);
        turno.setEsFeriado(feriadoService.isFeriado(turno.getFecha()));
        Turno savedTurno = turnoRepository.save(turno);
        return convertToDTO(savedTurno);
    }

    public List<TurnoDTO> getTurnosByColaboradorId(Long colaboradorId, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador con ID " + colaboradorId + " no encontrado"));
        if (!colaborador.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para acceder a este colaborador");
        }
        return turnoRepository.findByUsuarioAndColaborador_Id(usuario, colaboradorId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<TurnoDTO> getTurnosPorSemanaDTO(String fecha, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        try {
            LocalDate inicioSemana = getInicioSemana(fecha);
            LocalDate finSemana = inicioSemana.plusDays(6);

            List<Turno> turnos = turnoRepository.findByUsuarioAndFechaBetween(usuario, inicioSemana, finSemana);

            Map<Long, Double> horasSemanalesPorColaborador = new HashMap<>();
            for (Turno turno : turnos) {
                double horasTrabajadas = calcularHorasTrabajadas(turno);
                horasSemanalesPorColaborador.put(
                        turno.getColaborador().getId(),
                        horasSemanalesPorColaborador.getOrDefault(turno.getColaborador().getId(), 0.0) + horasTrabajadas
                );
            }

            return turnos.stream().map(turno -> {
                TurnoDTO dto = convertToDTO(turno);
                dto.setHorasTotalesSemana(horasSemanalesPorColaborador.getOrDefault(turno.getColaborador().getId(), 0.0));
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la semana: " + fecha, e);
        }
    }

    private double calcularHorasTrabajadas(Turno turno) {
        if (turno.getHoraEntrada() != null && turno.getHoraSalida() != null) {
            long minutosTrabajados = java.time.Duration.between(turno.getHoraEntrada(), turno.getHoraSalida()).toMinutes();
            if (turno.getHoraEntrada().isBefore(LocalTime.of(12, 1)) && turno.getHoraSalida().isAfter(LocalTime.of(14, 0))) {
                minutosTrabajados -= 45;
            }
            return minutosTrabajados / 60.0;
        }
        return 0;
    }

    private LocalDate getInicioSemana(String fecha) {
        LocalDate parsedDate = LocalDate.parse(fecha);
        return parsedDate.with(DayOfWeek.MONDAY);
    }

    public List<TurnoDTO> getTurnosMensualesPorColaborador(Long colaboradorId, int mes, int anio, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador con ID " + colaboradorId + " no encontrado"));
        if (!colaborador.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para acceder a este colaborador");
        }
        LocalDate inicioMes = LocalDate.of(anio, mes, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
        return turnoRepository.findByUsuarioAndColaborador_IdAndFechaBetween(usuario, colaboradorId, inicioMes, finMes)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<TurnoDTO> getTurnosMensuales(int mes, int anio, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        LocalDate inicioMes = LocalDate.of(anio, mes, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
        return turnoRepository.findByUsuarioAndFechaBetween(usuario, inicioMes, finMes)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public TurnoDTO updateTurno(Long id, Turno updatedTurno, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        return turnoRepository.findById(id).map(turno -> {
            if (!turno.getUsuario().getId().equals(usuario.getId())) {
                throw new ResourceNotFoundException("No tienes permiso para modificar este turno");
            }
            if (updatedTurno.getColaborador() == null || updatedTurno.getColaborador().getId() == null) {
                throw new IllegalArgumentException("El colaborador debe estar especificado en el turno");
            }
            if (updatedTurno.getTienda() == null || updatedTurno.getTienda().getId() == null) {
                throw new IllegalArgumentException("La tienda debe estar especificada en el turno");
            }
            if (updatedTurno.getFecha() == null || updatedTurno.getHoraEntrada() == null || updatedTurno.getHoraSalida() == null) {
                throw new IllegalArgumentException("Fecha y horas son obligatorias");
            }
            if (!updatedTurno.getHoraSalida().isAfter(updatedTurno.getHoraEntrada())) {
                throw new IllegalArgumentException("La hora de salida debe ser posterior a la hora de entrada");
            }

            Colaborador colaborador = colaboradorRepository.findById(updatedTurno.getColaborador().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Colaborador con ID " + updatedTurno.getColaborador().getId() + " no existe"));
            if (!colaborador.getUsuario().getId().equals(usuario.getId())) {
                throw new ResourceNotFoundException("No tienes permiso para asignar este colaborador");
            }

            Tienda tienda = tiendaRepository.findById(updatedTurno.getTienda().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tienda con ID " + updatedTurno.getTienda().getId() + " no existe"));
            if (!tienda.getUsuario().getId().equals(usuario.getId())) {
                throw new ResourceNotFoundException("No tienes permiso para asignar esta tienda");
            }

            if (colaborador.getEmpresa() == null) {
                throw new IllegalArgumentException("El colaborador no tiene una empresa asignada");
            }
            Empresa empresa = colaborador.getEmpresa();
            if (!empresa.getUsuario().getId().equals(usuario.getId())) {
                throw new ResourceNotFoundException("No tienes permiso para asignar esta empresa");
            }

            turno.setColaborador(colaborador);
            turno.setFecha(updatedTurno.getFecha());
            turno.setHoraEntrada(updatedTurno.getHoraEntrada());
            turno.setHoraSalida(updatedTurno.getHoraSalida());
            turno.setEmpresa(empresa);
            turno.setTienda(tienda);
            turno.setUsuario(usuario);
            turno.setEsFeriado(feriadoService.isFeriado(updatedTurno.getFecha()));
            Turno updated = turnoRepository.save(turno);
            return convertToDTO(updated);
        }).orElseThrow(() -> new ResourceNotFoundException("Turno con ID " + id + " no encontrado"));
    }

    @Transactional
    public void deleteTurno(Long id, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno con ID " + id + " no encontrado"));
        if (!turno.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para eliminar este turno");
        }
        turnoRepository.delete(turno);
    }

    public List<List<String>> calcularSemanasDelMes(int mes, int anio, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        List<List<String>> semanas = new ArrayList<>();
        List<String> semanaActual = new ArrayList<>();
        LocalDate inicioMes = LocalDate.of(anio, mes, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
        LocalDate diaActual = inicioMes;

        while (diaActual.getDayOfWeek() != DayOfWeek.MONDAY && !diaActual.isAfter(finMes)) {
            semanaActual.add(diaActual.toString());
            diaActual = diaActual.plusDays(1);
        }

        if (!semanaActual.isEmpty()) {
            semanas.add(new ArrayList<>(semanaActual));
            semanaActual.clear();
        }

        while (!diaActual.isAfter(finMes)) {
            for (int i = 0; i < 7 && !diaActual.isAfter(finMes); i++) {
                semanaActual.add(diaActual.toString());
                diaActual = diaActual.plusDays(1);
            }
            semanas.add(new ArrayList<>(semanaActual));
            semanaActual.clear();
        }

        if (!semanaActual.isEmpty()) {
            semanas.add(new ArrayList<>(semanaActual));
        }

        for (List<String> semana : semanas) {
            if (!semana.isEmpty()) {
                LocalDate inicioSemana = LocalDate.parse(semana.get(0));
                LocalDate finSemana = LocalDate.parse(semana.get(semana.size() - 1));
                List<Turno> turnos = turnoRepository.findByUsuarioAndFechaBetween(usuario, inicioSemana, finSemana);
                Map<Long, Double> horasSemanalesPorColaborador = new HashMap<>();

                for (Turno turno : turnos) {
                    double horasTrabajadas = calcularHorasTrabajadas(turno);
                    horasSemanalesPorColaborador.put(
                            turno.getColaborador().getId(),
                            horasSemanalesPorColaborador.getOrDefault(turno.getColaborador().getId(), 0.0) + horasTrabajadas
                    );
                }

                for (Turno turno : turnos) {
                    turno.setHorasTrabajadas(horasSemanalesPorColaborador.getOrDefault(turno.getColaborador().getId(), 0.0));
                }
            }
        }

        return semanas;
    }

    public List<TurnoDTO> getTurnosPorSemanaEstricta(int mes, int anio, int numeroSemana, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        List<List<String>> semanasDelMes = calcularSemanasDelMes(mes, anio, token);
        List<TurnoDTO> turnosDTO = new ArrayList<>();

        if (numeroSemana < 1 || numeroSemana > semanasDelMes.size()) {
            throw new IllegalArgumentException("El número de semana " + numeroSemana + " no es válido para el mes " + mes + "/" + anio + ". Hay " + semanasDelMes.size() + " semanas.");
        }

        List<String> semana = semanasDelMes.get(numeroSemana - 1);

        if (!semana.isEmpty()) {
            LocalDate inicioSemana = LocalDate.parse(semana.get(0));
            LocalDate finSemana = LocalDate.parse(semana.get(semana.size() - 1));

            List<Turno> turnos = turnoRepository.findByUsuarioAndFechaBetween(usuario, inicioSemana, finSemana);

            Map<Long, Double> horasSemanalesPorColaborador = new HashMap<>();
            for (Turno turno : turnos) {
                double horasTrabajadas = calcularHorasTrabajadas(turno);
                horasSemanalesPorColaborador.put(
                        turno.getColaborador().getId(),
                        horasSemanalesPorColaborador.getOrDefault(turno.getColaborador().getId(), 0.0) + horasTrabajadas
                );
            }

            for (Turno turno : turnos) {
                TurnoDTO dto = convertToDTO(turno);
                dto.setHorasTotalesSemana(horasSemanalesPorColaborador.getOrDefault(turno.getColaborador().getId(), 0.0));
                turnosDTO.add(dto);
            }
        }

        return turnosDTO;
    }

    public List<TurnoDTO> getColaboradoresPorTiendaYRangoFechas(Long tiendaId, String fechaInicio, String fechaFin, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        Tienda tienda = tiendaRepository.findById(tiendaId)
                .orElseThrow(() -> new ResourceNotFoundException("Tienda con ID " + tiendaId + " no encontrada"));
        if (!tienda.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para acceder a esta tienda");
        }
        try {
            LocalDate parsedFechaInicio = LocalDate.parse(fechaInicio);
            LocalDate parsedFechaFin = LocalDate.parse(fechaFin);
            List<Turno> turnos = turnoRepository.findByUsuarioAndTienda_IdAndFechaBetweenOrderByFechaAsc(usuario, tiendaId, parsedFechaInicio, parsedFechaFin);
            Map<Long, Double> horasTotalesPorColaborador = new HashMap<>();

            for (Turno turno : turnos) {
                double horasTrabajadas = calcularHorasTrabajadas(turno);
                horasTotalesPorColaborador.put(
                        turno.getColaborador().getId(),
                        horasTotalesPorColaborador.getOrDefault(turno.getColaborador().getId(), 0.0) + horasTrabajadas
                );
            }

            return turnos.stream()
                    .map(turno -> {
                        TurnoDTO dto = convertToDTO(turno);
                        dto.setHorasTotalesSemana(horasTotalesPorColaborador.get(turno.getColaborador().getId()));
                        return dto;
                    })
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear las fechas: " + fechaInicio + " - " + fechaFin, e);
        }
    }

    public List<TurnoDTO> getHorasTrabajadasPorColaboradores(List<Long> colaboradores, String fechaInicio, String fechaFin, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = LocalDate.parse(fechaFin);

        List<Colaborador> colaboradoresValidos = colaboradorRepository.findByUsuario(usuario)
                .stream()
                .filter(c -> colaboradores.contains(c.getId()))
                .toList();
        if (colaboradoresValidos.size() != colaboradores.size()) {
            throw new ResourceNotFoundException("Uno o más colaboradores no están autorizados o no existen");
        }

        List<Turno> turnos = turnoRepository.findByUsuarioAndColaborador_IdInAndFechaBetween(usuario, colaboradores, inicio, fin);

        Map<Long, Double> horasTotales = new HashMap<>();
        for (Turno turno : turnos) {
            horasTotales.put(
                    turno.getColaborador().getId(),
                    horasTotales.getOrDefault(turno.getColaborador().getId(), 0.0) + turno.getHorasTrabajadas()
            );
        }

        return turnos.stream().map(turno -> {
            TurnoDTO dto = convertToDTO(turno);
            dto.setHorasTotalesSemana(horasTotales.get(turno.getColaborador().getId()));
            return dto;
        }).distinct().toList();
    }

    public List<TurnoDTO> getTurnosEnFeriados(List<Long> colaboradores, String fechaInicio, String fechaFin, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = LocalDate.parse(fechaFin);

        List<Colaborador> colaboradoresValidos = colaboradorRepository.findByUsuario(usuario)
                .stream()
                .filter(c -> colaboradores.contains(c.getId()))
                .toList();
        if (colaboradoresValidos.size() != colaboradores.size()) {
            throw new ResourceNotFoundException("Uno o más colaboradores no están autorizados o no existen");
        }

        List<Turno> turnos = turnoRepository.findByUsuarioAndColaborador_IdInAndFechaBetween(usuario, colaboradores, inicio, fin)
                .stream()
                .filter(Turno::isEsFeriado)
                .collect(Collectors.toList());

        Map<Long, Double> horasFeriadosPorColaborador = new HashMap<>();
        for (Turno turno : turnos) {
            double horasTrabajadas = calcularHorasTrabajadas(turno);
            horasFeriadosPorColaborador.put(
                    turno.getColaborador().getId(),
                    horasFeriadosPorColaborador.getOrDefault(turno.getColaborador().getId(), 0.0) + horasTrabajadas
            );
        }

        return turnos.stream()
                .map(turno -> {
                    TurnoDTO dto = convertToDTO(turno);
                    dto.setHorasTotalesSemana(horasFeriadosPorColaborador.get(turno.getColaborador().getId()));
                    return dto;
                })
                .distinct()
                .collect(Collectors.toList());
    }

    public List<ResumenMensualDTO> getResumenMensualPorColaboradores(List<Long> colaboradoresIds, int mes, int anio, String token) {
        Usuario usuario = getUsuarioFromToken(token);
        LocalDate inicioMes = LocalDate.of(anio, mes, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

        List<Long> idsAConsultar = colaboradoresIds != null && !colaboradoresIds.isEmpty()
                ? colaboradoresIds
                : colaboradorRepository.findByUsuario(usuario).stream().map(Colaborador::getId).toList();

        List<Colaborador> colaboradoresValidos = colaboradorRepository.findByUsuario(usuario)
                .stream()
                .filter(c -> idsAConsultar.contains(c.getId()))
                .toList();
        if (colaboradoresValidos.size() != idsAConsultar.size()) {
            throw new ResourceNotFoundException("Uno o más colaboradores no están autorizados o no existen");
        }

        List<Turno> turnos = turnoRepository.findByUsuarioAndColaborador_IdInAndFechaBetween(usuario, idsAConsultar, inicioMes, finMes);

        Map<Long, List<Turno>> turnosPorColaborador = turnos.stream()
                .collect(Collectors.groupingBy(t -> t.getColaborador().getId()));

        List<ResumenMensualDTO> resumenes = new ArrayList<>();
        for (Long colaboradorId : idsAConsultar) {
            List<Turno> turnosColaborador = turnosPorColaborador.getOrDefault(colaboradorId, Collections.emptyList());

            double totalHorasMes = turnosColaborador.stream()
                    .mapToDouble(this::calcularHorasTrabajadas)
                    .sum();

            long diasFeriadosTrabajados = turnosColaborador.stream()
                    .filter(Turno::isEsFeriado)
                    .map(Turno::getFecha)
                    .distinct()
                    .count();

            double horasEnFeriados = turnosColaborador.stream()
                    .filter(Turno::isEsFeriado)
                    .mapToDouble(this::calcularHorasTrabajadas)
                    .sum();

            Colaborador colaborador = colaboradorRepository.findById(colaboradorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Colaborador no encontrado"));

            ResumenMensualDTO resumen = new ResumenMensualDTO(
                    colaboradorId,
                    colaborador.getNombre(),
                    totalHorasMes,
                    (int) diasFeriadosTrabajados,
                    horasEnFeriados,
                    turnosColaborador.stream().map(this::convertToDTO).toList()
            );

            resumenes.add(resumen);
        }

        return resumenes;
    }

    public TurnoDTO convertToDTO(Turno turno) {
        boolean tomoAlmuerzo = turno.getHoraEntrada() != null && turno.getHoraSalida() != null &&
                turno.getHoraEntrada().isBefore(LocalTime.of(12, 1)) &&
                turno.getHoraSalida().isAfter(LocalTime.of(14, 0));

        boolean esFeriado = feriadoService.isFeriado(turno.getFecha());

        return new TurnoDTO(
                turno.getId(),
                turno.getColaborador() != null ? turno.getColaborador().getId() : null,
                turno.getColaborador() != null ? turno.getColaborador().getNombre() : "Sin Nombre",
                turno.getColaborador() != null ? turno.getColaborador().getDni() : "Sin DNI",
                turno.getEmpresa() != null ? turno.getEmpresa().getNombre() : "Sin Empresa",
                turno.getEmpresa() != null ? turno.getEmpresa().getId() : null,
                turno.getTienda() != null ? turno.getTienda().getId() : null,
                turno.getTienda() != null ? turno.getTienda().getNombre() : "Sin Tienda",
                turno.getFecha(),
                turno.getHoraEntrada(),
                turno.getHoraSalida(),
                turno.getHorasTrabajadas(),
                tomoAlmuerzo,
                0.0,
                esFeriado
        );
    }

    @Transactional
    public List<TurnoDTO> crearTurnosMasa(TurnoMasaDTO dto, String token) {
        // Obtener el usuario autenticado
        Usuario usuario = getUsuarioFromToken(token);

        // Validaciones básicas
        if (dto.getColaboradorId() == null || dto.getTiendaId() == null || dto.getFechas() == null || dto.getFechas().isEmpty() ||
                dto.getHoraInicio() == null || dto.getHoraFin() == null) {
            throw new IllegalArgumentException("Todos los campos son obligatorios: colaborador, tienda, fechas y horarios");
        }
        if (!dto.getHoraFin().isAfter(dto.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de salida debe ser posterior a la hora de entrada");
        }

        // Obtener y validar el colaborador
        Colaborador colaborador = colaboradorRepository.findById(dto.getColaboradorId())
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador con ID " + dto.getColaboradorId() + " no encontrado"));
        if (!colaborador.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para asignar este colaborador");
        }
        if (colaborador.getEmpresa() == null) {
            throw new IllegalArgumentException("El colaborador no tiene una empresa asignada");
        }
        Empresa empresa = colaborador.getEmpresa();
        if (!empresa.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para asignar esta empresa");
        }

        // Obtener y validar la tienda
        Tienda tienda = tiendaRepository.findById(dto.getTiendaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tienda con ID " + dto.getTiendaId() + " no encontrada"));
        if (!tienda.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("No tienes permiso para asignar esta tienda");
        }

        // Convertir las fechas de String a LocalDate
        List<LocalDate> fechas = dto.getFechas().stream()
                .map(LocalDate::parse)
                .collect(Collectors.toList());

        // Verificar conflictos con turnos existentes para el colaborador en las fechas dadas
        List<Turno> turnosExistentes = turnoRepository.findByUsuarioAndColaborador_IdAndFechaBetween(
                usuario, colaborador.getId(), fechas.get(0), fechas.get(fechas.size() - 1));
        if (!turnosExistentes.isEmpty()) {
            String fechasConflictivas = turnosExistentes.stream()
                    .map(Turno::getFecha)
                    .map(LocalDate::toString)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Ya existen turnos para el colaborador en las fechas: " + fechasConflictivas);
        }

        // Crear los nuevos turnos
        List<Turno> nuevosTurnos = fechas.stream().map(fecha -> {
            Turno turno = new Turno();
            turno.setColaborador(colaborador);
            turno.setFecha(fecha);
            turno.setHoraEntrada(dto.getHoraInicio());
            turno.setHoraSalida(dto.getHoraFin());
            turno.setEmpresa(empresa);
            turno.setTienda(tienda);
            turno.setUsuario(usuario);
            turno.setEsFeriado(feriadoService.isFeriado(fecha));
            return turno;
        }).collect(Collectors.toList());

        // Guardar todos los turnos en la base de datos
        List<Turno> turnosGuardados = turnoRepository.saveAll(nuevosTurnos);

        // Convertir a DTO y devolver
        return turnosGuardados.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}