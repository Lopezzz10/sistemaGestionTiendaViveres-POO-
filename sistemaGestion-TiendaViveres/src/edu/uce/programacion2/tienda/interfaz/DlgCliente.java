package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.excepciones.FachadaException;
import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.negocio.Cliente;
import edu.uce.programacion2.tienda.objetosServicio.Validaciones;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Diálogo para gestionar {@link Cliente} con modos AGREGAR / VER / ACTUALIZAR / INACTIVAR.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DlgCliente extends JDialog implements ActionListener {

    public enum Modo { AGREGAR, VER, ACTUALIZAR, INACTIVAR }

    private final IFachadaTienda fachada;
    private final Modo           modo;
    private boolean              guardado = false;

    private JTextField txtNombre;
    private JTextField txtEmail;
    private JTextField txtContrasena;
    private JTextField txtDireccion;
    private JTextField txtTelefono;
    private JTextField txtCedula;  // ← NUEVO

    private JLabel lblNombreStatus;
    private JLabel lblEmailStatus;
    private JLabel lblContrasenaStatus;
    private JLabel lblCedulaStatus;    // ← NUEVO
    private JLabel lblTelefonoStatus;   // ← NUEVO

    private JButton btnAccion;
    private JButton btnRestaurar;
    private JButton btnCancelar;

    private Cliente clienteOriginal;

    // ── Constructores ─────────────────────────────────────────────────────────

    /** AGREGAR: sin objeto previo. */
    public DlgCliente(Frame owner, IFachadaTienda fachada, Modo modo) {
        super(owner, tituloVentana(modo), true);
        this.fachada = fachada;
        this.modo    = modo;
        initComponents();
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(480, 460));
    }

    /** VER / ACTUALIZAR / INACTIVAR: con objeto existente. */
    public DlgCliente(Frame owner, IFachadaTienda fachada, Modo modo, Cliente c) {
        super(owner, tituloVentana(modo), true);
        this.fachada         = fachada;
        this.modo            = modo;
        this.clienteOriginal = c;
        initComponents();
        poblarFormulario(c);
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(480, 460));
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
                        "Datos del Cliente",
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
        txtNombre.setToolTipText("Nombre completo del cliente");
        txtNombre.getDocument().addDocumentListener(docListener(() -> validarNombre()));
        lblNombreStatus = new JLabel(" ");
        lblNombreStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fila(pnl, "Nombre *", panelConStatus(txtNombre, lblNombreStatus), lc, fc, row++);

        // Email (validación mejorada)
        txtEmail = new JTextField(25);
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtEmail.setBackground(Color.WHITE);
        txtEmail.setToolTipText("ejemplo: cliente@dominio.com");
        txtEmail.getDocument().addDocumentListener(docListener(() -> validarEmail()));
        lblEmailStatus = new JLabel(" ");
        lblEmailStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fila(pnl, "Email *", panelConStatus(txtEmail, lblEmailStatus), lc, fc, row++);

        // Contraseña
        txtContrasena = new JTextField(15);
        txtContrasena.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtContrasena.setBackground(Color.WHITE);
        txtContrasena.setToolTipText("Minimo 6 caracteres");
        txtContrasena.getDocument().addDocumentListener(docListener(() -> validarContrasena()));
        lblContrasenaStatus = new JLabel(" ");
        lblContrasenaStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fila(pnl, "Contrasena *", panelConStatus(txtContrasena, lblContrasenaStatus), lc, fc, row++);

        // Cédula (NUEVO - con validación)
        txtCedula = new JTextField(10);
        txtCedula.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCedula.setBackground(Color.WHITE);
        txtCedula.setToolTipText("Cédula de identidad (10 dígitos)");
        txtCedula.getDocument().addDocumentListener(docListener(() -> validarCedula()));
        lblCedulaStatus = new JLabel(" ");
        lblCedulaStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fila(pnl, "Cedula *", panelConStatus(txtCedula, lblCedulaStatus), lc, fc, row++);

        // Teléfono (validación mejorada)
        txtTelefono = new JTextField(12);
        txtTelefono.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTelefono.setBackground(Color.WHITE);
        txtTelefono.setToolTipText("Numero de telefono o celular (7-10 digitos)");
        txtTelefono.getDocument().addDocumentListener(docListener(() -> validarTelefono()));
        lblTelefonoStatus = new JLabel(" ");
        lblTelefonoStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fila(pnl, "Telefono", panelConStatus(txtTelefono, lblTelefonoStatus), lc, fc, row++);

        // Dirección
        txtDireccion = new JTextField(30);
        txtDireccion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDireccion.setBackground(Color.WHITE);
        txtDireccion.setToolTipText("Direccion del cliente");
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
        txtEmail.setEditable(editable);
        txtContrasena.setEditable(editable);
        txtCedula.setEditable(editable);
        txtTelefono.setEditable(editable);
        txtDireccion.setEditable(editable);
        Color bg = editable ? Color.WHITE : new Color(235, 235, 235);
        txtNombre.setBackground(bg);
        txtEmail.setBackground(bg);
        txtContrasena.setBackground(bg);
        txtCedula.setBackground(bg);
        txtTelefono.setBackground(bg);
        txtDireccion.setBackground(bg);
    }

    private void poblarFormulario(Cliente c) {
        txtNombre.setText(c.getNombre());
        txtEmail.setText(c.getEmail());
        txtContrasena.setText(c.getContrasena());
        txtCedula.setText(c.getCedula());
        txtTelefono.setText(c.getTelefono());
        txtDireccion.setText(c.getDireccion());
        validarNombre();
        validarEmail();
        validarContrasena();
        validarCedula();
        validarTelefono();
    }

    // ── Validaciones ─────────────────────────────────────────────────────────

    private void validarNombre() {
        String v = txtNombre.getText().trim();
        boolean ok = v.length() >= 3;
        lblNombreStatus.setText(ok ? "(OK)" : "(O)");
        lblNombreStatus.setForeground(ok ? new Color(0, 130, 0) : new Color(180, 0, 0));
        if (txtNombre.isEditable())
            txtNombre.setBackground(ok || v.isEmpty() ? Color.WHITE : new Color(255, 240, 240));
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

    private void validarContrasena() {
        String v = txtContrasena.getText().trim();
        boolean ok = v.length() >= 6;
        lblContrasenaStatus.setText(ok ? "(OK)" : "(O)");
        lblContrasenaStatus.setForeground(ok ? new Color(0, 130, 0) : new Color(180, 0, 0));
        lblContrasenaStatus.setToolTipText("Minimo 6 caracteres");
        if (txtContrasena.isEditable())
            txtContrasena.setBackground(ok || v.isEmpty() ? Color.WHITE : new Color(255, 240, 240));
        actualizarBotonAccion();
    }

    private void validarCedula() {
        String v = txtCedula.getText().trim();
        boolean ok = Validaciones.validarCedula(v);
        lblCedulaStatus.setText(ok ? "(OK)" : "(O)");
        lblCedulaStatus.setForeground(ok ? new Color(0, 130, 0) : new Color(180, 0, 0));
        if (txtCedula.isEditable())
            txtCedula.setBackground(ok || v.isEmpty() ? Color.WHITE : new Color(255, 240, 240));
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

    private void actualizarBotonAccion() {
        if (modo == Modo.VER || modo == Modo.INACTIVAR) return;

        String nombre = txtNombre.getText().trim();
        String email = txtEmail.getText().trim();
        String contrasena = txtContrasena.getText().trim();
        String cedula = txtCedula.getText().trim();
        String telefono = txtTelefono.getText().trim();

        boolean valido = nombre.length() >= 3
                && Validaciones.validarEmail(email)
                && contrasena.length() >= 6
                && Validaciones.validarCedula(cedula)
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
        Cliente c = leerFormulario(-1);
        if (c == null) return;
        try {
            // El id se genera automáticamente en la capa de persistencia
            // (auto-incremental: máximo id existente + 1).
            fachada.agregarUsuario(c);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Cliente agregado correctamente.\n" +
                            "Nombre: " + c.getNombre() + "\n" +
                            "Cedula: " + c.getCedula(),
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionActualizar() {
        Cliente c = leerFormulario(clienteOriginal.getIdUsuario());
        if (c == null) return;
        try {
            fachada.actualizarUsuario(c);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Cliente actualizado correctamente.",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionInactivar() {
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Desea inactivar al cliente \"" + clienteOriginal.getNombre() + "\"?\n" +
                        "El registro no se eliminará, solo dejará de estar disponible.",
                "Confirmar inactivación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            fachada.inactivarUsuario(clienteOriginal.getIdUsuario());
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Cliente inactivado correctamente.",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restaurar() {
        if (clienteOriginal != null) {
            poblarFormulario(clienteOriginal);
        } else {
            txtNombre.setText(""); txtEmail.setText("");
            txtContrasena.setText(""); txtCedula.setText("");
            txtTelefono.setText(""); txtDireccion.setText("");
            lblNombreStatus.setText(" "); lblEmailStatus.setText(" ");
            lblContrasenaStatus.setText(" "); lblCedulaStatus.setText(" ");
            lblTelefonoStatus.setText(" ");
            txtNombre.setBackground(Color.WHITE); txtEmail.setBackground(Color.WHITE);
            txtContrasena.setBackground(Color.WHITE); txtCedula.setBackground(Color.WHITE);
            txtTelefono.setBackground(Color.WHITE); txtDireccion.setBackground(Color.WHITE);
            btnAccion.setEnabled(false);
        }
        JOptionPane.showMessageDialog(this, "Datos restaurados.", "Informacion", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Lee todos los campos y construye un Cliente.
     * @param id  id del cliente; pasar -1 para que se asigne después.
     * @return el objeto creado, o null si hay error de validación.
     */
    private Cliente leerFormulario(int id) {
        String nombre = txtNombre.getText().trim();
        String email = txtEmail.getText().trim();
        String contrasena = txtContrasena.getText().trim();
        String cedula = txtCedula.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String direccion = txtDireccion.getText().trim();

        // Validaciones con mensajes descriptivos
        if (nombre.length() < 3) {
            JOptionPane.showMessageDialog(this,
                    "El nombre debe tener al menos 3 caracteres.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return null;
        }

        String errorEmail = Validaciones.validarEmailConMensaje(email);
        if (errorEmail != null) {
            JOptionPane.showMessageDialog(this, errorEmail, "Validacion", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return null;
        }

        if (contrasena.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    "La contrasena debe tener al menos 6 caracteres.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            txtContrasena.requestFocus();
            return null;
        }

        String errorCedula = Validaciones.validarCedulaConMensaje(cedula);
        if (errorCedula != null) {
            JOptionPane.showMessageDialog(this, errorCedula, "Validacion", JOptionPane.WARNING_MESSAGE);
            txtCedula.requestFocus();
            return null;
        }

        if (!telefono.isEmpty()) {
            String errorTelefono = Validaciones.validarTelefonoConMensaje(telefono);
            if (errorTelefono != null) {
                JOptionPane.showMessageDialog(this, errorTelefono, "Validacion", JOptionPane.WARNING_MESSAGE);
                txtTelefono.requestFocus();
                return null;
            }
        }

        Cliente cliente = new Cliente(id, nombre, email, contrasena, direccion, telefono);
        cliente.setCedula(cedula);
        return cliente;
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
            case AGREGAR:    return "Agregar Cliente";
            case VER:        return "Ver Cliente";
            case ACTUALIZAR: return "Editar Cliente";
            case INACTIVAR:  return "Inactivar Cliente";
            default:         return "Cliente";
        }
    }
}