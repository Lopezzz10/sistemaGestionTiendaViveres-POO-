package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.negocio.Factura;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PanelListaFacturas extends JPanel {

    // ===== MISMOS COLORES QUE DlgVenta y PanelFactura =====
    private static final Color COLOR_ACENTO      = new Color(70, 130, 180);    // Azul acento
    private static final Color COLOR_FONDO       = new Color(245, 245, 245);   // Gris claro
    private static final Color COLOR_LISTA_FONDO = new Color(248, 246, 242);   // Fondo lista
    private static final Color COLOR_ITEM_SEL    = new Color(200, 220, 240);   // Azul claro selección
    private static final Color COLOR_TEXTO       = new Color(50, 50, 50);      // Texto oscuro
    private static final Color COLOR_TEXTO_GRIS  = new Color(120, 120, 120);   // Texto gris
    private static final Color COLOR_BORDE       = new Color(200, 200, 200);   // Borde gris
    private static final Color COLOR_EMITIDA     = new Color(70, 140, 70);     // Verde emitida
    private static final Color COLOR_PAGADA      = new Color(50, 100, 180);    // Azul pagada
    private static final Color COLOR_ANULADA     = new Color(180, 60, 60);     // Rojo anulada

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    private final ArrayList<Factura>       facturas;
    private final PanelFactura             panelDetalle;
    private final DefaultListModel<String> modeloLista;
    private final JList<String>            jList;

    public PanelListaFacturas(ArrayList<Factura> facturas) {
        this.facturas = facturas;
        setLayout(new BorderLayout());
        setBackground(COLOR_FONDO);

        // ── Panel izquierdo ─────────────────────────────────────────────────
        JPanel panelIzq = new JPanel(new BorderLayout());
        panelIzq.setBackground(COLOR_LISTA_FONDO);
        panelIzq.setPreferredSize(new Dimension(220, 0));
        panelIzq.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_BORDE));

        // Título de la lista
        JLabel lblTituloLista = new JLabel("  Facturas");
        lblTituloLista.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloLista.setForeground(Color.WHITE);
        lblTituloLista.setBackground(COLOR_ACENTO);
        lblTituloLista.setOpaque(true);
        lblTituloLista.setBorder(new EmptyBorder(10, 10, 10, 10));
        lblTituloLista.setPreferredSize(new Dimension(220, 40));
        panelIzq.add(lblTituloLista, BorderLayout.NORTH);

        modeloLista = new DefaultListModel<>();
        jList = new JList<>(modeloLista);
        jList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jList.setBackground(COLOR_LISTA_FONDO);
        jList.setSelectionBackground(COLOR_ITEM_SEL);
        jList.setSelectionForeground(COLOR_TEXTO);
        jList.setFixedCellHeight(54);
        jList.setBorder(new EmptyBorder(4, 0, 4, 0));
        jList.setCellRenderer(new FacturaCellRenderer());

        cargarLista();

        JScrollPane scrollLista = new JScrollPane(jList);
        scrollLista.setBorder(null);
        scrollLista.getVerticalScrollBar().setUnitIncrement(10);
        scrollLista.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        panelIzq.add(scrollLista, BorderLayout.CENTER);

        JLabel lblConteo = new JLabel("  " + facturas.size() + " factura(s)");
        lblConteo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblConteo.setForeground(COLOR_TEXTO_GRIS);
        lblConteo.setBorder(new EmptyBorder(6, 6, 6, 6));
        lblConteo.setBackground(COLOR_LISTA_FONDO);
        lblConteo.setOpaque(true);
        panelIzq.add(lblConteo, BorderLayout.SOUTH);

        // ── Panel derecho ───────────────────────────────────────────────────
        panelDetalle = new PanelFactura();

        // ── Split ───────────────────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzq, panelDetalle);
        split.setDividerLocation(220);
        split.setDividerSize(1);
        split.setResizeWeight(0.0);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        // ── Selección ───────────────────────────────────────────────────────
        jList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = jList.getSelectedIndex();
                if (idx >= 0 && idx < facturas.size()) {
                    panelDetalle.setFactura(facturas.get(idx));
                }
            }
        });

        if (!facturas.isEmpty()) {
            jList.setSelectedIndex(0);
        }
    }

    private void cargarLista() {
        modeloLista.clear();
        for (Factura f : facturas) {
            String fecha = SDF.format(f.getFechaEmision());
            String total = String.format("$%.2f", f.getTotal());
            modeloLista.addElement(
                    f.getNumeroFactura() + "|" + fecha + "|" + total + "|" + f.getEstado().name());
        }
    }

    // ── Renderer ────────────────────────────────────────────────────────────

    private class FacturaCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            String[] p = value.toString().split("\\|");
            String nroCorto = extraerNroCorto(p.length > 0 ? p[0] : "-");
            String fecha    = p.length > 1 ? p[1] : "-";
            String total    = p.length > 2 ? p[2] : "-";
            String estado   = p.length > 3 ? p[3] : "-";

            JPanel card = new JPanel(new GridLayout(3, 1, 0, 2));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDE),
                    new EmptyBorder(6, 10, 6, 10)
            ));
            card.setBackground(isSelected ? COLOR_ITEM_SEL : COLOR_LISTA_FONDO);

            // Número de factura
            JLabel lNro = new JLabel("Factura #" + nroCorto);
            lNro.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lNro.setForeground(isSelected ? COLOR_TEXTO : COLOR_ACENTO);

            // Fecha y total
            JLabel lFecha = new JLabel(fecha + "   " + total);
            lFecha.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lFecha.setForeground(COLOR_TEXTO);

            // Estado
            JLabel lEstado = new JLabel(estado);
            lEstado.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lEstado.setForeground(estadoColor(estado));

            card.add(lNro);
            card.add(lFecha);
            card.add(lEstado);

            return card;
        }

        private String extraerNroCorto(String nro) {
            if (nro == null) return "---";
            String[] p = nro.split("-");
            if (p.length == 3) {
                try { return String.valueOf(Integer.parseInt(p[2])); }
                catch (NumberFormatException ignored) {}
            }
            return nro;
        }

        private Color estadoColor(String estado) {
            if (estado == null) return COLOR_TEXTO_GRIS;
            switch (estado.toUpperCase()) {
                case "EMITIDA":
                case "PAGADA":
                    return COLOR_EMITIDA;
                case "ANULADA":
                    return COLOR_ANULADA;
                default:
                    return COLOR_TEXTO_GRIS;
            }
        }
    }
}