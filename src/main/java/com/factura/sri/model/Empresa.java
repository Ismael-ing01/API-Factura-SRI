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
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 13, max = 13, message = "El RUC debe tener 13 caracteres")
    @Column(name = "ruc", nullable = false, unique = true, length = 13)
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Column(name = "razon_social", nullable = false, length = 300)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 300)
    private String nombreComercial;

    @NotBlank(message = "La dirección matriz es obligatoria")
    @Column(name = "direccion_matriz", nullable = false, length = 300)
    private String direccionMatriz;

    @Column(name = "contribuyente_especial", length = 10)
    private String contribuyenteEspecial;

    @Column(name = "obligado_contabilidad", length = 2)
    private String obligadoContabilidad; // SI / NO

    @Column(name = "logo_url")
    private String logoUrl; // URL o path del logo

    @Column(name = "firma_electronica_path")
    private String firmaElectronicaPath;

    @Column(name = "clave_firma")
    private String claveFirma; // Encriptado idealmente

    @Column(name = "ambiente_sri", nullable = false)
    private Integer ambienteSri; // 1: Pruebas, 2: Producción

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Sucursal> sucursales;
}
