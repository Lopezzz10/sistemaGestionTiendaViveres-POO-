package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.negocio.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PanelFactura extends JPanel {

    // ===== COLORES ACTUALIZADOS A AZUL =====
    private static final Color COLOR_ACENTO     = new Color(70, 130, 180);    // Azul acento
    private static final Color COLOR_FONDO      = new Color(245, 245, 245);   // Fondo gris claro
    private static final Color COLOR_FONDO_DOC  = new Color(255, 255, 255);   // Blanco
    private static final Color COLOR_TEXTO      = new Color(50, 50, 50);      // Texto oscuro
    private static final Color COLOR_TEXTO_GRIS = new Color(120, 120, 120);   // Texto gris
    private static final Color COLOR_BORDE      = new Color(200, 200, 200);   // Borde gris
    private static final Color COLOR_TABLA_ALT  = new Color(240, 248, 255);   // Azul muy claro para filas alternas

    // ===== FUENTES =====
    private static final Font FONT_TITULO    = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SUBTITULO = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_NORMAL    = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_CURSIVA   = new Font("Segoe UI", Font.BOLD | Font.ITALIC, 20);

    private static final SimpleDateFormat SDF = new SimpleDateFormat(
            "dd 'de' MMMM 'de' yyyy", new java.util.Locale("es", "EC"));

    private final JLabel  lblNumero;
    private final JLabel  lblFecha;
    private final DefaultTableModel modeloProductos;
    private final JLabel  lblSubtotal;
    private final JLabel  lblIva;
    private final JLabel  lblTotal;
    private final JLabel  lblMetodoPago;

    public PanelFactura() {
        setLayout(new BorderLayout());
        setBackground(COLOR_FONDO);

        JPanel documento = new JPanel();
        documento.setLayout(new BoxLayout(documento, BoxLayout.Y_AXIS));
        documento.setBackground(COLOR_FONDO_DOC);
        documento.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1),
                new EmptyBorder(30, 40, 30, 40)
        ));
        documento.setMaximumSize(new Dimension(680, Integer.MAX_VALUE));

        // ===== BARRA SUPERIOR (AZUL) =====
        JPanel barraTop = new JPanel();
        barraTop.setBackground(COLOR_ACENTO);
        barraTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        barraTop.setPreferredSize(new Dimension(600, 8));
        documento.add(barraTop);
        documento.add(Box.createVerticalStrut(20));

        // ===== TÍTULO Y NÚMERO =====
        JPanel panelTituloNro = new JPanel(new BorderLayout());
        panelTituloNro.setBackground(COLOR_FONDO_DOC);
        panelTituloNro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel lblTitulo = new JLabel("FACTURA DE VENTA");
        lblTitulo.setFont(FONT_TITULO);
        lblTitulo.setForeground(COLOR_TEXTO);
        panelTituloNro.add(lblTitulo, BorderLayout.WEST);

        lblNumero = new JLabel("Factura  Número  000");
        lblNumero.setFont(FONT_SUBTITULO);
        lblNumero.setForeground(COLOR_ACENTO);  // ← AZUL
        lblNumero.setHorizontalAlignment(SwingConstants.RIGHT);
        panelTituloNro.add(lblNumero, BorderLayout.EAST);

        documento.add(panelTituloNro);
        documento.add(Box.createVerticalStrut(5));
        documento.add(separador());
        documento.add(Box.createVerticalStrut(10));

        // ===== FECHA =====
        lblFecha = new JLabel("Fecha: --");
        lblFecha.setFont(FONT_NORMAL);
        lblFecha.setForeground(COLOR_TEXTO_GRIS);
        lblFecha.setAlignmentX(Component.LEFT_ALIGNMENT);
        documento.add(lblFecha);
        documento.add(Box.createVerticalStrut(20));

        // ===== TABLA DE PRODUCTOS =====
        modeloProductos = new DefaultTableModel(
                new String[]{"Descripción", "Cantidad", "Valor Unidad", "Total"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tablaProductos = new JTable(modeloProductos);
        tablaProductos.setFont(FONT_SMALL);
        tablaProductos.setRowHeight(28);
        tablaProductos.setShowGrid(false);
        tablaProductos.setIntercellSpacing(new Dimension(0, 0));
        tablaProductos.setBackground(COLOR_FONDO_DOC);
        tablaProductos.setSelectionBackground(new Color(200, 220, 240));

        // ===== HEADER DE TABLA (Fondo claro, texto AZUL) =====
        JTableHeader header = tablaProductos.getTableHeader();
        header.setBackground(COLOR_FONDO_DOC);              // ← Fondo BLANCO
        header.setForeground(COLOR_ACENTO);                 // ← Texto AZUL
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 30));
        header.setReorderingAllowed(false);

        // ===== RENDERER DE TABLA =====
        tablaProductos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? COLOR_FONDO_DOC : COLOR_TABLA_ALT);
                }
                setForeground(COLOR_TEXTO);
                setFont(FONT_SMALL);
                if (col >= 1) setHorizontalAlignment(SwingConstants.CENTER);
                else          setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        // ===== LÍNEA SEPARADORA DEBAJO DEL HEADER (AZUL) =====
        tablaProductos.setGridColor(COLOR_ACENTO);  // ← Líneas AZULES
        tablaProductos.setShowHorizontalLines(true);
        tablaProductos.setShowVerticalLines(false);

        JScrollPane scrollTabla = new JScrollPane(tablaProductos);
        scrollTabla.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1));
        scrollTabla.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollTabla.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        documento.add(scrollTabla);
        documento.add(Box.createVerticalStrut(10));

        // ===== TOTALES =====
        JPanel panelTotales = new JPanel(new GridBagLayout());
        panelTotales.setBackground(COLOR_FONDO_DOC);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 8, 3, 8);

        g.gridx = 0; g.gridy = 0; g.anchor = GridBagConstraints.WEST;
        panelTotales.add(etiquetaTotalLabel("Subtotal (sin IVA):"), g);
        g.gridx = 1; g.anchor = GridBagConstraints.EAST;
        lblSubtotal = etiquetaTotalValor("$0.00");
        panelTotales.add(lblSubtotal, g);

        g.gridx = 0; g.gridy = 1; g.anchor = GridBagConstraints.WEST;
        // Nota: el IVA puede ser mixto (tarifas distintas por categoría de producto),
        // por eso no se etiqueta con un único porcentaje fijo (ver Factura.mostrar()).
        panelTotales.add(etiquetaTotalLabel("IVA:"), g);
        g.gridx = 1; g.anchor = GridBagConstraints.EAST;
        lblIva = etiquetaTotalValor("$0.00");
        panelTotales.add(lblIva, g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        panelTotales.add(new JSeparator(), g);
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;

        g.gridx = 0; g.gridy = 3; g.anchor = GridBagConstraints.WEST;
        JLabel lblTotalLabel = new JLabel("TOTAL:");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalLabel.setForeground(COLOR_ACENTO);  // ← AZUL
        panelTotales.add(lblTotalLabel, g);
        g.gridx = 1; g.anchor = GridBagConstraints.EAST;
        lblTotal = new JLabel("$0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(COLOR_ACENTO);  // ← AZUL
        panelTotales.add(lblTotal, g);

        JPanel wrapTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        wrapTotales.setBackground(COLOR_FONDO_DOC);
        wrapTotales.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        wrapTotales.add(panelTotales);
        wrapTotales.setAlignmentX(Component.LEFT_ALIGNMENT);
        documento.add(wrapTotales);
        documento.add(Box.createVerticalStrut(20));
        documento.add(separador());
        documento.add(Box.createVerticalStrut(15));

        // ===== PIE DE FACTURA =====
        JPanel panelPie = new JPanel(new BorderLayout(20, 0));
        panelPie.setBackground(COLOR_FONDO_DOC);
        panelPie.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel colPago = new JPanel(new GridLayout(2, 1, 0, 4));
        colPago.setBackground(COLOR_FONDO_DOC);

        JLabel tPago = new JLabel("Información de pago:");
        tPago.setFont(FONT_SUBTITULO);
        tPago.setForeground(COLOR_TEXTO);

        lblMetodoPago = new JLabel("Método: -");
        lblMetodoPago.setFont(FONT_SMALL);
        lblMetodoPago.setForeground(COLOR_TEXTO_GRIS);

        colPago.add(tPago);
        colPago.add(lblMetodoPago);

        JLabel lblGracias = new JLabel(
                "<html><div style='text-align:center; color: " + colorToHex(COLOR_ACENTO) + ";'>" +
                        "¡Gracias por<br>su compra!</div></html>");
        lblGracias.setFont(FONT_CURSIVA);
        lblGracias.setForeground(COLOR_ACENTO);  // ← AZUL
        lblGracias.setHorizontalAlignment(SwingConstants.RIGHT);

        panelPie.add(colPago,    BorderLayout.WEST);
        panelPie.add(lblGracias, BorderLayout.EAST);
        panelPie.setAlignmentX(Component.LEFT_ALIGNMENT);
        documento.add(panelPie);
        documento.add(Box.createVerticalStrut(10));

        // ===== BARRA INFERIOR (AZUL) =====
        JPanel barraBot = new JPanel();
        barraBot.setBackground(COLOR_ACENTO);
        barraBot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        barraBot.setPreferredSize(new Dimension(600, 8));
        documento.add(Box.createVerticalStrut(15));
        documento.add(barraBot);

        // ===== CENTRAR DOCUMENTO =====
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(COLOR_FONDO);
        wrapper.add(documento);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        mostrarVacio();
    }

    public void setFactura(Factura f) {
        if (f == null) { mostrarVacio(); return; }

        lblNumero.setText("Factura  Número  " + extraerNumeroCorto(f.getNumeroFactura()));
        lblFecha.setText("Fecha: " + SDF.format(f.getFechaEmision()));

        modeloProductos.setRowCount(0);
        ArrayList<DetalleFactura> detalles = f.getDetalles();
        if (!detalles.isEmpty()) {
            for (DetalleFactura d : detalles) {
                String nombre = d.getProducto() != null ? d.getProducto().getNombre() : "-";
                modeloProductos.addRow(new Object[]{
                        nombre,
                        d.getCantidad(),
                        String.format("$%.2f", d.getPrecioUnitarioSinIva()),
                        String.format("$%.2f", d.getSubtotal())
                });
            }
        } else if (f.getVenta() != null) {
            for (DetalleVenta d : f.getVenta().getDetalles()) {
                String nombre = d.getProducto() != null ? d.getProducto().getNombre() : "-";
                modeloProductos.addRow(new Object[]{
                        nombre,
                        d.getCantidad(),
                        String.format("$%.2f", d.getPrecioUnitario()),
                        String.format("$%.2f", d.calcularSubtotal())
                });
            }
        }

        lblSubtotal.setText(String.format("$%.2f", f.getSubtotal()));
        lblIva.setText(String.format("$%.2f", f.getMontoIva()));
        lblTotal.setText(String.format("$%.2f", f.getTotal()));

        String metodo = (f.getVenta() != null) ? f.getVenta().getMetodoPago() : "-";
        lblMetodoPago.setText("Método: " + metodo);
    }

    private void mostrarVacio() {
        lblNumero.setText("Factura  Número  ---");
        lblFecha.setText("Fecha: --");
        modeloProductos.setRowCount(0);
        lblSubtotal.setText("$0.00");
        lblIva.setText("$0.00");
        lblTotal.setText("$0.00");
        lblMetodoPago.setText("Método: -");
    }

    private String extraerNumeroCorto(String numeroFactura) {
        if (numeroFactura == null) return "---";
        String[] partes = numeroFactura.split("-");
        if (partes.length == 3) {
            try { return String.valueOf(Integer.parseInt(partes[2])); }
            catch (NumberFormatException ignored) {}
        }
        return numeroFactura;
    }

    private JLabel etiquetaInfo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONT_SMALL);
        l.setForeground(COLOR_TEXTO_GRIS);
        return l;
    }

    private JLabel etiquetaInfoDer(String texto) {
        JLabel l = etiquetaInfo(texto);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private JLabel etiquetaTotalLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONT_SMALL);
        l.setForeground(COLOR_TEXTO_GRIS);
        return l;
    }

    private JLabel etiquetaTotalValor(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONT_SMALL);
        l.setForeground(COLOR_TEXTO);
        return l;
    }

    private JSeparator separador() {
        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_BORDE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}