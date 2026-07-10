package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.excepciones.FachadaException;
import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.negocio.Administrador;
import edu.uce.programacion2.tienda.negocio.Cajero;
import edu.uce.programacion2.tienda.negocio.Rol;
import edu.uce.programacion2.tienda.negocio.Usuario;
import edu.uce.programacion2.tienda.objetosServicio.Validaciones;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Diálogo para gestionar {@link Usuario} con modos AGREGAR / VER / ACTUALIZAR / INACTIVAR.
 *
 * Campos comunes: tipo, nombre, email, contraseña.
 * Campo dinámico según tipo:
 *   - Administrador → turno (combo: MAÑANA / TARDE / NOCHE)
 *   - Cajero        → caja asignada (campo numérico)
 * Campo opcional: Rol (seleccionable desde los roles creados)
 *
 * En VER/ACTUALIZAR/INACTIVAR el tipo queda fijo; solo se usa CardLayout
 * para mostrar el campo específico correcto.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DlgUsuario extends JDialog implements ActionListener {

    public enum Modo { AGREGAR, VER, ACTUALIZAR, INACTIVAR }

    private final IFachadaTienda fachada;
    private final Modo           modo;
    private boolean              guardado = false;

    // Campos comunes
    private JComboBox<String> cmbTipoUsuario;
    private JTextField        txtNombre;
    private JTextField        txtEmail;
    private JPasswordField    txtContrasena;

    // Status labels
    private JLabel lblNombreStatus;
    private JLabel lblEmailStatus;
    private JLabel lblContrasenaStatus;
    private JLabel lblEspecificoStatus;

    // Panel dinámico y sus controles
    private JPanel            pnlEspecifico;
    private JLabel            lblEspecifico;
    private JComboBox<String> cmbTurno;
    private JTextField        txtCajaAsignada;
    private CardLayout        cardLayout;

    // Rol
    private JComboBox<String> cmbRol;
    private JLabel            lblRolStatus;

    private JButton btnAccion;
    private JButton btnRestaurar;
    private JButton btnCancelar;

    private Usuario usuarioOriginal;
    private ArrayList<Rol> rolesDisponibles = new ArrayList<>();

    private static final String CARD_ADMIN  = "ADMINISTRADOR";
    private static final String CARD_CAJERO = "CAJERO";

    // ── Constructores ─────────────────────────────────────────────────────────

    /** AGREGAR: sin objeto previo. */
    public DlgUsuario(Frame owner, IFachadaTienda fachada, Modo modo) {
        super(owner, tituloVentana(modo), true);
        this.fachada = fachada;
        this.modo    = modo;
        cargarRoles();
        initComponents();
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(480, 480));
    }

    /** VER / ACTUALIZAR / INACTIVAR: con objeto existente. */
    public DlgUsuario(Frame owner, IFachadaTienda fachada, Modo modo, Usuario u) {
        super(owner, tituloVentana(modo), true);
        this.fachada          = fachada;
        this.modo             = modo;
        this.usuarioOriginal  = u;
        cargarRoles();
        initComponents();
        poblarFormulario(u);
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(480, 480));
    }

    public boolean isGuardado() { return guardado; }

    // ── Carga de roles ────────────────────────────────────────────────────────

    private void cargarRoles() {
        rolesDisponibles.clear();
        try {
            rolesDisponibles.addAll(fachada.listarRolesActivos());
        } catch (Exception e) {
            // Si no se pueden cargar roles, continuar con lista vacía
        }
    }

    // ── Construcción de componentes ───────────────────────────────────────────

    private void initComponents() {
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245));

        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 25, 15, 25),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                        "Datos del Usuario",
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

        // Tipo de usuario
        cmbTipoUsuario = new JComboBox<>(new String[]{"Administrador", "Cajero"});
        cmbTipoUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbTipoUsuario.setBackground(Color.WHITE);
        cmbTipoUsuario.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) cambiarCampoEspecifico();
        });
        fila(pnl, "Tipo *", cmbTipoUsuario, lc, fc, row++);

        // Nombre
        txtNombre = new JTextField(25);
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNombre.setBackground(Color.WHITE);
        txtNombre.getDocument().addDocumentListener(docListener(() -> validarNombre()));
        lblNombreStatus = new JLabel(" ");
        lblNombreStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fila(pnl, "Nombre *", panelConStatus(txtNombre, lblNombreStatus), lc, fc, row++);

        // Email (con validación mejorada)
        txtEmail = new JTextField(25);
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtEmail.setBackground(Color.WHITE);
        txtEmail.setToolTipText("ejemplo: usuario@dominio.com");
        txtEmail.getDocument().addDocumentListener(docListener(() -> validarEmail()));
        lblEmailStatus = new JLabel(" ");
        lblEmailStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fila(pnl, "Email *", panelConStatus(txtEmail, lblEmailStatus), lc, fc, row++);

        // Contraseña
        txtContrasena = new JPasswordField(20);
        txtContrasena.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtContrasena.setBackground(Color.WHITE);
        txtContrasena.getDocument().addDocumentListener(docListener(() -> validarContrasena()));
        lblContrasenaStatus = new JLabel(" ");
        lblContrasenaStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fila(pnl, "Contrasena *", panelConStatus(txtContrasena, lblContrasenaStatus), lc, fc, row++);

        // Rol (NUEVO)
        cmbRol = new JComboBox<>();
        cmbRol.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbRol.setBackground(Color.WHITE);
        cmbRol.setToolTipText("Seleccione un rol para permisos personalizados");
        actualizarComboRoles();
        lblRolStatus = new JLabel(" ");
        lblRolStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fila(pnl, "Rol", panelConStatus(cmbRol, lblRolStatus), lc, fc, row++);

        // Campo específico dinámico con CardLayout
        cardLayout    = new CardLayout();
        pnlEspecifico = new JPanel(cardLayout);
        pnlEspecifico.setOpaque(false);

        // Card Administrador: combo turno
        cmbTurno = new JComboBox<>(new String[]{"MANANA", "TARDE", "NOCHE"});
        cmbTurno.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbTurno.setBackground(Color.WHITE);
        pnlEspecifico.add(cmbTurno, CARD_ADMIN);

        // Card Cajero: campo numérico + status
        lblEspecificoStatus = new JLabel(" ");
        lblEspecificoStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtCajaAsignada = new JTextField(6);
        txtCajaAsignada.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCajaAsignada.setBackground(Color.WHITE);
        txtCajaAsignada.getDocument().addDocumentListener(docListener(() -> validarCaja()));
        pnlEspecifico.add(panelConStatus(txtCajaAsignada, lblEspecificoStatus), CARD_CAJERO);

        // Etiqueta dinámica del campo específico
        lblEspecifico = new JLabel("Turno *:");
        lblEspecifico.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEspecifico.setForeground(new Color(50, 50, 50));

        lc.gridy = fc.gridy = row++;
        lc.gridx = 0; fc.gridx = 1;
        pnl.add(lblEspecifico,  lc);
        pnl.add(pnlEspecifico, fc);

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

        cardLayout.show(pnlEspecifico, CARD_ADMIN);
    }

    // ── Actualizar combo de roles ────────────────────────────────────────────

    private void actualizarComboRoles() {
        cmbRol.removeAllItems();
        cmbRol.addItem("(Sin rol)");
        for (Rol r : rolesDisponibles) {
            if (r.isActivo()) {
                cmbRol.addItem(r.getNombreCargo());
            }
        }
    }

    // ── Configuración por modo ────────────────────────────────────────────────

    private void configModo() {
        boolean editable = (modo == Modo.AGREGAR || modo == Modo.ACTUALIZAR);

        cmbTipoUsuario.setEnabled(modo == Modo.AGREGAR);
        cmbRol.setEnabled(editable);
        setEditableComunes(editable);

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
                cmbTurno.setEnabled(false);
                txtCajaAsignada.setEditable(false);
                txtCajaAsignada.setBackground(new Color(235, 235, 235));
                cmbRol.setEnabled(false);
                break;
            case ACTUALIZAR:
                btnAccion.setText("Actualizar");
                btnAccion.setBackground(new Color(255, 165, 0));
                cmbTurno.setEnabled(true);
                txtCajaAsignada.setEditable(true);
                txtCajaAsignada.setBackground(Color.WHITE);
                cmbRol.setEnabled(true);
                break;
            case INACTIVAR:
                btnAccion.setText("Inactivar");
                btnAccion.setBackground(new Color(200, 60, 60));
                btnAccion.setEnabled(true);
                btnRestaurar.setEnabled(false);
                cmbTurno.setEnabled(false);
                txtCajaAsignada.setEditable(false);
                txtCajaAsignada.setBackground(new Color(235, 235, 235));
                cmbRol.setEnabled(false);
                break;
        }

        getRootPane().setDefaultButton(btnAccion);
    }

    private void setEditableComunes(boolean editable) {
        txtNombre.setEditable(editable);
        txtEmail.setEditable(editable);
        txtContrasena.setEditable(editable);
        Color bg = editable ? Color.WHITE : new Color(235, 235, 235);
        txtNombre.setBackground(bg);
        txtEmail.setBackground(bg);
        txtContrasena.setBackground(bg);
    }

    // ── Poblar formulario ─────────────────────────────────────────────────────

    private void poblarFormulario(Usuario u) {
        txtNombre.setText(u.getNombre());
        txtEmail.setText(u.getEmail());
        txtContrasena.setText(u.getContrasena());

        // Seleccionar el rol del usuario
        if (u.getRol() != null) {
            for (int i = 0; i < cmbRol.getItemCount(); i++) {
                String item = cmbRol.getItemAt(i);
                if (item != null && item.equals(u.getRol().getNombreCargo())) {
                    cmbRol.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            cmbRol.setSelectedIndex(0); // "(Sin rol)"
        }

        if (u instanceof Administrador) {
            cmbTipoUsuario.setSelectedIndex(0);
            cardLayout.show(pnlEspecifico, CARD_ADMIN);
            lblEspecifico.setText("Turno *:");
            String turno = ((Administrador) u).getTurno();
            String turnoNorm = turno.replace("MAÑANA", "MANANA").toUpperCase();
            for (int i = 0; i < cmbTurno.getItemCount(); i++)
                if (cmbTurno.getItemAt(i).equalsIgnoreCase(turnoNorm)) { cmbTurno.setSelectedIndex(i); break; }
        } else if (u instanceof Cajero) {
            cmbTipoUsuario.setSelectedIndex(1);
            cardLayout.show(pnlEspecifico, CARD_CAJERO);
            lblEspecifico.setText("Caja asignada *:");
            txtCajaAsignada.setText(String.valueOf(((Cajero) u).getCajaAsignada()));
        }

        validarNombre();
        validarEmail();
        validarContrasena();
    }

    // ── Campo dinámico ────────────────────────────────────────────────────────

    private void cambiarCampoEspecifico() {
        boolean esAdmin = cmbTipoUsuario.getSelectedIndex() == 0;
        if (esAdmin) {
            lblEspecifico.setText("Turno *:");
            cardLayout.show(pnlEspecifico, CARD_ADMIN);
        } else {
            lblEspecifico.setText("Caja asignada *:");
            cardLayout.show(pnlEspecifico, CARD_CAJERO);
        }
        actualizarBotonAccion();
    }

    // ── Validaciones ─────────────────────────────────────────────────────────

    private void validarNombre() {
        String v  = txtNombre.getText().trim();
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
        String v = new String(txtContrasena.getPassword()).trim();
        boolean esAdmin = cmbTipoUsuario.getSelectedIndex() == 0;
        int minLen = esAdmin ? 8 : 6;
        boolean ok = v.length() >= minLen;
        lblContrasenaStatus.setText(ok ? "(OK)" : "(O)");
        lblContrasenaStatus.setForeground(ok ? new Color(0, 130, 0) : new Color(180, 0, 0));
        lblContrasenaStatus.setToolTipText("Minimo " + minLen + " caracteres");
        if (txtContrasena.isEditable())
            txtContrasena.setBackground(ok || v.isEmpty() ? Color.WHITE : new Color(255, 240, 240));
        actualizarBotonAccion();
    }

    private void validarCaja() {
        String v  = txtCajaAsignada.getText().trim();
        String error = v.isEmpty() ? null : Validaciones.validarEnteroPositivo(v, "numero de caja");
        boolean ok = error == null && !v.isEmpty();
        lblEspecificoStatus.setText(ok ? "(OK)" : "(O)");
        lblEspecificoStatus.setForeground(ok ? new Color(0, 130, 0) : new Color(180, 0, 0));
        if (txtCajaAsignada.isEditable())
            txtCajaAsignada.setBackground(ok || v.isEmpty() ? Color.WHITE : new Color(255, 240, 240));
        actualizarBotonAccion();
    }

    private void actualizarBotonAccion() {
        if (modo == Modo.VER || modo == Modo.INACTIVAR) return;

        boolean esAdmin = cmbTipoUsuario.getSelectedIndex() == 0;
        int minPass = esAdmin ? 8 : 6;
        String pass = new String(txtContrasena.getPassword()).trim();
        String email = txtEmail.getText().trim();

        boolean campoEspecificoOk;
        if (esAdmin) {
            campoEspecificoOk = true;
        } else {
            String caja = txtCajaAsignada.getText().trim();
            campoEspecificoOk = !caja.isEmpty() && Validaciones.validarEnteroPositivo(caja, "caja") == null;
        }

        boolean valido = txtNombre.getText().trim().length() >= 3
                && Validaciones.validarEmail(email)
                && pass.length() >= minPass
                && campoEspecificoOk;

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
        Usuario u = leerFormulario(-1);
        if (u == null) return;
        try {
            // El id se genera automáticamente en la capa de persistencia
            // (auto-incremental: máximo id existente + 1).

            // Asignar rol seleccionado
            asignarRolSeleccionado(u);

            fachada.agregarUsuario(u);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Usuario agregado correctamente.\n" +
                            "Nombre: " + u.getNombre() + "\n" +
                            "Tipo: " + u.getPermiso() +
                            (u.getRol() != null ? "\nRol: " + u.getRol().getNombreCargo() : ""),
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionActualizar() {
        Usuario u = leerFormulario(usuarioOriginal.getIdUsuario());
        if (u == null) return;
        try {
            // Asignar rol seleccionado
            asignarRolSeleccionado(u);

            fachada.actualizarUsuario(u);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Usuario actualizado correctamente." +
                            (u.getRol() != null ? "\nRol: " + u.getRol().getNombreCargo() : ""),
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionInactivar() {
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Desea inactivar al usuario \"" + usuarioOriginal.getNombre() + "\"?\n" +
                        "El registro no se eliminará, solo dejará de estar disponible.",
                "Confirmar inactivación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            fachada.inactivarUsuario(usuarioOriginal.getIdUsuario());
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Usuario inactivado correctamente.",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Asigna el rol seleccionado en el combo al usuario.
     */
    private void asignarRolSeleccionado(Usuario u) {
        String rolSeleccionado = (String) cmbRol.getSelectedItem();
        if (rolSeleccionado != null && !"(Sin rol)".equals(rolSeleccionado)) {
            for (Rol r : rolesDisponibles) {
                if (r.getNombreCargo().equals(rolSeleccionado)) {
                    u.setRol(r);
                    return;
                }
            }
        }
        u.setRol(null);
    }

    private void restaurar() {
        if (usuarioOriginal != null) {
            poblarFormulario(usuarioOriginal);
        } else {
            txtNombre.setText(""); txtEmail.setText(""); txtContrasena.setText("");
            txtCajaAsignada.setText("");
            cmbTipoUsuario.setSelectedIndex(0);
            cmbTurno.setSelectedIndex(0);
            cmbRol.setSelectedIndex(0);
            lblNombreStatus.setText(" "); lblEmailStatus.setText(" ");
            lblContrasenaStatus.setText(" "); lblEspecificoStatus.setText(" ");
            lblRolStatus.setText(" ");
            txtNombre.setBackground(Color.WHITE); txtEmail.setBackground(Color.WHITE);
            txtContrasena.setBackground(Color.WHITE); txtCajaAsignada.setBackground(Color.WHITE);
            btnAccion.setEnabled(false);
        }
        JOptionPane.showMessageDialog(this, "Datos restaurados.", "Informacion", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Lee todos los campos y construye un Usuario (Administrador o Cajero).
     * @param id  id del usuario; pasar -1 para que se asigne después.
     * @return el objeto creado, o null si hay error de validación.
     */
    private Usuario leerFormulario(int id) {
        String nombre    = txtNombre.getText().trim();
        String email     = txtEmail.getText().trim();
        String contrasena = new String(txtContrasena.getPassword()).trim();
        boolean esAdmin  = cmbTipoUsuario.getSelectedIndex() == 0;
        int minPass      = esAdmin ? 8 : 6;

        // Validaciones con mensajes descriptivos
        if (nombre.length() < 3) {
            JOptionPane.showMessageDialog(this,
                    "El nombre debe tener al menos 3 caracteres.", "Validacion", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return null;
        }

        String errorEmail = Validaciones.validarEmailConMensaje(email);
        if (errorEmail != null) {
            JOptionPane.showMessageDialog(this, errorEmail, "Validacion", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return null;
        }

        if (contrasena.length() < minPass) {
            JOptionPane.showMessageDialog(this,
                    "La contrasena debe tener al menos " + minPass + " caracteres.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            txtContrasena.requestFocus();
            return null;
        }

        Usuario usuario;
        if (esAdmin) {
            String turno = (String) cmbTurno.getSelectedItem();
            usuario = new Administrador(id, nombre, email, contrasena, turno);
        } else {
            String cajaStr = txtCajaAsignada.getText().trim();
            String errorCaja = Validaciones.validarEnteroPositivo(cajaStr, "numero de caja");
            if (errorCaja != null) {
                JOptionPane.showMessageDialog(this, errorCaja, "Validacion", JOptionPane.WARNING_MESSAGE);
                txtCajaAsignada.requestFocus();
                return null;
            }
            int caja = Integer.parseInt(cajaStr);
            usuario = new Cajero(id, nombre, email, contrasena, caja);
        }

        return usuario;
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
            case AGREGAR:    return "Agregar Usuario";
            case VER:        return "Ver Usuario";
            case ACTUALIZAR: return "Editar Usuario";
            case INACTIVAR:  return "Inactivar Usuario";
            default:         return "Usuario";
        }
    }
}