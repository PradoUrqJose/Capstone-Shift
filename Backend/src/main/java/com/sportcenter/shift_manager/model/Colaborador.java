package com.sportcenter.shift_manager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(
        name = "colaborador",
        indexes = {
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_dni", columnList = "dni"),
                @Index(name = "idx_empresa_id", columnList = "empresa_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Colaborador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 15, message = "El nombre no puede tener más de 15 caracteres.")
    @Column(nullable = false, length = 15)
    private String nombre;

    @Size(max = 20, message = "El apellido no puede tener más de 20 caracteres.")
    @Column(nullable = false, length = 20)
    private String apellido;

    @Column(unique = true, nullable = false, length = 8)
    private String dni;

    @Column(length = 15)
    private String telefono;

    @Column(unique = true, nullable = true, length = 100)
    private String email;

    private String fotoUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id", nullable = true)
    @JsonIgnoreProperties("colaboradores")
    private Empresa empresa;

    @Column(nullable = false)
    private boolean habilitado = true;

    @Column(nullable = true)
    private LocalDate fechaNacimiento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "puesto_id")
    @JsonIgnoreProperties("colaboradores")
    private Puesto puesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario usuario;
}