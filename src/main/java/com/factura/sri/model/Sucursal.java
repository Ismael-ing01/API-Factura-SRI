package com.factura.sri.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "sucursal")
public class Sucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código de establecimiento es obligatorio")
    @Size(min = 3, max = 3, message = "El código debe tener 3 dígitos (ej: 001)")
    @Column(name = "codigo", nullable = false, length = 3)
    private String codigo; // 001

    @NotBlank(message = "La dirección de la sucursal es obligatoria")
    @Column(name = "direccion", nullable = false, length = 300)
    private String direccion;

    @Column(name = "nombre", nullable = false)
    private String nombre; // Nombre amigable (ej: "Matriz", "Sucursal Centro")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @OneToMany(mappedBy = "sucursal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Caja> cajas;
}
