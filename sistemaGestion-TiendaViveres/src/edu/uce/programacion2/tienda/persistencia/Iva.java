package edu.uce.programacion2.tienda.persistencia;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Clase que gestiona la persistencia de la configuración general del
 * sistema en un archivo binario de acceso aleatorio, siguiendo el mismo
 * patrón que el resto de los DAOs de la capa de persistencia (extiende
 * {@link AccesoAleatorio}).
 *
 * Por ahora solo se persiste el porcentaje de IVA vigente, para que deje
 * de "quemarse" en memoria (antes se reiniciaba a 15% cada vez que se
 * cerraba y volvia a abrir la aplicacion). A diferencia de Categorias,
 * Productos, etc. (que guardan multiples registros), este archivo
 * contiene un UNICO registro de tamaño fijo:
 *
 * <pre>
 * estructura del registro (8 bytes):
 * iva   double   8 bytes   (fraccion, ej. 0.15 = 15%)
 * total:         8 bytes
 * </pre>
 *
 * Si el archivo iva.dat no existe todavia (primera ejecucion del
 * sistema), {@link #leerIva()} retorna {@link #IVA_POR_DEFECTO} sin
 * lanzar ninguna excepcion, y la primera llamada a {@link #guardarIva}
 * crea el archivo automaticamente.
 *
 * @author Equipo08
 */
public class Iva extends AccesoAleatorio {

    private static final int TAM_REGISTRO = 8; // un double (el IVA)

    /** Valor de IVA usado mientras iva.dat todavia no existe (primera ejecucion). */
    public static final double IVA_POR_DEFECTO = 0.15;

    // Constructor que establece el nombre del archivo y el tamano del registro.
    public Iva(String nomArchivo) {
        super(nomArchivo, TAM_REGISTRO);
    }

    // Constructor de conveniencia: usa "iva.dat" en la raiz del proyecto.
    public Iva() {
        this("iva.dat");
    }

    /**
     * Lee el IVA vigente desde iva.dat. Si el archivo no existe todavia,
     * o esta vacio/corrupto, retorna {@link #IVA_POR_DEFECTO} en vez de
     * fallar: el arranque de la aplicacion no debe romperse por esto.
     *
     * <p>Al ser un único registro, no se usa Stream API, pero se mejora
     * el manejo de recursos utilizando try-with-resources.
     */
    public double leerIva() {
        try (RandomAccessFile raf = new RandomAccessFile(nomArchivo, "r")) {
            return raf.readDouble();
        } catch (FileNotFoundException fnf) {
            // El archivo no existe: es la primera ejecución
            return IVA_POR_DEFECTO;
        } catch (IOException ioe) {
            // El archivo existe pero está vacío o corrupto
            return IVA_POR_DEFECTO;
        }
    }

    /**
     * Escribe (crea si no existe, o sobreescribe si ya existe) el IVA
     * vigente en iva.dat.
     *
     * <p>Al ser un único registro, no se usa Stream API, pero se mejora
     * el manejo de recursos utilizando try-with-resources.
     */
    public void guardarIva(double iva) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(nomArchivo, "rw")) {
            raf.seek(0);
            raf.writeDouble(iva);
        }
    }
}