package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.excepciones.FachadaException;
import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.negocio.Proveedor;
import edu.uce.programacion2.tienda.objetosServicio.Validaciones;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Diálogo para gestionar {@link Proveedor} con modos AGREGAR / VER / ACTUALIZAR / INACTIVAR.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DlgProveedor extends JDialog implements ActionListener {

    public enum Modo { AGREGAR, VER, ACTUALIZAR, INACTIVAR }

    private final IFachadaTienda fachada;
    private final Modo           modo;
    private boolean              guardado = false;

    private JTextField txtNombre;
    private JTextField txtRuc;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JTextField txtDireccion;

    private JLabel lblNombreStatus;
    private JLabel lblRucStatus;
    private JLabel lblTelefonoStatus;  // ← NUEVO
    private JLabel lblEmailStatus;

    private JButton btnAccion;
    private JButton btnRestaurar;
    private JButton btnCancelar;

    private Proveedor proveedorOriginal;

    // ── Constructores ─────────────────────────────────────────────────────────

    /** AGREGAR: sin objeto previo. */
    public DlgProveedor(Frame owner, IFachadaTienda fachada, Modo modo) {
        super(owner, tituloVentana(modo), true);
        this.fachada = fachada;
        this.modo    = modo;
        initComponents();
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(480, 420));
    }

    /** VER / ACTUALIZAR / INACTIVAR: con objeto existente. */
    public DlgProveedor(Frame owner, IFachadaTienda fachada, Modo modo, Proveedor p) {
        super(owner, tituloVentana(modo), true);
        this.fachada           = fachada;
        this.modo              = modo;
        this.proveedorOriginal = p;
        initComponents();
        poblarFormulario(p);
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(480, 420));
    }

    public boolean isGuardado() { return guardado; }

    // ── Construcción de componentes ───────────────────────────────────────────

    private void initComponents() {
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245));

        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 25, 15, 25),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                        "Datos del Proveedor",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("SansSerif", Font.BOLD, 13),
                        new Color(70, 130, 180)
                )
        ));
        pnl.setBackground(new Color(245, 245, 245));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.EAST;
        lc.insets = new Insets(7, 4, 7, 8);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(7, 0, 7, 4);

        int row = 0;

        // Nombre
        txtNombre = new JTextField(25);
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNombre.setBackground(Color.WHITE);
        txtNombre.setToolTipText("Nombre o razon social del proveedor");
        txtNombre.getDocument().addDocumentListener(docListener(() -> validarNombre()));
        lblNombreStatus = new JLabel(" ");
        lblNombreStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fila(pnl, "Nombre *", panelConStatus(txtNombre, lblNombreStatus), lc, fc, row++);

        // RUC
        txtRuc = new JTextField(15);
        txtRuc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtRuc.setBackground(Color.WHITE);
        txtRuc.setToolTipText("RUC de 13 digitos");
        txtRuc.getDocument().addDocumentListener(docListener(() -> validarRuc()));
        lblRucStatus = new JLabel(" ");
        lblRucStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fila(pnl, "RUC *", panelConStatus(txtRuc, lblRucStatus), lc, fc, row++);

        // Teléfono (con validación mejorada)
        txtTelefono = new JTextField(12);
        txtTelefono.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTelefono.setBackground(Color.WHITE);
        txtTelefono.setToolTipText("Numero de telefono o celular (7-10 digitos)");
        txtTelefono.getDocument().addDocumentListener(docListener(() -> validarTelefono()));
        lblTelefonoStatus = new JLabel(" ");
        lblTelefonoStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fila(pnl, "Telefono", panelConStatus(txtTelefono, lblTelefonoStatus), lc, fc, row++);

        // Email (con validación más estricta)
        txtEmail = new JTextField(25);
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtEmail.setBackground(Color.WHITE);
        txtEmail.setToolTipText("Correo electronico (ejemplo: usuario@dominio.com)");
        txtEmail.getDocument().addDocumentListener(docListener(() -> validarEmail()));
        lblEmailStatus = new JLabel(" ");
        lblEmailStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fila(pnl, "Email *", panelConStatus(txtEmail, lblEmailStatus), lc, fc, row++);

        // Dirección
        txtDireccion = new JTextField(30);
        txtDireccion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDireccion.setBackground(Color.WHITE);
        txtDireccion.setToolTipText("Direccion fisica del proveedor");
        fila(pnl, "Direccion", txtDireccion, lc, fc, row++);

        // Botones con texto en NEGRO para mejor visibilidad
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlBot.setBackground(new Color(245, 245, 245));
        pnlBot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        btnAccion    = crearBoton("+ Guardar", new Color(70, 130, 180), Color.BLACK);
        btnRestaurar = crearBoton("Restaurar", new Color(100, 100, 100), Color.BLACK);
        btnCancelar  = crearBoton("Cancelar",  new Color(180, 80, 80),  Color.BLACK);

        btnAccion.setEnabled(false);
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
        setEditable(editable);

        switch (modo) {
            case AGREGAR:
                btnAccion.setText("+ Guardar");
                btnAccion.setBackground(new Color(70, 130, 180));
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

    private void setEditable(boolean editable) {
        txtNombre.setEditable(editable);
        txtRuc.setEditable(editable);
        txtTelefono.setEditable(editable);
        txtEmail.setEditable(editable);
        txtDireccion.setEditable(editable);
        Color bg = editable ? Color.WHITE : new Color(235, 235, 235);
        txtNombre.setBackground(bg);
        txtRuc.setBackground(bg);
        txtTelefono.setBackground(bg);
        txtEmail.setBackground(bg);
        txtDireccion.setBackground(bg);
    }

    private void poblarFormulario(Proveedor p) {
        txtNombre.setText(p.getNombre());
        txtRuc.setText(p.getRuc());
        txtTelefono.setText(p.getTelefono());
        txtEmail.setText(p.getEmail());
        txtDireccion.setText(p.getDireccion());
        validarNombre();
        validarRuc();
        validarTelefono();
        validarEmail();
    }

    // ── Validaciones (mejoradas) ─────────────────────────────────────────────

    private void validarNombre() {
        String v = txtNombre.getText().trim();
        boolean ok = v.length() >= 3;
        lblNombreStatus.setText(ok ? "(OK)" : "(O)");
        lblNombreStatus.setForeground(ok ? new Color(0, 130, 0) : new Color(180, 0, 0));
        if (txtNombre.isEditable())
            txtNombre.setBackground(ok || v.isEmpty() ? Color.WHITE : new Color(255, 240, 240));
        actualizarBotonAccion();
    }

    private void validarRuc() {
        String v = txtRuc.getText().trim();
        boolean ok = Validaciones.validarRuc(v);
        lblRucStatus.setText(ok ? "(OK)" : "(O)");
        lblRucStatus.setForeground(ok ? new Color(0, 130, 0) : new Color(180, 0, 0));
        if (txtRuc.isEditable())
            txtRuc.setBackground(ok || v.isEmpty() ? Color.WHITE : new Color(255, 240, 240));
        actualizarBotonAccion();
    }

    private void validarTelefono() {
        String v = txtTelefono.getText().trim();
        boolean ok = v.isEmpty() || Validaciones.validarTelefono(v);
        lblTelefonoStatus.setText(ok ? (v.isEmpty() ? "(opcional)" : "(OK)") : "(O)");
        lblTelefonoStatus.setForeground(ok ? new Color(0, 130, 0) : new Color(180, 0, 0));
        if (txtTelefono.isEditable())
            txtTelefono.setBackground(ok || v.isEmpty() ? Color.WHITE : new Color(255, 240, 240));
        actualizarBotonAccion();
    }

    private void validarEmail() {
        String v = txtEmail.getText().trim();
        boolean ok = Validaciones.validarEmail(v);
        lblEmailStatus.setText(ok ? "(OK)" : "(O)");
        lblEmailStatus.setForeground(ok ? new Color(0, 130, 0) : new Color(180, 0, 0));
        if (txtEmail.isEditable())
            txtEmail.setBackground(ok || v.isEmpty() ? Color.WHITE : new Color(255, 240, 240));
        actualizarBotonAccion();
    }

    private void actualizarBotonAccion() {
        if (modo == Modo.VER || modo == Modo.INACTIVAR) return;

        String nombre = txtNombre.getText().trim();
        String ruc = txtRuc.getText().trim();
        String email = txtEmail.getText().trim();
        String telefono = txtTelefono.getText().trim();

        boolean valido = nombre.length() >= 3
                && Validaciones.validarRuc(ruc)
                && Validaciones.validarEmail(email)
                && (telefono.isEmpty() || Validaciones.validarTelefono(telefono));

        btnAccion.setEnabled(valido);
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
        Proveedor p = leerFormulario(-1);
        if (p == null) return;
        try {
            // El id se genera automáticamente en la capa de persistencia
            // (auto-incremental: máximo id existente + 1).
            fachada.agregarProveedor(p);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Proveedor agregado correctamente.\nNombre: " + p.getNombre(),
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionActualizar() {
        Proveedor p = leerFormulario(proveedorOriginal.getIdProveedor());
        if (p == null) return;
        try {
            fachada.actualizarProveedor(p);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Proveedor actualizado correctamente.",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionInactivar() {
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Desea inactivar al proveedor \"" + proveedorOriginal.getNombre() + "\"?\n" +
                        "El registro no se eliminará, solo dejará de estar disponible.",
                "Confirmar inactivación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            fachada.inactivarProveedor(proveedorOriginal.getIdProveedor());
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Proveedor inactivado correctamente.",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restaurar() {
        if (proveedorOriginal != null) {
            poblarFormulario(proveedorOriginal);
        } else {
            txtNombre.setText(""); txtRuc.setText(""); txtTelefono.setText("");
            txtEmail.setText(""); txtDireccion.setText("");
            lblNombreStatus.setText(" "); lblRucStatus.setText(" ");
            lblTelefonoStatus.setText(" "); lblEmailStatus.setText(" ");
            txtNombre.setBackground(Color.WHITE); txtRuc.setBackground(Color.WHITE);
            txtTelefono.setBackground(Color.WHITE); txtEmail.setBackground(Color.WHITE);
            btnAccion.setEnabled(false);
        }
        JOptionPane.showMessageDialog(this, "Datos restaurados.", "Informacion", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Lee los campos y crea un Proveedor. Devuelve null si hay error de validación. */
    private Proveedor leerFormulario(int id) {
        String nombre    = txtNombre.getText().trim();
        String ruc       = txtRuc.getText().trim();
        String telefono  = txtTelefono.getText().trim();
        String email     = txtEmail.getText().trim();
        String direccion = txtDireccion.getText().trim();

        // Validaciones con mensajes más descriptivos
        String error = Validaciones.validarRucConMensaje(ruc);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Validacion", JOptionPane.WARNING_MESSAGE);
            txtRuc.requestFocus();
            return null;
        }

        error = Validaciones.validarEmailConMensaje(email);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Validacion", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return null;
        }

        if (nombre.length() < 3) {
            JOptionPane.showMessageDialog(this,
                    "El nombre debe tener al menos 3 caracteres.", "Validacion", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return null;
        }

        if (!telefono.isEmpty()) {
            error = Validaciones.validarTelefonoConMensaje(telefono);
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Validacion", JOptionPane.WARNING_MESSAGE);
                txtTelefono.requestFocus();
                return null;
            }
        }

        return new Proveedor(id, nombre, ruc, telefono, email, direccion);
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────────

    private JPanel panelConStatus(JComponent campo, JLabel status) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setOpaque(false);
        p.add(campo,  BorderLayout.CENTER);
        p.add(status, BorderLayout.EAST);
        return p;
    }

    private javax.swing.event.DocumentListener docListener(Runnable accion) {
        return new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { accion.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { accion.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { accion.run(); }
        };
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
            case AGREGAR:    return "Agregar Proveedor";
            case VER:        return "Ver Proveedor";
            case ACTUALIZAR: return "Editar Proveedor";
            case INACTIVAR:  return "Inactivar Proveedor";
            default:         return "Proveedor";
        }
    }
}