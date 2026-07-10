package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.excepciones.FachadaException;
import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.negocio.Inventario;
import edu.uce.programacion2.tienda.negocio.Producto;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Diálogo para gestionar {@link Inventario} con modos AGREGAR / VER / ACTUALIZAR / INACTIVAR.
 *
 * En AGREGAR: combo de productos disponibles + campo cantidad.
 * En VER / ACTUALIZAR / INACTIVAR: JLabel fijo con el producto (no se cambia),
 * campo cantidad editable solo en ACTUALIZAR.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DlgInventario extends JDialog implements ActionListener {

    public enum Modo { AGREGAR, VER, ACTUALIZAR, INACTIVAR }

    private final IFachadaTienda fachada;
    private final Modo           modo;
    private boolean              guardado = false;

    // Panel de producto: en AGREGAR muestra combo, en otros modos muestra JLabel fijo
    private JComboBox<String> cmbProducto;
    private JLabel            lblProductoFijo;
    private JPanel            pnlProducto;

    private JTextField txtCantidad;
    private JLabel     lblCantidadStatus;

    private JButton btnAccion;
    private JButton btnRestaurar;
    private JButton btnCancelar;

    private ArrayList<Producto> catalogo;
    private Inventario          inventarioOriginal;

    // ── Constructores ─────────────────────────────────────────────────────────

    /** AGREGAR: sin objeto previo. */
    public DlgInventario(Frame owner, IFachadaTienda fachada, Modo modo) {
        super(owner, tituloVentana(modo), true);
        this.fachada = fachada;
        this.modo    = modo;
        initComponents();
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(480, 300));
    }

    /** VER / ACTUALIZAR / INACTIVAR: con objeto existente. */
    public DlgInventario(Frame owner, IFachadaTienda fachada, Modo modo, Inventario inv) {
        super(owner, tituloVentana(modo), true);
        this.fachada             = fachada;
        this.modo                = modo;
        this.inventarioOriginal  = inv;
        initComponents();
        poblarFormulario(inv);
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(480, 300));
    }

    public boolean isGuardado() { return guardado; }

    // ── Construcción de componentes ───────────────────────────────────────────

    private void initComponents() {
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245));

        // Cargar catálogo de productos disponibles (solo para AGREGAR)
        catalogo = new ArrayList<>();
        if (modo == Modo.AGREGAR) {
            try {
                ArrayList<Producto> todos = fachada.listarCatalogo();
                ArrayList<String> codigosConInv = new ArrayList<>();
                for (Inventario i : fachada.listarInventarios())
                    if (i.getProducto() != null)
                        codigosConInv.add(i.getProducto().getCodigo());
                for (Producto p : todos)
                    if (!codigosConInv.contains(p.getCodigo()))
                        catalogo.add(p);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error al cargar el catalogo: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 25, 15, 25),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                        "Datos del Inventario",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("SansSerif", Font.BOLD, 13),
                        new Color(70, 130, 180)
                )
        ));
        pnl.setBackground(new Color(245, 245, 245));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.EAST;
        lc.insets = new Insets(8, 4, 8, 8);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(8, 0, 8, 4);

        int row = 0;

        // ── Fila Producto ────────────────────────────────────────────────────
        pnlProducto = new JPanel(new BorderLayout());
        pnlProducto.setOpaque(false);

        if (modo == Modo.AGREGAR) {
            // Combo de productos disponibles
            String[] items;
            if (catalogo.isEmpty()) {
                items = new String[]{"-- Sin productos disponibles --"};
            } else {
                items = new String[catalogo.size()];
                for (int i = 0; i < catalogo.size(); i++)
                    items[i] = catalogo.get(i).getCodigo() + " — " + catalogo.get(i).getNombre();
            }
            cmbProducto = new JComboBox<>(items);
            cmbProducto.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cmbProducto.setBackground(Color.WHITE);
            pnlProducto.add(cmbProducto, BorderLayout.CENTER);
        } else {
            // JLabel fijo: el producto de un inventario no se cambia
            lblProductoFijo = new JLabel();
            lblProductoFijo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblProductoFijo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(4, 6, 4, 6)
            ));
            lblProductoFijo.setBackground(new Color(235, 235, 235));
            lblProductoFijo.setOpaque(true);
            pnlProducto.add(lblProductoFijo, BorderLayout.CENTER);
        }
        fila(pnl, "Producto", pnlProducto, lc, fc, row++);

        // ── Fila Cantidad ────────────────────────────────────────────────────
        txtCantidad = new JTextField(8);
        txtCantidad.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCantidad.setBackground(Color.WHITE);
        txtCantidad.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { validarCantidad(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { validarCantidad(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validarCantidad(); }
        });

        lblCantidadStatus = new JLabel(" ");
        lblCantidadStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JPanel cantPanel = new JPanel(new BorderLayout(5, 0));
        cantPanel.setOpaque(false);
        cantPanel.add(txtCantidad,       BorderLayout.CENTER);
        cantPanel.add(lblCantidadStatus, BorderLayout.EAST);
        fila(pnl, "Cantidad *", cantPanel, lc, fc, row++);

        // ── Botones con texto en NEGRO para mejor visibilidad ──────────────
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlBot.setBackground(new Color(245, 245, 245));
        pnlBot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        btnAccion    = crearBoton("+ Guardar", new Color(70, 130, 180), Color.BLACK);
        btnRestaurar = crearBoton("Restaurar", new Color(100, 100, 100), Color.BLACK);
        btnCancelar  = crearBoton("Cancelar",  new Color(180, 80, 80),  Color.BLACK);

        btnAccion.addActionListener(this);
        btnRestaurar.addActionListener(this);
        btnCancelar.addActionListener(this);

        pnlBot.add(btnAccion);
        pnlBot.add(btnRestaurar);
        pnlBot.add(btnCancelar);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pnl,    BorderLayout.CENTER);
        getContentPane().add(pnlBot, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    // ── Configuración por modo ────────────────────────────────────────────────

    private void configModo() {
        boolean editable = (modo == Modo.AGREGAR || modo == Modo.ACTUALIZAR);
        txtCantidad.setEditable(editable);
        if (!editable) txtCantidad.setBackground(new Color(235, 235, 235));

        switch (modo) {
            case AGREGAR:
                btnAccion.setText("+ Guardar");
                btnAccion.setBackground(new Color(70, 130, 180));
                btnAccion.setEnabled(false); // se habilita al validar
                break;
            case VER:
                btnAccion.setText("Cerrar");
                btnAccion.setBackground(new Color(100, 180, 100));
                btnAccion.setEnabled(true);
                btnRestaurar.setEnabled(false);
                break;
            case ACTUALIZAR:
                btnAccion.setText("Actualizar");
                btnAccion.setBackground(new Color(255, 165, 0));
                break;
            case INACTIVAR:
                btnAccion.setText("Inactivar");
                btnAccion.setBackground(new Color(200, 60, 60));
                btnAccion.setEnabled(true);
                btnRestaurar.setEnabled(false);
                break;
        }

        getRootPane().setDefaultButton(btnAccion);
    }

    private void poblarFormulario(Inventario inv) {
        // Producto fijo como texto
        if (inv.getProducto() != null)
            lblProductoFijo.setText(inv.getProducto().getCodigo() + " — " + inv.getProducto().getNombre());
        else
            lblProductoFijo.setText("(sin producto)");

        txtCantidad.setText(String.valueOf(inv.getCantidadDisponible()));
    }

    // ── Validación ────────────────────────────────────────────────────────────

    private void validarCantidad() {
        if (modo == Modo.VER || modo == Modo.INACTIVAR) return;
        String txt = txtCantidad.getText().trim();
        boolean valido = false;
        try {
            int v = Integer.parseInt(txt);
            if (v >= 0) {
                valido = true;
                lblCantidadStatus.setText("(OK)");
                lblCantidadStatus.setForeground(new Color(0, 130, 0));
                txtCantidad.setBackground(Color.WHITE);
            } else {
                lblCantidadStatus.setText("(O)");
                lblCantidadStatus.setForeground(new Color(180, 0, 0));
                txtCantidad.setBackground(new Color(255, 240, 240));
            }
        } catch (NumberFormatException ex) {
            lblCantidadStatus.setText("(O)");
            lblCantidadStatus.setForeground(new Color(180, 0, 0));
            txtCantidad.setBackground(txt.isEmpty() ? Color.WHITE : new Color(255, 240, 240));
        }
        boolean productoOk = (modo == Modo.AGREGAR) ? !catalogo.isEmpty() : true;
        btnAccion.setEnabled(valido && productoOk);
    }

    // ── ActionListener ────────────────────────────────────────────────────────

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCancelar) {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Esta seguro de cancelar?", "Confirmar",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) dispose();
            return;
        }
        if (e.getSource() == btnRestaurar) { restaurar(); return; }

        switch (modo) {
            case AGREGAR:    accionGuardar();    break;
            case VER:        dispose();          break;
            case ACTUALIZAR: accionActualizar(); break;
            case INACTIVAR:  accionInactivar();  break;
        }
    }

    // ── Acciones ──────────────────────────────────────────────────────────────

    private void accionGuardar() {
        if (catalogo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay productos disponibles para inventariar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int cantidad = leerCantidad();
        if (cantidad < 0) return;

        Producto producto = catalogo.get(cmbProducto.getSelectedIndex());
        try {
            // El id se genera automáticamente en la capa de persistencia
            // (auto-incremental: máximo id existente + 1); se pasa 0 como
            // valor provisional, que la fachada sobrescribe al agregar.
            Inventario inv = new Inventario(0, producto, cantidad);
            fachada.agregarInventario(inv);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Inventario agregado correctamente.\n" +
                            "Producto: " + producto.getNombre() + "\n" +
                            "Cantidad: " + cantidad,
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionActualizar() {
        int cantidad = leerCantidad();
        if (cantidad < 0) return;
        try {
            inventarioOriginal.setCantidadDisponible(cantidad);
            fachada.actualizarInventario(inventarioOriginal);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Inventario actualizado correctamente.\nCantidad: " + cantidad,
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionInactivar() {
        String nombre = inventarioOriginal.getProducto() != null
                ? inventarioOriginal.getProducto().getNombre() : "ID=" + inventarioOriginal.getIdInventario();
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Desea inactivar el inventario del producto \"" + nombre + "\"?\n" +
                        "El registro no se eliminará, solo dejará de estar disponible.",
                "Confirmar inactivación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            fachada.inactivarInventario(inventarioOriginal.getIdInventario());
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Inventario inactivado correctamente.",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restaurar() {
        if (inventarioOriginal != null) {
            txtCantidad.setText(String.valueOf(inventarioOriginal.getCantidadDisponible()));
        } else {
            txtCantidad.setText("");
            if (cmbProducto != null && !catalogo.isEmpty()) cmbProducto.setSelectedIndex(0);
        }
        JOptionPane.showMessageDialog(this, "Datos restaurados.", "Informacion", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Lee y valida el campo cantidad. Devuelve -1 si es inválido. */
    private int leerCantidad() {
        try {
            int v = Integer.parseInt(txtCantidad.getText().trim());
            if (v >= 0) return v;
        } catch (NumberFormatException ignored) {}
        JOptionPane.showMessageDialog(this,
                "La cantidad debe ser un numero entero mayor o igual a 0.",
                "Validacion", JOptionPane.WARNING_MESSAGE);
        txtCantidad.requestFocus();
        return -1;
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────────

    private JButton crearBoton(String texto, Color fondo, Color textoColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(fondo);
        btn.setForeground(textoColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fondo.darker(), 1),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(fondo.brighter()); }
            public void mouseExited (java.awt.event.MouseEvent evt) { btn.setBackground(fondo); }
        });
        return btn;
    }

    private void fila(JPanel p, String label, JComponent comp,
                      GridBagConstraints lc, GridBagConstraints fc, int row) {
        lc.gridy = fc.gridy = row;
        lc.gridx = 0; fc.gridx = 1;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(50, 50, 50));
        p.add(lbl, lc);
        p.add(comp, fc);
    }

    private static String tituloVentana(Modo modo) {
        switch (modo) {
            case AGREGAR:    return "Agregar Inventario";
            case VER:        return "Ver Inventario";
            case ACTUALIZAR: return "Editar Inventario";
            case INACTIVAR:  return "Inactivar Inventario";
            default:         return "Inventario";
        }
    }
}