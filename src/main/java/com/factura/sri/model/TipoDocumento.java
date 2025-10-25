package com.factura.sri.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private Long idTipoDocumento;

    @NotBlank(message = "El codigo del tipo de documento es obligatorio")
    @Size(max =3)
    @Column(name = "codigo_tipo_documento", nullable = false)
    private String codigoTipoDocumento;

    @NotBlank(message = "El nombre del tipo de documento es obligatorio")
    @Size(max = 30)
    @Column(name = "nombre_tipo_documento", nullable = false)
    private String nombreTipoDocumento;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDate fechaCreacion;

    // --- AÑADE ESTE CAMPO ---
    @NotEmpty(message = "El código SRI del tipo de documento es obligatorio")
    @Size(max = 2, message = "El código SRI debe tener máximo 2 caracteres") // Para "04", "05", etc.
    @Column(name = "codigo_sri", nullable = false, length = 2)
    private String codigoSri; // Guarda el código numérico que usa el SRI
    // -------------------------

}
