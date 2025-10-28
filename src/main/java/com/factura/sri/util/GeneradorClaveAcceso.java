package com.factura.sri.util;

import com.factura.sri.model.Factura;
import java.time.format.DateTimeFormatter;

public class GeneradorClaveAcceso {

    // Formateador para la fecha en formato ddmmaaaa
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    // Constantes para los códigos fijos (según Ficha Técnica)
    private static final String TIPO_COMPROBANTE_FACTURA = "01"; // Tabla 3 [cite: 119]
    private static final String TIPO_EMISION_NORMAL = "1";     // Tabla 2 [cite: 115]

    /**
     * Genera la clave de acceso de 49 dígitos para una factura.
     *
     * @param factura La entidad Factura con los datos necesarios.
     * @param rucEmisor El RUC de la empresa que emite la factura.
     * @param tipoAmbiente "1" para Pruebas, "2" para Producción (Tabla 4 [cite: 126]).
     * @return La clave de acceso de 49 dígitos.
     */
    public static String generarClaveAcceso(Factura factura, String rucEmisor, String tipoAmbiente) {

        // --- 1. Formatear la Fecha ---
        String fechaEmisionStr = factura.getFechaEmision().format(DATE_FORMATTER);

        // Validar que fechaEmisionStr solo contenga dígitos
        if (!fechaEmisionStr.matches("\\d+")) {
            throw new IllegalArgumentException("La fecha de emisión no es numérica: " + fechaEmisionStr);
        }

        // --- 2. Extraer Serie y Secuencial ---
        // Asume que numeroFactura es "001-001-000000001"
        String[] partesNumero = factura.getNumeroFactura().split("-");
        if (partesNumero.length != 3) {
            throw new IllegalArgumentException("Formato de numeroFactura inválido: " + factura.getNumeroFactura());
        }
        String establecimiento = partesNumero[0]; // "001"
        String puntoEmision = partesNumero[1];    // "001"
        String secuencial = partesNumero[2];      // "000000001"

        // Validar que todos son numéricos
        if (!establecimiento.matches("\\d{3}")) {
            throw new IllegalArgumentException("El establecimiento debe ser numérico y de 3 dígitos: " + establecimiento);
        }
        if (!puntoEmision.matches("\\d{3}")) {
            throw new IllegalArgumentException("El punto de emisión debe ser numérico y de 3 dígitos: " + puntoEmision);
        }
        if (!secuencial.matches("\\d{9}")) {
            throw new IllegalArgumentException("El secuencial debe ser numérico y de 9 dígitos: " + secuencial);
        }

        String serie = establecimiento + puntoEmision; // "001001"

        // --- 3. Generar Código Numérico Aleatorio ---
        String codigoNumerico = String.format("%08d", (int) (Math.random() * 100000000));

        // --- 4. Validar rucEmisor y tipoAmbiente ---
        if (rucEmisor == null || !rucEmisor.matches("\\d{13}")) {
            throw new IllegalArgumentException("El RUC del emisor debe tener exactamente 13 dígitos numéricos: " + rucEmisor);
        }
        if (tipoAmbiente == null || !(tipoAmbiente.equals("1") || tipoAmbiente.equals("2"))) {
            throw new IllegalArgumentException("El tipo de ambiente debe ser '1' o '2', recibido: " + tipoAmbiente);
        }

        // --- 5. Concatenar los Primeros 48 Dígitos ---
        StringBuilder claveSinDigito = new StringBuilder();
        claveSinDigito.append(fechaEmisionStr);            // 8 dígitos [cite: 90]
        claveSinDigito.append(TIPO_COMPROBANTE_FACTURA); // 2 dígitos [cite: 90]
        claveSinDigito.append(rucEmisor);                 // 13 dígitos [cite: 90]
        claveSinDigito.append(tipoAmbiente);              // 1 dígito [cite: 90]
        claveSinDigito.append(serie);                     // 6 dígitos [cite: 90]
        claveSinDigito.append(secuencial);                // 9 dígitos [cite: 90]
        claveSinDigito.append(codigoNumerico);            // 8 dígitos [cite: 90]
        claveSinDigito.append(TIPO_EMISION_NORMAL);       // 1 dígito [cite: 90]

        // Validar que la claveSinDigito son solo números
        if (!claveSinDigito.toString().matches("\\d{48}")) {
            throw new IllegalStateException("La clave de acceso generada (sin dígito) no es 100% numérica: " + claveSinDigito);
        }

        // --- 6. Calcular el Dígito Verificador (Módulo 11) ---
        int digitoVerificador = calcularModulo11(claveSinDigito.toString());

        // --- 7. Añadir Dígito Verificador y Devolver ---
        return claveSinDigito.append(digitoVerificador).toString();
    }

    /**
     * Calcula el dígito verificador usando el algoritmo Módulo 11
     * con factor ponderado 2-7 repetido (especificación SRI [cite: 92]).
     *
     * @param cadena48 La cadena de 48 dígitos numéricos.
     * @return El dígito verificador (0-9).
     */
    private static int calcularModulo11(String cadena48) {
        int[] factores = {7, 6, 5, 4, 3, 2}; // Se repiten
        int suma = 0;
        int factorIndex = 0;

        // Recorre la cadena de derecha a izquierda
        for (int i = cadena48.length() - 1; i >= 0; i--) {
            char ch = cadena48.charAt(i);
            if (!Character.isDigit(ch)) {
                throw new IllegalArgumentException("Caracter no numérico en clave de acceso: '" + ch + "' en posición " + i);
            }
            int digito = Character.digit(ch, 10);
            suma += digito * factores[factorIndex];
            factorIndex = (factorIndex + 1) % factores.length; // Cicla los factores 2-7
        }

        int residuo = suma % 11;
        int digito = 11 - residuo;

        // Casos especiales según SRI [cite: 93]
        if (digito == 11) {
            return 0;
        } else if (digito == 10) {
            return 1;
        } else {
            return digito;
        }
    }
}
