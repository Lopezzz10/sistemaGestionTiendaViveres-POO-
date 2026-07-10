package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.excepciones.FachadaException;
import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.objetosServicio.Permiso;
import edu.uce.programacion2.tienda.negocio.Rol;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Diálogo para que el Administrador gestione roles (cargos) dinámicos.
 *
 * Permite ingresar el nombre del cargo y elegir, mediante checkboxes, los
 * permisos que ese cargo tendrá en el sistema (uno por cada valor del enum
 * {@link Permiso}, generados automáticamente).
 *
 * Modos AGREGAR / VER / ACTUALIZAR / INACTIVAR, siguiendo el mismo patrón
 * que {@code DlgCategoria} y {@code DlgUsuario}.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DlgRol extends JDialog implements ActionListener {

    public enum Modo { AGREGAR, VER, ACTUALIZAR, INACTIVAR }

    private final IFachadaTienda fachada;
    private final Modo           modo;
    private boolean              guardado = false;

    private JTextField txtCargo;
    private JLabel     lblCargoStatus;

    /** Un checkbox por cada permiso disponible en el sistema. */
    private final Map<Permiso, JCheckBox> checkboxesPermisos = new LinkedHashMap<>();

    private JButton btnAccion;
    private JButton btnRestaurar;
    private JButton btnCancelar;

    private Rol rolOriginal;

    // ── Constructores ───────────────────────────────────────────────────────

    /** AGREGAR: sin objeto previo. */
    public DlgRol(Frame owner, IFachadaTienda fachada, Modo modo) {
        super(owner, tituloVentana(modo), true);
        this.fachada = fachada;
        this.modo    = modo;
        initComponents();
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(430, 480));
    }

    /** VER / ACTUALIZAR / INACTIVAR: con objeto existente. */
    public DlgRol(Frame owner, IFachadaTienda fachada, Modo modo, Rol r) {
        super(owner, tituloVentana(modo), true);
        this.fachada     = fachada;
        this.modo        = modo;
        this.rolOriginal = r;
        initComponents();
        poblarFormulario(r);
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(430, 480));
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
                        "Datos del Rol",
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

        // Nombre del cargo
        txtCargo = new JTextField(20);
        txtCargo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCargo.setBackground(Color.WHITE);
        txtCargo.getDocument().addDocumentListener(docListener(this::validarCargo));

        lblCargoStatus = new JLabel(" ");
        lblCargoStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JPanel cargoPanel = new JPanel(new BorderLayout(5, 0));
        cargoPanel.setOpaque(false);
        cargoPanel.add(txtCargo,       BorderLayout.CENTER);
        cargoPanel.add(lblCargoStatus, BorderLayout.EAST);
        fila(pnl, "Nombre del Cargo *", cargoPanel, lc, fc, row++);

        // Permisos: un checkbox por cada valor del enum Permiso
        JPanel pnlPermisos = new JPanel();
        pnlPermisos.setLayout(new BoxLayout(pnlPermisos, BoxLayout.Y_AXIS));
        pnlPermisos.setBackground(Color.WHITE);
        for (Permiso p : Permiso.values()) {
            JCheckBox chk = new JCheckBox(p.getDescripcion());
            chk.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            chk.setBackground(Color.WHITE);
            chk.addActionListener(e -> actualizarBotonAccion());
            checkboxesPermisos.put(p, chk);
            pnlPermisos.add(chk);
        }

        JScrollPane scrollPermisos = new JScrollPane(pnlPermisos);
        scrollPermisos.setPreferredSize(new Dimension(260, 220));
        scrollPermisos.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        lc.gridy = fc.gridy = row++;
        lc.gridx = 0; fc.gridx = 1;
        lc.anchor = GridBagConstraints.NORTHEAST;
        JLabel lblPermisos = new JLabel("Permisos *:");
        lblPermisos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPermisos.setForeground(new Color(50, 50, 50));
        pnl.add(lblPermisos, lc);
        pnl.add(scrollPermisos, fc);

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
        txtCargo.setEditable(editable);
        txtCargo.setBackground(editable ? Color.WHITE : new Color(235, 235, 235));
        for (JCheckBox chk : checkboxesPermisos.values()) {
            chk.setEnabled(editable);
        }

        switch (modo) {
            case AGREGAR:
                btnAccion.setText("+ Guardar");
                btnAccion.setBackground(new Color(70, 130, 180));
                break;
            case VER:
                btnAccion.setText("Continuar");
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

    // ── Poblar formulario ─────────────────────────────────────────────────────

    private void poblarFormulario(Rol r) {
        txtCargo.setText(r.getNombreCargo());
        for (Map.Entry<Permiso, JCheckBox> entry : checkboxesPermisos.entrySet()) {
            entry.getValue().setSelected(r.tienePermiso(entry.getKey()));
        }
        validarCargo();
    }

    // ── Validaciones ──────────────────────────────────────────────────────────

    private void validarCargo() {
        String v  = txtCargo.getText().trim();
        boolean ok = v.length() >= 3;
        lblCargoStatus.setText(ok ? "(OK)" : "(O)");
        lblCargoStatus.setForeground(ok ? new Color(0, 130, 0) : new Color(180, 0, 0));
        lblCargoStatus.setToolTipText("Minimo 3 caracteres");
        if (txtCargo.isEditable())
            txtCargo.setBackground(ok || v.isEmpty() ? Color.WHITE : new Color(255, 240, 240));
        actualizarBotonAccion();
    }

    private void actualizarBotonAccion() {
        if (modo == Modo.VER || modo == Modo.INACTIVAR) return;
        boolean nombreOk  = txtCargo.getText().trim().length() >= 3;
        boolean algunoMarcado = checkboxesPermisos.values().stream().anyMatch(JCheckBox::isSelected);
        btnAccion.setEnabled(nombreOk && algunoMarcado);
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
        Rol r = leerFormulario();
        if (r == null) return;
        try {
            int nuevoId = fachada.listarRoles().size() + 1;
            r.setIdRol(nuevoId);
            fachada.agregarRol(r);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Rol agregado correctamente.\n" +
                            "Cargo: " + r.getNombreCargo() + "\n" +
                            "Permisos: " + r.getPermisosTexto(),
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionActualizar() {
        Rol r = leerFormulario();
        if (r == null) return;
        r.setIdRol(rolOriginal.getIdRol());
        try {
            fachada.actualizarRol(r);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Rol actualizado correctamente.",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionInactivar() {
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Desea inactivar el rol \"" + rolOriginal.getNombreCargo() + "\"?\n" +
                        "El registro no se eliminará, solo dejará de estar disponible.",
                "Confirmar inactivación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            fachada.inactivarRol(rolOriginal.getIdRol());
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Rol inactivado correctamente.",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restaurar() {
        if (rolOriginal != null) {
            poblarFormulario(rolOriginal);
        } else {
            txtCargo.setText("");
            for (JCheckBox chk : checkboxesPermisos.values()) chk.setSelected(false);
            lblCargoStatus.setText(" ");
            txtCargo.setBackground(Color.WHITE);
            btnAccion.setEnabled(false);
        }
        JOptionPane.showMessageDialog(this, "Datos restaurados.", "Informacion", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Lee el formulario y construye un {@link Rol} nuevo (sin id definitivo).
     * @return el objeto creado, o null si hay error de validación.
     */
    private Rol leerFormulario() {
        String nombreCargo = txtCargo.getText().trim();

        if (nombreCargo.length() < 3) {
            JOptionPane.showMessageDialog(this,
                    "El nombre del cargo debe tener al menos 3 caracteres.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            txtCargo.requestFocus();
            return null;
        }

        Set<Permiso> seleccionados = EnumSet.noneOf(Permiso.class);
        for (Map.Entry<Permiso, JCheckBox> entry : checkboxesPermisos.entrySet()) {
            if (entry.getValue().isSelected()) seleccionados.add(entry.getKey());
        }

        if (seleccionados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar al menos un permiso para el rol.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        return new Rol(-1, nombreCargo, seleccionados);
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────────

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
            case AGREGAR:    return "Agregar Rol";
            case VER:        return "Ver Rol";
            case ACTUALIZAR: return "Editar Rol";
            case INACTIVAR:  return "Inactivar Rol";
            default:         return "Rol";
        }
    }
}
