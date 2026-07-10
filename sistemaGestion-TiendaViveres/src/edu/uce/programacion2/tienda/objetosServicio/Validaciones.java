package edu.uce.programacion2.tienda.objetosServicio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Clase utilitaria para validaciones de campos comunes.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Validaciones {

    // ── Patrones de validación ──────────────────────────────────────────────

    /** Email: formato estándar con dominio válido. */
    private static final Pattern PATTERN_EMAIL = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    /** Teléfono: 7-10 dígitos, opcional con prefijo. */
    private static final Pattern PATTERN_TELEFONO = Pattern.compile(
            "^[0-9]{7,10}$"
    );

    /** Teléfono con formato internacional: +593 99 999 9999. */
    private static final Pattern PATTERN_TELEFONO_INTERNACIONAL = Pattern.compile(
            "^\\+?[0-9]{1,3}[\\s-]?[0-9]{1,4}[\\s-]?[0-9]{6,10}$"
    );

    /** Cédula ecuatoriana: 10 dígitos. */
    private static final Pattern PATTERN_CEDULA = Pattern.compile(
            "^[0-9]{10}$"
    );

    /** RUC: 13 dígitos. */
    private static final Pattern PATTERN_RUC = Pattern.compile(
            "^[0-9]{13}$"
    );

    // ── Formato de fechas ──────────────────────────────────────────────────

    private static final SimpleDateFormat SDF_FECHA = new SimpleDateFormat("dd/MM/yyyy");

    // ── Validación de Email ──────────────────────────────────────────────────

    public static boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return PATTERN_EMAIL.matcher(email.trim()).matches();
    }

    public static String validarEmailConMensaje(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "El email es obligatorio.";
        }
        if (!PATTERN_EMAIL.matcher(email.trim()).matches()) {
            return "El email no tiene un formato válido. Ejemplo: usuario@dominio.com";
        }
        return null;
    }

    // ── Validación de Teléfono ──────────────────────────────────────────────

    public static boolean validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return false;
        }
        String limpio = telefono.trim().replaceAll("[\\s\\-()]", "");
        return PATTERN_TELEFONO.matcher(limpio).matches()
                || PATTERN_TELEFONO_INTERNACIONAL.matcher(telefono.trim()).matches();
    }

    public static String validarTelefonoConMensaje(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return null; // El teléfono puede ser opcional
        }
        String limpio = telefono.trim().replaceAll("[\\s\\-()]", "");
        if (!PATTERN_TELEFONO.matcher(limpio).matches()
                && !PATTERN_TELEFONO_INTERNACIONAL.matcher(telefono.trim()).matches()) {
            return "El teléfono debe tener entre 7 y 10 dígitos. Ejemplo: 0999123456 o +593 99 912 3456";
        }
        return null;
    }

    // ── Validación de Cédula (Ecuatoriana) ──────────────────────────────────

    public static boolean validarCedula(String cedula) {
        if (cedula == null || !PATTERN_CEDULA.matcher(cedula).matches()) {
            return false;
        }

        try {
            int[] digitos = new int[10];
            for (int i = 0; i < 10; i++) {
                digitos[i] = Integer.parseInt(cedula.substring(i, i + 1));
            }

            // Verificar provincia (primeros 2 dígitos deben estar entre 1 y 24)
            int provincia = digitos[0] * 10 + digitos[1];
            if (provincia < 1 || provincia > 24) {
                return false;
            }

            // Calcular dígito verificador
            int suma = 0;
            for (int i = 0; i < 9; i++) {
                int valor = digitos[i];
                if (i % 2 == 0) {
                    valor *= 2;
                    if (valor > 9) {
                        valor -= 9;
                    }
                }
                suma += valor;
            }

            int residuo = suma % 10;
            int digitoVerificador = (residuo == 0) ? 0 : 10 - residuo;

            return digitos[9] == digitoVerificador;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String validarCedulaConMensaje(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            return "La cédula es obligatoria.";
        }
        if (!PATTERN_CEDULA.matcher(cedula.trim()).matches()) {
            return "La cédula debe tener exactamente 10 dígitos.";
        }
        if (!validarCedula(cedula.trim())) {
            return "La cédula no es válida. Verifique el dígito verificador.";
        }
        return null;
    }

    // ── Validación de RUC ────────────────────────────────────────────────────

    public static boolean validarRuc(String ruc) {
        if (ruc == null || ruc.trim().isEmpty()) {
            return false;
        }
        return PATTERN_RUC.matcher(ruc.trim()).matches();
    }

    public static String validarRucConMensaje(String ruc) {
        if (ruc == null || ruc.trim().isEmpty()) {
            return "El RUC es obligatorio.";
        }
        if (!PATTERN_RUC.matcher(ruc.trim()).matches()) {
            return "El RUC debe tener exactamente 13 dígitos.";
        }
        return null;
    }

    // ── Validación de Fechas ──────────────────────────────────────────────────

    public static boolean validarFechaNoAnterior(Date fecha, boolean permitirPasado) {
        if (fecha == null) {
            return false;
        }
        if (permitirPasado) {
            return true;
        }
        Date hoy = new Date();
        return !fecha.before(truncarFecha(hoy));
    }

    public static String validarFechaConMensaje(String fechaStr, boolean permitirPasado, String nombreCampo) {
        if (fechaStr == null || fechaStr.trim().isEmpty() || fechaStr.equals("__/__/____")) {
            return "La " + nombreCampo + " es obligatoria.";
        }
        try {
            Date fecha = SDF_FECHA.parse(fechaStr.trim());
            if (!permitirPasado) {
                Date hoy = truncarFecha(new Date());
                if (fecha.before(hoy)) {
                    return "La " + nombreCampo + " no puede ser anterior a hoy.";
                }
            }
            return null;
        } catch (ParseException e) {
            return "La " + nombreCampo + " no tiene un formato válido. Use dd/MM/yyyy";
        }
    }

    private static Date truncarFecha(Date fecha) {
        try {
            String str = SDF_FECHA.format(fecha);
            return SDF_FECHA.parse(str);
        } catch (ParseException e) {
            return fecha;
        }
    }

    // ── Validación de Números ──────────────────────────────────────────────────

    public static String validarNumeroPositivo(String valor, String nombreCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            return "El " + nombreCampo + " es obligatorio.";
        }
        try {
            double num = Double.parseDouble(valor.trim());
            if (num <= 0) {
                return "El " + nombreCampo + " debe ser mayor a 0.";
            }
            return null;
        } catch (NumberFormatException e) {
            return "El " + nombreCampo + " debe ser un número válido.";
        }
    }

    public static String validarEnteroPositivo(String valor, String nombreCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            return "El " + nombreCampo + " es obligatorio.";
        }
        try {
            int num = Integer.parseInt(valor.trim());
            if (num <= 0) {
                return "El " + nombreCampo + " debe ser mayor a 0.";
            }
            return null;
        } catch (NumberFormatException e) {
            return "El " + nombreCampo + " debe ser un número entero válido.";
        }
    }
}