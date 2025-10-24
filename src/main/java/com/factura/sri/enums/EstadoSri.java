package com.factura.sri.enums;

public enum EstadoSri {
    GENERADA,    // 1. Creada en nuestro sistema, no enviada al SRI.
    ENVIADA,     // 2. Enviada al SRI, esperando respuesta.
    AUTORIZADA,  // 3. Aprobada por el SRI.
    RECHAZADA    //4. Rechazada por el SRI.
}
