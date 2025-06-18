package com.sportcenter.shift_manager.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;

public class TurnoMasaDTO {
    @NotNull(message = "El ID del colaborador es obligatorio")
    private Long colaboradorId;

    @NotNull(message = "El ID de la tienda es obligatorio")
    private Long tiendaId;

    @NotNull(message = "Las fechas son obligatorias")
    private List<String> fechas; // Lista de fechas en formato "yyyy-MM-dd"

    @NotNull(message = "La hora de entrada es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de salida es obligatoria")
    private LocalTime horaFin;

    // Constructor vacío
    public TurnoMasaDTO() {}

    // Getters y Setters
    public Long getColaboradorId() {
        return colaboradorId;
    }

    public void setColaboradorId(Long colaboradorId) {
        this.colaboradorId = colaboradorId;
    }

    public Long getTiendaId() {
        return tiendaId;
    }

    public void setTiendaId(Long tiendaId) {
        this.tiendaId = tiendaId;
    }

    public List<String> getFechas() {
        return fechas;
    }

    public void setFechas(List<String> fechas) {
        this.fechas = fechas;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }
}