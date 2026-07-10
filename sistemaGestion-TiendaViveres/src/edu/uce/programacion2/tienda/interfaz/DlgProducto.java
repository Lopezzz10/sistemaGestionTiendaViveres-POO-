package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.excepciones.FachadaException;
import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.negocio.Categoria;
import edu.uce.programacion2.tienda.negocio.Producto;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DlgProducto extends JDialog implements ActionListener {

    public enum Modo { AGREGAR, VER, ACTUALIZAR, INACTIVAR }

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    private final IFachadaTienda fachada;
    private final Modo           modo;
    private boolean              guardado = false;

    // Campos comunes
    private JTextField           txtCodigo;
    private JTextField           txtNombre;
    private JComboBox<Categoria> cmbCategoria;
    private JTextField           txtPrecio;
    private JComboBox<String>    cmbTipo;

    // Campos de Perecible
    private JFormattedTextField  txtFechaVencimiento;
    private JTextField           txtTemperatura;
    private JPanel               pnlPerecible;

    // Campos de No Perecible
    private JTextField           txtPeso;
    private JTextField           txtMarca;
    private JPanel               pnlNoPerecible;

    // Botones
    private JButton btnAccion;
    private JButton btnRestaurar;
    private JButton btnCancelar;

    private Producto productoOriginal;

    // ── Constructores ─────────────────────────────────────────────────────────

    /** Para AGREGAR: solo se pasa el código generado. */
    public DlgProducto(Frame owner, IFachadaTienda fachada, Modo modo, String codigo) {
        super(owner, tituloVentana(modo), true);
        this.fachada = fachada;
        this.modo    = modo;
        initComponents();
        txtCodigo.setText(codigo);
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(520, 520));
    }

    /** Para VER, ACTUALIZAR, INACTIVAR: se pasa el producto completo. */
    public DlgProducto(Frame owner, IFachadaTienda fachada, Modo modo, Producto p) {
        super(owner, tituloVentana(modo), true);
        this.fachada          = fachada;
        this.modo             = modo;
        this.productoOriginal = p;
        initComponents();
        poblarFormulario(p);
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(520, 520));
    }

    public boolean isGuardado() { return guardado; }

    // ── Construcción de la UI ─────────────────────────────────────────────────

    private void initComponents() {
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245));

        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 25, 15, 25),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                        "Datos del Producto",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("SansSerif", Font.BOLD, 13),
                        new Color(70, 130, 180)
                )
        ));
        pnl.setBackground(new Color(245, 245, 245));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.EAST;
        lc.insets = new Insets(6, 4, 6, 8);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(6, 0, 6, 4);

        int row = 0;

        // Código
        txtCodigo = new JTextField(12);
        txtCodigo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCodigo.setBackground(new Color(240, 248, 255));
        fila(pnl, "Codigo", txtCodigo, lc, fc, row++);

        // Nombre
        txtNombre = new JTextField(25);
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fila(pnl, "Nombre *", txtNombre, lc, fc, row++);

        // Categoría
        cmbCategoria = new JComboBox<>();
        cmbCategoria.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbCategoria.setBackground(Color.WHITE);
        cargarCategorias();
        fila(pnl, "Categoria", cmbCategoria, lc, fc, row++);

        // Precio
        txtPrecio = new JTextField(10);
        txtPrecio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPrecio.setHorizontalAlignment(JTextField.RIGHT);
        fila(pnl, "Precio *", txtPrecio, lc, fc, row++);

        // Tipo — al cambiar muestra/oculta los paneles correspondientes
        cmbTipo = new JComboBox<>(new String[]{"Perecible", "No Perecible"});
        cmbTipo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbTipo.addActionListener(e -> actualizarPanelesTipo());
        fila(pnl, "Tipo *", cmbTipo, lc, fc, row++);

        // Panel Perecible
        pnlPerecible = new JPanel(new GridBagLayout());
        pnlPerecible.setOpaque(false);
        pnlPerecible.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 60, 60), 1, true),
                "Datos Perecible",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(200, 60, 60)
        ));

        GridBagConstraints lc2 = (GridBagConstraints) lc.clone();
        GridBagConstraints fc2 = (GridBagConstraints) fc.clone();

        txtTemperatura = new JTextField("4.0", 10);
        txtTemperatura.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTemperatura.setHorizontalAlignment(JTextField.RIGHT);
        fila(pnlPerecible, "Temperatura (C)", txtTemperatura, lc2, fc2, 0);

        try {
            MaskFormatter mask = new MaskFormatter("##/##/####");
            mask.setPlaceholderCharacter('_');
            txtFechaVencimiento = new JFormattedTextField(mask);
        } catch (ParseException e) {
            txtFechaVencimiento = new JFormattedTextField();
        }
        txtFechaVencimiento.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtFechaVencimiento.setToolTipText("Formato: dd/mm/aaaa");
        fila(pnlPerecible, "Fecha venc. *", txtFechaVencimiento, lc2, fc2, 1);

        // Panel No Perecible
        pnlNoPerecible = new JPanel(new GridBagLayout());
        pnlNoPerecible.setOpaque(false);
        pnlNoPerecible.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1, true),
                "Datos No Perecible",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(70, 130, 180)
        ));

        GridBagConstraints lc3 = (GridBagConstraints) lc.clone();
        GridBagConstraints fc3 = (GridBagConstraints) fc.clone();

        txtPeso = new JTextField("1.0", 10);
        txtPeso.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPeso.setHorizontalAlignment(JTextField.RIGHT);
        fila(pnlNoPerecible, "Peso (kg) *", txtPeso, lc3, fc3, 0);

        txtMarca = new JTextField(20);
        txtMarca.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fila(pnlNoPerecible, "Marca *", txtMarca, lc3, fc3, 1);

        JLabel notaDesc = new JLabel("Descuento 10% automatico si peso > 5 kg");
        notaDesc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        notaDesc.setForeground(new Color(70, 130, 180));
        GridBagConstraints nc = (GridBagConstraints) fc3.clone();
        nc.gridx = 0; nc.gridwidth = 2; nc.gridy = 2;
        pnlNoPerecible.add(notaDesc, nc);

        // Agregar paneles al formulario principal
        GridBagConstraints panelC = new GridBagConstraints();
        panelC.gridx = 0; panelC.gridwidth = 2;
        panelC.fill = GridBagConstraints.HORIZONTAL;
        panelC.insets = new Insets(8, 4, 4, 4);

        panelC.gridy = row++;
        pnl.add(pnlPerecible, panelC);

        panelC.gridy = row++;
        pnl.add(pnlNoPerecible, panelC);

        // Botones
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlBot.setBackground(new Color(245, 245, 245));
        pnlBot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        btnAccion    = crearBoton("Guardar",    new Color(70, 130, 180), Color.BLACK);
        btnRestaurar = crearBoton("Restaurar",  new Color(100, 100, 100), Color.BLACK);
        btnCancelar  = crearBoton("Cancelar",   new Color(180, 80, 80),  Color.BLACK);

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
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Estado inicial de paneles
        actualizarPanelesTipo();
    }

    /** Muestra u oculta los paneles según el tipo seleccionado. */
    private void actualizarPanelesTipo() {
        boolean esPerecible = "Perecible".equals(cmbTipo.getSelectedItem());
        pnlPerecible.setVisible(esPerecible);
        pnlNoPerecible.setVisible(!esPerecible);
        pack();
    }

    private void configModo() {
        boolean editable = (modo == Modo.AGREGAR || modo == Modo.ACTUALIZAR);
        txtCodigo.setEditable(false);
        txtNombre.setEditable(editable);
        cmbCategoria.setEnabled(editable);
        txtPrecio.setEditable(editable);
        cmbTipo.setEnabled(modo == Modo.AGREGAR); // tipo solo cambia al agregar
        txtTemperatura.setEditable(editable);
        txtFechaVencimiento.setEditable(editable);
        txtPeso.setEditable(editable);
        txtMarca.setEditable(editable);

        switch (modo) {
            case AGREGAR:
                btnAccion.setText("+ Guardar");
                btnAccion.setBackground(new Color(70, 130, 180));
                break;
            case VER:
                btnAccion.setText("Continuar");
                btnAccion.setBackground(new Color(100, 180, 100));
                btnRestaurar.setEnabled(false);
                break;
            case ACTUALIZAR:
                btnAccion.setText("Actualizar");
                btnAccion.setBackground(new Color(255, 165, 0));
                break;
            case INACTIVAR:
                btnAccion.setText("Inactivar");
                btnAccion.setBackground(new Color(200, 60, 60));
                btnRestaurar.setEnabled(false);
                break;
        }
        getRootPane().setDefaultButton(btnAccion);
    }

    private void poblarFormulario(Producto p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtPrecio.setText(String.format("%.2f", p.getPrecioUnitario()));
        cmbTipo.setSelectedItem(p.getTipo());

        if (p.getCategoria() != null) {
            for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                Categoria cat = cmbCategoria.getItemAt(i);
                if (cat != null && cat.getCveCategoria().equals(p.getCategoria().getCveCategoria())) {
                    cmbCategoria.setSelectedIndex(i);
                    break;
                }
            }
        }

        if ("Perecible".equals(p.getTipo())) {
            txtTemperatura.setText(String.format("%.1f", p.getTemperaturaAlmacenamiento()));
            if (p.getFechaVencimiento() != null)
                txtFechaVencimiento.setText(SDF.format(p.getFechaVencimiento()));
        } else {
            txtPeso.setText(String.format("%.2f", p.getPesoKg()));
            txtMarca.setText(p.getMarca());
        }

        actualizarPanelesTipo();
    }

    // ── Acciones ──────────────────────────────────────────────────────────────

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

    private void accionGuardar() {
        Producto p = leerFormulario();
        if (p == null) return;
        try {
            fachada.agregarProducto(p);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Producto agregado correctamente.\nCodigo: " + p.getCodigo(),
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionActualizar() {
        Producto p = leerFormulario();
        if (p == null) return;
        try {
            fachada.actualizarProducto(p);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Producto actualizado correctamente.",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionInactivar() {
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Desea inactivar el producto \"" + productoOriginal.getNombre() + "\"?\n" +
                        "El registro no se eliminará, solo dejará de estar disponible.",
                "Confirmar inactivación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            fachada.inactivarProducto(productoOriginal.getCodigo());
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Producto inactivado correctamente.",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restaurar() {
        if (productoOriginal != null) {
            poblarFormulario(productoOriginal);
        } else {
            txtNombre.setText("");
            txtPrecio.setText("");
            txtTemperatura.setText("4.0");
            txtFechaVencimiento.setText("");
            txtPeso.setText("1.0");
            txtMarca.setText("");
            if (cmbCategoria.getItemCount() > 0) cmbCategoria.setSelectedIndex(0);
            cmbTipo.setSelectedIndex(0);
            actualizarPanelesTipo();
        }
        JOptionPane.showMessageDialog(this, "Datos restaurados.",
                "Informacion", JOptionPane.INFORMATION_MESSAGE);
    }

    private Producto leerFormulario() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String tipo   = (String) cmbTipo.getSelectedItem();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return null;
        }

        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim());
            if (precio <= 0) {
                JOptionPane.showMessageDialog(this, "El precio debe ser mayor a 0.",
                        "Validacion", JOptionPane.WARNING_MESSAGE);
                txtPrecio.requestFocus();
                return null;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un numero valido.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            txtPrecio.requestFocus();
            return null;
        }

        Categoria cat = (Categoria) cmbCategoria.getSelectedItem();
        Producto p;

        if ("Perecible".equals(tipo)) {
            double temp;
            try {
                temp = Double.parseDouble(txtTemperatura.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "La temperatura debe ser un numero.",
                        "Validacion", JOptionPane.WARNING_MESSAGE);
                txtTemperatura.requestFocus();
                return null;
            }

            String fechaStr = txtFechaVencimiento.getText().trim();
            if (fechaStr.isEmpty() || fechaStr.equals("__/__/____")) {
                JOptionPane.showMessageDialog(this, "La fecha de vencimiento es obligatoria.",
                        "Validacion", JOptionPane.WARNING_MESSAGE);
                txtFechaVencimiento.requestFocus();
                return null;
            }
            Date fecha;
            try {
                fecha = SDF.parse(fechaStr);
                if (modo == Modo.AGREGAR && fecha.before(new Date())) {
                    JOptionPane.showMessageDialog(this,
                            "La fecha de vencimiento no puede ser anterior a hoy.",
                            "Validacion", JOptionPane.WARNING_MESSAGE);
                    txtFechaVencimiento.requestFocus();
                    return null;
                }
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Fecha invalida. Use dd/MM/yyyy.",
                        "Validacion", JOptionPane.WARNING_MESSAGE);
                txtFechaVencimiento.requestFocus();
                return null;
            }

            p = new Producto(codigo, nombre, cat, precio, fecha, temp);

        } else {
            double peso;
            try {
                peso = Double.parseDouble(txtPeso.getText().trim());
                if (peso <= 0) {
                    JOptionPane.showMessageDialog(this, "El peso debe ser mayor a 0.",
                            "Validacion", JOptionPane.WARNING_MESSAGE);
                    txtPeso.requestFocus();
                    return null;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "El peso debe ser un numero valido.",
                        "Validacion", JOptionPane.WARNING_MESSAGE);
                txtPeso.requestFocus();
                return null;
            }

            String marca = txtMarca.getText().trim();
            if (marca.isEmpty()) {
                JOptionPane.showMessageDialog(this, "La marca es obligatoria.",
                        "Validacion", JOptionPane.WARNING_MESSAGE);
                txtMarca.requestFocus();
                return null;
            }

            p = new Producto(codigo, nombre, cat, precio, peso, marca);
        }

        return p;
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private void cargarCategorias() {
        try {
            ArrayList<Categoria> lista = fachada.listarCategorias();
            cmbCategoria.removeAllItems();
            for (Categoria c : lista) cmbCategoria.addItem(c);
            if (cmbCategoria.getItemCount() == 0) cmbCategoria.addItem(null);
        } catch (FachadaException ignored) { }
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

    private static String tituloVentana(Modo modo) {
        switch (modo) {
            case AGREGAR:    return "Agregar Producto";
            case VER:        return "Ver Producto";
            case ACTUALIZAR: return "Editar Producto";
            case INACTIVAR:  return "Inactivar Producto";
            default:         return "Producto";
        }
    }
}