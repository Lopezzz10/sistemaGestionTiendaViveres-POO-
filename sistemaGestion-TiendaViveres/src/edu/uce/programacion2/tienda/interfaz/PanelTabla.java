package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.control.Tabla;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel que contiene un {@link JTable} para mostrar los resultados
 * de las consultas del sistema de gestión de tienda.
 *
 * Recibe un objeto {@link Tabla} (generado por
 * {@link edu.uce.programacion2.tienda.control.Conversiones}) con título,
 * columnas y filas, y los presenta en una tabla no editable con
 * encabezados, scroll y formato estándar.
 *
 * Se integra en el {@link JScrollPane} central de la
 * {@link VentanaPrincipal} cada vez que el usuario ejecuta una consulta.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 * @see edu.uce.programacion2.tienda.control.Conversiones
 * @see VentanaPrincipal
 */
public class PanelTabla extends JPanel {

    private final JLabel     lblTitulo;
    private final JTable     jTable;
    private final JLabel     lblConteo;

    /**
     * Construye el panel vacío, listo para recibir datos mediante
     * {@link #setTabla(Tabla)}.
     */
    public PanelTabla() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblTitulo = new JLabel();
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(lblTitulo, BorderLayout.NORTH);

        jTable = new JTable();
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable.setRowHeight(22);
        jTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(jTable);
        add(scroll, BorderLayout.CENTER);

        lblConteo = new JLabel();
        lblConteo.setForeground(Color.GRAY);
        add(lblConteo, BorderLayout.SOUTH);
    }

    /**
     * Construye el panel y lo carga inmediatamente con los datos de
     * {@code tabla}.
     *
     * @param titulo texto del encabezado (si es {@code null}, se usa
     *               {@code tabla.getTitulo()})
     * @param tabla  objeto con título, columnas y filas de la consulta
     */
    public PanelTabla(String titulo, Tabla tabla) {
        this();
        if (titulo != null && tabla != null) {
            tabla.setTitulo(titulo);
        }
        setTabla(tabla);
    }

    /**
     * Actualiza el panel con los datos de {@code tabla}: refresca el
     * modelo de la tabla, el título y el contador de registros.
     * La tabla resultante es de solo lectura.
     *
     * @param tabla objeto con título, columnas y filas de la consulta
     */
    public void setTabla(Tabla tabla) {
        if (tabla == null) return;

        DefaultTableModel modelo = new DefaultTableModel(
                tabla.getFilas(), tabla.getColumnas()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTable.setModel(modelo);

        lblTitulo.setText(tabla.getTitulo());

        int numFilas = tabla.getFilas() != null ? tabla.getFilas().length : 0;
        lblConteo.setText(numFilas + " registro(s)");
    }
}