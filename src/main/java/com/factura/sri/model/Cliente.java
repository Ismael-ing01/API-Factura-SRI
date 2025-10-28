package com.factura.sri.model;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(min = 5, max = 50, message = "Los nombres deben tener entre 5 y 50 caracteres")
    @Column(name = "nombres", nullable = false)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 5, max = 50, message = "Los apellidos deben tener entre 5 y 50 caracteres")
    @Column(name = "apellidos", nullable = false)
    private String apellidos;

    @Size(min = 5, max = 50, message = "La direccion debe tener entre 5 y 50 caracteres")
    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Size(max = 10, message = "El numero de telefono debe tener maximo 10 digitos")
    @Pattern(regexp = "^[0-9]{10}$")
    private String telefono;

    @Size(max = 40, message = "El correo debe tener maximo 40 caracteres")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es valido")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "El Estado es obligatorio")
    @Size(max = 1)
    @Column(name = "estado", nullable = false)
    private String estado;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentoCliente> documentoClientes;
}
