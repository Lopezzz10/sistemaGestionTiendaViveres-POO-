package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.control.Control;
import edu.uce.programacion2.tienda.negocio.Usuario;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Ventana de acceso al sistema. Un único formulario sirve para ambos roles
 * (Administrador y Cajero): pide email + contraseña, delega la validación
 * en {@link Control#
 * autenticarUsuario(JFrame, String, String)}, y el rol
 * concreto se determina de forma polimórfica según el {@link Usuario}
 * devuelto (Administrador o Cajero).
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DlgLogin extends JDialog implements ActionListener {

    private final Control control;
    private Usuario        usuarioAutenticado;

    private JTextField     txtEmail;
    private JPasswordField txtContrasena;
    private JButton         btnIngresar;
    private JButton         btnCancelar;

    public DlgLogin(Frame owner, Control control) {
        super(owner, "Iniciar Sesion", true);
        this.control = control;
        initComponents();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(380, 260));
    }

    /** Retorna el usuario autenticado, o {@code null} si se canceló el login. */
    public Usuario getUsuarioAutenticado() { return usuarioAutenticado; }

    private void initComponents() {
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245));

        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 25, 15, 25),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                        "Acceso al Sistema",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
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

        txtEmail = new JTextField(18);
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtEmail.setBackground(Color.WHITE);
        fila(pnl, "Email", txtEmail, lc, fc, row++);

        txtContrasena = new JPasswordField(18);
        txtContrasena.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtContrasena.setBackground(Color.WHITE);
        fila(pnl, "Contrasena", txtContrasena, lc, fc, row++);

        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlBot.setBackground(new Color(245, 245, 245));
        pnlBot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        btnIngresar = crearBoton("Ingresar", new Color(70, 130, 180), Color.BLACK);
        btnCancelar = crearBoton("Cancelar", new Color(180, 80, 80), Color.BLACK);

        btnIngresar.addActionListener(this);
        btnCancelar.addActionListener(this);

        pnlBot.add(btnIngresar);
        pnlBot.add(btnCancelar);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pnl,    BorderLayout.CENTER);
        getContentPane().add(pnlBot, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnIngresar);
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
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
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(fondo.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(fondo);
            }
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCancelar) {
            usuarioAutenticado = null;
            dispose();
            return;
        }
        if (e.getSource() == btnIngresar) {
            accionIngresar();
        }
    }

    private void accionIngresar() {
        String email      = txtEmail.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        if (email.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe ingresar email y contrasena.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario u = control.autenticarUsuario(this, email, contrasena);
        if (u != null) {
            // ── Mostrar información del usuario y su rol ─────────────────────
            String mensaje = "Bienvenido " + u.getNombre() + "!";

            // Mostrar el tipo de usuario o rol
            if (u.getRol() != null) {
                mensaje += "\n\nRol: " + u.getRol().getNombreCargo();
                mensaje += "\nPermisos: " + u.getRol().getPermisosTexto();
            } else {
                mensaje += "\n\nPermiso: " + u.getPermiso();
                // Mostrar permisos por defecto según el tipo
                if (u.getPermiso().equals("ADMINISTRADOR")) {
                    mensaje += "\nAcceso: Administrador (todos los permisos)";
                } else if (u.getPermiso().equals("CAJERO")) {
                    mensaje += "\nAcceso: Cajero (ventas, facturas y reportes)";
                } else if (u.getPermiso().equals("CLIENTE")) {
                    mensaje += "\nAcceso: Cliente (compras y consulta de puntos)";
                }
            }

            // Mostrar el mensaje de bienvenida
            JOptionPane.showMessageDialog(this,
                    mensaje,
                    "Acceso concedido",
                    JOptionPane.INFORMATION_MESSAGE);

            usuarioAutenticado = u;
            dispose();
        } else {
            txtContrasena.setText("");
            txtContrasena.requestFocus();
        }
    }
}