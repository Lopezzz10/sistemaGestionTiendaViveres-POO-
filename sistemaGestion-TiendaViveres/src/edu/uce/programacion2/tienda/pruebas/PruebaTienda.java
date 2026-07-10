package edu.uce.programacion2.tienda.pruebas;

import edu.uce.programacion2.tienda.control.Control;
import edu.uce.programacion2.tienda.interfaz.VentanaPrincipal;
import edu.uce.programacion2.tienda.negocio.Administrador;
import edu.uce.programacion2.tienda.negocio.Cajero;
import javax.swing.*;

public class PruebaTienda {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> {
            // Un único Control para toda la sesión: el login y la ventana
            // principal comparten la misma fachada (mismos datos en memoria).
            Control control = new Control();

            // ── Datos de prueba ─────────────────────────────────────
            // Crea un Administrador y un Cajero de ejemplo para poder probar
            // el login y la restricción de menú por rol . Si ya
            // existen (por ejemplo, persistidos en un .dat de una corrida
            // anterior), agregarUsuario lanzará FachadaException y se ignora.
            crearUsuariosDePrueba(control);

            // Muestra el login y, si es exitoso, abre la ventana principal.
            // Este mismo metodo se reutiliza al "Cerrar Sesion" desde la
            // ventana principal, para volver a autenticar sin reiniciar
            // la ejecucion del programa.
            VentanaPrincipal.mostrarLogin(control);
        });
    }

    /**
     * Crea un Administrador y un Cajero de ejemplo para poder probar el
     * login con ambos roles. Las contraseñas cumplen las reglas
     * de {@link Administrador#autenticar()} (>= 8 caracteres) y
     * {@link Cajero#autenticar()} (>= 6 caracteres y caja asignada > 0).
     */
    private static void crearUsuariosDePrueba(Control control) {
        try {
            control.getFachada().agregarUsuario(
                    new Administrador(1, "Admin Prueba", "admin@tienda.com",
                            "admin1234", "MAÑANA"));
        } catch (Exception ex) {
            // Ya existe (por ejemplo, persistido en usuarios.dat): se ignora.
        }

        try {
            control.getFachada().agregarUsuario(
                    new Cajero(2, "Cajero Prueba", "cajero@tienda.com",
                            "cajero12", 1));
        } catch (Exception ex) {
            // Ya existe (por ejemplo, persistido en usuarios.dat): se ignora.
        }
    }
}