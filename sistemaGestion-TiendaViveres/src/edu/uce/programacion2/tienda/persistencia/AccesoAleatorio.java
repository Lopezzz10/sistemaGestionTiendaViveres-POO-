package edu.uce.programacion2.tienda.persistencia;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Clase base que implementa el acceso a archivos binarios de acceso aleatorio.
 * Todos los registros tienen un tamano fijo, lo que permite calcular su posicion
 * con la formula: posicion = numero_de_registro * tamano_de_registro.
 * Las clases hijas llaman a super(nomArchivo, tamRegistro) indicando el tamano
 * exacto en bytes de cada registro, que el profesor puede verificar en el constructor.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class AccesoAleatorio {

    // Objeto para leer y escribir en posiciones especificas del archivo
    protected RandomAccessFile archivo;
    // Nombre del archivo binario en disco
    protected String nomArchivo;
    // Tamano fijo en bytes de cada registro del archivo
    protected int tamRegistro;
    // Arreglo de ceros para borrar un registro fisicamente
    protected byte[] blancos;

    // Constructor que inicializa el archivo y prepara el arreglo de borrado.
    public AccesoAleatorio(String nomArchivo, int tamRegistro) {
        this.nomArchivo   = nomArchivo;
        this.tamRegistro  = tamRegistro;
        this.blancos      = new byte[tamRegistro];
        for (int i = 0; i < tamRegistro; i++) blancos[i] = 0;
    }

    // =========================================================================
    // lectura y escritura de tipos basicos
    // =========================================================================

    // Lee exactamente tam caracteres del archivo y devuelve un String sin espacios sobrantes.
    public String leeString(int tam) throws IOException {
        char[] cadena = new char[tam];
        for (int i = 0; i < tam; i++) cadena[i] = archivo.readChar();
        return new String(cadena).replace('\u0000', ' ').trim();
    }

    // Escribe una cadena de longitud fija al archivo.
    // Si la cadena es mas corta que tam, rellena con caracteres nulos.
    public void escribeString(String sCadena, int tam) throws IOException {
        StringBuffer buffer = (sCadena != null)
                ? new StringBuffer(sCadena) : new StringBuffer(tam);
        buffer.setLength(tam);
        archivo.writeChars(buffer.toString());
    }

    // =========================================================================
    // lectura y escritura de fechas y horas de java.time
    // =========================================================================

    // Lee una fecha del archivo (anho, mes, dia) como tres enteros.
    public LocalDate leeLocalDate() throws IOException {
        int anho = archivo.readInt();
        int mes  = archivo.readInt();
        int dia  = archivo.readInt();
        return LocalDate.of(anho, mes, dia);
    }

    // Escribe una fecha al archivo como tres enteros: anho, mes, dia.
    // Si la fecha es null, escribe tres ceros.
    public void escribeLocalDate(LocalDate fecha) throws IOException {
        if (fecha != null) {
            archivo.writeInt(fecha.getYear());
            archivo.writeInt(fecha.getMonthValue());
            archivo.writeInt(fecha.getDayOfMonth());
        } else {
            archivo.writeInt(0);
            archivo.writeInt(0);
            archivo.writeInt(0);
        }
    }

    // Lee una hora del archivo (hora, minuto) como dos enteros.
    public LocalTime leeLocalTime() throws IOException {
        int hora   = archivo.readInt();
        int minuto = archivo.readInt();
        return LocalTime.of(hora, minuto);
    }

    // Escribe una hora al archivo como dos enteros: hora, minuto.
    // Si la hora es null, escribe dos ceros.
    public void escribeLocalTime(LocalTime hora) throws IOException {
        if (hora != null) {
            archivo.writeInt(hora.getHour());
            archivo.writeInt(hora.getMinute());
        } else {
            archivo.writeInt(0);
            archivo.writeInt(0);
        }
    }

    // Lee una fecha y hora del archivo (anho, mes, dia, hora, minuto, segundo) como seis enteros.
    public LocalDateTime leeLocalDateTime() throws IOException {
        int anho    = archivo.readInt();
        int mes     = archivo.readInt();
        int dia     = archivo.readInt();
        int hora    = archivo.readInt();
        int minuto  = archivo.readInt();
        int segundo = archivo.readInt();
        return LocalDateTime.of(anho, mes, dia, hora, minuto, segundo);
    }

    // Escribe una fecha y hora al archivo como seis enteros.
    // Si el parametro es null, escribe seis ceros.
    public void escribeLocalDateTime(LocalDateTime dt) throws IOException {
        if (dt != null) {
            archivo.writeInt(dt.getYear());
            archivo.writeInt(dt.getMonthValue());
            archivo.writeInt(dt.getDayOfMonth());
            archivo.writeInt(dt.getHour());
            archivo.writeInt(dt.getMinute());
            archivo.writeInt(dt.getSecond());
        } else {
            for (int i = 0; i < 6; i++) archivo.writeInt(0);
        }
    }

    // =========================================================================
    // borrado fisico y empaque
    // =========================================================================

    // Sobreescribe la posicion actual del archivo con el arreglo de ceros.
    // Se usa para marcar un registro como borrado fisicamente.
    public void borraRegistro() throws IOException {
        archivo.write(blancos);
    }

    // Verifica si un registro contiene solo ceros (fue borrado fisicamente).
    public boolean estaRegistroBorrado(byte[] registro) {
        for (int i = 0; i < tamRegistro; i++) {
            if (registro[i] != 0) return false;
        }
        return true;
    }

    // Elimina fisicamente los registros en ceros del archivo compactandolo.
    // Desplaza los registros validos hacia el inicio y trunca el archivo.
    public void empaca() throws IOException {
        byte[] registro = new byte[tamRegistro];
        int registrosBorrados = 0;
        int numRegistros = (int) (archivo.length() / tamRegistro);

        for (int i = 0; i < numRegistros; i++) {
            archivo.seek((long) i * tamRegistro);
            archivo.read(registro);
            if (estaRegistroBorrado(registro)) {
                for (int j = i; j < numRegistros - 1; j++) {
                    archivo.seek((long) (j + 1) * tamRegistro);
                    archivo.read(registro);
                    archivo.seek((long) j * tamRegistro);
                    archivo.write(registro);
                }
                numRegistros--;
                registrosBorrados++;
            }
        }
        archivo.setLength(archivo.length() - (long) registrosBorrados * tamRegistro);
    }

    // Devuelve la cantidad de registros almacenados en el archivo.
    // Util para verificar el contenido del archivo en tiempo de ejecucion.
    public long numRegistros() {
        try {
            File f = new File(nomArchivo);
            if (!f.exists()) return 0;
            return f.length() / tamRegistro;
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================================================================
    // lectura de registros con Stream API (reemplaza el patron
    // "while(true) { leer(); } catch(EOFException)" que usaban antes todas
    // las clases DAO). Como cada registro tiene tamano fijo, la cantidad
    // total de registros se conoce de antemano (archivo.length() / tamRegistro),
    // asi que se puede recorrer con LongStream.range(...) en vez de leer
    // "a ciegas" hasta que truene el EOF.
    // =========================================================================

    /** Funcion que lee UN registro completo en la posicion actual del archivo. */
    @FunctionalInterface
    public interface LectorRegistro<T> {
        T leer() throws IOException;
    }

    // Envuelve una IOException dentro de una excepcion no verificada para
    // poder lanzarla desde dentro de un lambda de Stream (filter/mapToObj no
    // declaran "throws IOException"), y la desenvuelve de vuelta apenas
    // termina el stream. El resto del codigo sigue viendo IOException, como
    // siempre.
    private <R> R propagando(java.util.concurrent.Callable<R> accion) throws IOException {
        try {
            return accion.call();
        } catch (UncheckedIOException uioe) {
            throw uioe.getCause();
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lee TODOS los registros del archivo ya abierto (campo {@code archivo})
     * usando Stream API: genera un indice por cada registro existente,
     * posiciona el archivo (seek) y lo lee con el {@code lector} recibido.
     * Reemplaza el clasico bucle "while(true) { lista.add(leeX()); }".
     */
    protected <T> ArrayList<T> leerTodosConStream(LectorRegistro<T> lector) throws IOException {
        return propagando(() -> {
            long total = archivo.length() / tamRegistro;
            return LongStream.range(0, total)
                    .mapToObj(i -> {
                        try {
                            archivo.seek(i * tamRegistro);
                            return lector.leer();
                        } catch (IOException ioe) {
                            throw new UncheckedIOException(ioe);
                        }
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
        });
    }

    /**
     * Busca el PRIMER registro que cumpla {@code criterio}, leyendo con
     * Stream API. Gracias a que el Stream es perezoso (lazy), la busqueda
     * se corta apenas se encuentra coincidencia (findFirst), sin necesidad
     * de leer el archivo completo -- igual de eficiente que el bucle
     * "while(true) { if (...) return; }" que reemplaza. Retorna null si
     * ningun registro cumple el criterio.
     */
    protected <T> T buscarConStream(LectorRegistro<T> lector, Predicate<T> criterio) throws IOException {
        return propagando(() -> {
            long total = archivo.length() / tamRegistro;
            return LongStream.range(0, total)
                    .mapToObj(i -> {
                        try {
                            archivo.seek(i * tamRegistro);
                            return lector.leer();
                        } catch (IOException ioe) {
                            throw new UncheckedIOException(ioe);
                        }
                    })
                    .filter(criterio)
                    .findFirst()
                    .orElse(null);
        });
    }

    /**
     * Igual que {@link #buscarConStream}, pero en vez de devolver el
     * registro devuelve su INDICE (numero de registro, 0-based) dentro del
     * archivo, o -1 si ninguno cumple el criterio. Se usa en los metodos
     * actualizar() de las clases DAO para reposicionarse (seek) y
     * sobreescribir el registro encontrado.
     */
    protected <T> long indiceConStream(LectorRegistro<T> lector, Predicate<T> criterio) throws IOException {
        return propagando(() -> {
            long total = archivo.length() / tamRegistro;
            return LongStream.range(0, total)
                    .filter(i -> {
                        try {
                            archivo.seek(i * tamRegistro);
                            return criterio.test(lector.leer());
                        } catch (IOException ioe) {
                            throw new UncheckedIOException(ioe);
                        }
                    })
                    .findFirst()
                    .orElse(-1L);
        });
    }
}