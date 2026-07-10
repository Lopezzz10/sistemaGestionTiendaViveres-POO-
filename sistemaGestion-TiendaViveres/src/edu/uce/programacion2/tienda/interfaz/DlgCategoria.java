package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.excepciones.FachadaException;
import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.negocio.Categoria;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class DlgCategoria extends JDialog implements ActionListener {

    public enum Modo { AGREGAR, VER, ACTUALIZAR, INACTIVAR }

    private final IFachadaTienda fachada;
    private final Modo          modo;
    private boolean             guardado = false;

    private JTextField        txtClave;
    private JTextField        txtNombre;
    private JComboBox<String> cmbTipo;
    private JLabel            lblClaveStatus;
    private JLabel            lblNombreStatus;
    private JCheckBox         chkIvaEspecial;
    private JComboBox<String> cmbTarifaIvaEspecial;

    private JButton btnAccion;
    private JButton btnRestaurar;
    private JButton btnCancelar;

    private Categoria categoriaOriginal;

    /** Recuerda la última tarifa válida seleccionada (para revertir si se cancela "Otro..."). */
    private String ultimaTarifaValida = "0%";

    public DlgCategoria(Frame owner, IFachadaTienda fachada, Modo modo, String clave) {
        super(owner, tituloVentana(modo), true);
        this.fachada = fachada;
        this.modo    = modo;
        initComponents();
        txtClave.setText(clave);
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(450, 300));
    }

    public DlgCategoria(Frame owner, IFachadaTienda fachada, Modo modo, Categoria c) {
        super(owner, tituloVentana(modo), true);
        this.fachada            = fachada;
        this.modo               = modo;
        this.categoriaOriginal  = c;
        initComponents();
        poblarFormulario(c);
        configModo();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(450, 300));
    }

    public boolean isGuardado() { return guardado; }

    private void initComponents() {
        try {
            UIManager.put("OptionPane.background", new Color(245, 245, 245));
            UIManager.put("Panel.background", new Color(245, 245, 245));
        } catch (Exception e) {}

        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245));

        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 25, 15, 25),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                        "Datos de la Categoria",
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

        txtClave = new JTextField(12);
        txtClave.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtClave.setBackground(new Color(255, 255, 255));
        txtClave.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validarClave(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validarClave(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validarClave(); }
        });

        lblClaveStatus = new JLabel(" ");
        lblClaveStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JPanel clavePanel = new JPanel(new BorderLayout(5, 0));
        clavePanel.setOpaque(false);
        clavePanel.add(txtClave, BorderLayout.CENTER);
        clavePanel.add(lblClaveStatus, BorderLayout.EAST);
        fila(pnl, "Clave *", clavePanel, lc, fc, row++);

        txtNombre = new JTextField(25);
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNombre.setBackground(new Color(255, 255, 255));
        txtNombre.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validarNombre(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validarNombre(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validarNombre(); }
        });
        txtNombre.setToolTipText("Ingrese el nombre de la categoria (minimo 3 caracteres)");

        lblNombreStatus = new JLabel(" ");
        lblNombreStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JPanel nombrePanel = new JPanel(new BorderLayout(5, 0));
        nombrePanel.setOpaque(false);
        nombrePanel.add(txtNombre, BorderLayout.CENTER);
        nombrePanel.add(lblNombreStatus, BorderLayout.EAST);
        fila(pnl, "Nombre *", nombrePanel, lc, fc, row++);

        cmbTipo = new JComboBox<>(new String[]{"P - Perecible", "N - No Perecible"});
        cmbTipo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbTipo.setBackground(Color.WHITE);
        fila(pnl, "Tipo", cmbTipo, lc, fc, row++);

        // ── IVA especial predeterminado ─────────────────────────────────────
        // Permite marcar categorías como "canasta básica" (Lácteos, Carnes,
        // Granos, Enlatados, etc.) con una tarifa fija (típicamente 0%), para
        // que el cajero nunca tenga que decidir el IVA manualmente: sale
        // automático del producto -> categoría. Si no se marca, el producto
        // usa la tasa general vigente (la que el administrador cambia desde
        // "Configuracion > Configurar IVA", ej. por feriados).
        chkIvaEspecial = new JCheckBox("Tiene tarifa de IVA especial (predeterminada)");
        chkIvaEspecial.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkIvaEspecial.setOpaque(false);
        chkIvaEspecial.addActionListener(e ->
                cmbTarifaIvaEspecial.setEnabled(chkIvaEspecial.isSelected() && chkIvaEspecial.isEnabled()));
        fila(pnl, "IVA Especial", chkIvaEspecial, lc, fc, row++);

        cmbTarifaIvaEspecial = new JComboBox<>(new String[]{"0%", "5%", "8%", "15%", "Otro..."});
        cmbTarifaIvaEspecial.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbTarifaIvaEspecial.setBackground(Color.WHITE);
        cmbTarifaIvaEspecial.setEnabled(false);
        cmbTarifaIvaEspecial.addActionListener(e -> manejarSeleccionTarifaPersonalizada());
        fila(pnl, "Tarifa Fija", cmbTarifaIvaEspecial, lc, fc, row++);

        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlBot.setBackground(new Color(245, 245, 245));
        pnlBot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        // Botones con texto en NEGRO para mejor visibilidad
        btnAccion = crearBoton("Guardar", new Color(70, 130, 180), Color.BLACK);
        btnRestaurar = crearBoton("Restaurar", new Color(100, 100, 100), Color.BLACK);
        btnCancelar = crearBoton("Cancelar", new Color(180, 80, 80), Color.BLACK);

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

    /**
     * Si el usuario elige "Otro..." en el combo de tarifa fija, pide un
     * porcentaje personalizado por teclado (ej. 12.5), lo agrega al combo
     * como una opcion mas (ej. "12.5%") y la deja seleccionada. Si cancela
     * o ingresa un valor invalido, revierte a la ultima tarifa valida.
     */
    private void manejarSeleccionTarifaPersonalizada() {
        Object seleccionado = cmbTarifaIvaEspecial.getSelectedItem();
        if (seleccionado == null || !"Otro...".equals(seleccionado.toString())) {
            if (seleccionado != null && !"Otro...".equals(seleccionado.toString())) {
                ultimaTarifaValida = seleccionado.toString();
            }
            return;
        }

        String entrada = JOptionPane.showInputDialog(this,
                "Ingrese el porcentaje de IVA (ejemplo: 12.5 para 12.5%):",
                "Tarifa de IVA personalizada",
                JOptionPane.QUESTION_MESSAGE);

        if (entrada == null) {
            cmbTarifaIvaEspecial.setSelectedItem(ultimaTarifaValida);
            return;
        }

        try {
            double porcentaje = Double.parseDouble(entrada.trim().replace("%", "").replace(",", "."));
            if (porcentaje < 0 || porcentaje > 100) {
                JOptionPane.showMessageDialog(this,
                        "El porcentaje debe estar entre 0 y 100.",
                        "Valor invalido", JOptionPane.WARNING_MESSAGE);
                cmbTarifaIvaEspecial.setSelectedItem(ultimaTarifaValida);
                return;
            }
            String texto = formatearPorcentaje(porcentaje) + "%";

            boolean yaExiste = false;
            for (int i = 0; i < cmbTarifaIvaEspecial.getItemCount(); i++) {
                if (cmbTarifaIvaEspecial.getItemAt(i).equals(texto)) { yaExiste = true; break; }
            }
            if (!yaExiste) {
                cmbTarifaIvaEspecial.insertItemAt(texto, cmbTarifaIvaEspecial.getItemCount() - 1);
            }
            ultimaTarifaValida = texto;
            cmbTarifaIvaEspecial.setSelectedItem(texto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Debe ingresar un numero valido (ejemplo: 12.5).",
                    "Error", JOptionPane.ERROR_MESSAGE);
            cmbTarifaIvaEspecial.setSelectedItem(ultimaTarifaValida);
        }
    }

    /** Formatea un porcentaje sin decimales innecesarios (12 en vez de 12.0, pero 12.5 se conserva). */
    private String formatearPorcentaje(double porcentaje) {
        if (porcentaje == Math.rint(porcentaje)) {
            return String.valueOf((int) porcentaje);
        }
        return String.valueOf(porcentaje);
    }

    private void validarClave() {
        String clave = txtClave.getText().trim();
        if (clave.isEmpty()) {
            lblClaveStatus.setText("(O)");
            lblClaveStatus.setToolTipText("La clave es obligatoria");
            txtClave.setBackground(new Color(255, 240, 240));
        } else if (clave.length() < 3) {
            lblClaveStatus.setText("(O)");
            lblClaveStatus.setToolTipText("Minimo 3 caracteres");
            txtClave.setBackground(new Color(255, 240, 240));
        } else {
            lblClaveStatus.setText("(OK)");
            lblClaveStatus.setToolTipText("Clave valida");
            txtClave.setBackground(Color.WHITE);
        }
        validarFormulario();
    }

    private void validarNombre() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            lblNombreStatus.setText("(O)");
            lblNombreStatus.setToolTipText("El nombre es obligatorio");
            txtNombre.setBackground(new Color(255, 240, 240));
        } else if (nombre.length() < 3) {
            lblNombreStatus.setText("(O)");
            lblNombreStatus.setToolTipText("Minimo 3 caracteres");
            txtNombre.setBackground(new Color(255, 240, 240));
        } else {
            lblNombreStatus.setText("(OK)");
            lblNombreStatus.setToolTipText("Nombre valido");
            txtNombre.setBackground(Color.WHITE);
        }
        validarFormulario();
    }

    private void validarFormulario() {
        boolean valido = !txtClave.getText().trim().isEmpty() &&
                txtClave.getText().trim().length() >= 3 &&
                !txtNombre.getText().trim().isEmpty() &&
                txtNombre.getText().trim().length() >= 3;
        btnAccion.setEnabled(valido || modo == Modo.VER || modo == Modo.INACTIVAR);
    }

    private void configModo() {
        boolean editable = (modo == Modo.AGREGAR || modo == Modo.ACTUALIZAR);
        txtClave.setEditable(false);
        txtNombre.setEditable(editable);
        cmbTipo.setEnabled(editable);
        chkIvaEspecial.setEnabled(editable);
        cmbTarifaIvaEspecial.setEnabled(editable && chkIvaEspecial.isSelected());

        switch (modo) {
            case AGREGAR:
                btnAccion.setText("+ Guardar");
                btnAccion.setBackground(new Color(70, 130, 180));
                break;
            case VER:
                btnAccion.setText("Continuar");
                btnRestaurar.setEnabled(false);
                btnAccion.setBackground(new Color(100, 180, 100));
                break;
            case ACTUALIZAR:
                btnAccion.setText("Actualizar");
                btnAccion.setBackground(new Color(255, 165, 0));
                break;
            case INACTIVAR:
                btnAccion.setText("Inactivar");
                btnRestaurar.setEnabled(false);
                btnAccion.setBackground(new Color(200, 60, 60));
                break;
        }

        getRootPane().setDefaultButton(btnAccion);
    }

    private void poblarFormulario(Categoria c) {
        txtClave.setText(c.getCveCategoria());
        txtNombre.setText(c.getNombre());
        cmbTipo.setSelectedIndex(c.getTipoProducto() == 'P' ? 0 : 1);
        chkIvaEspecial.setSelected(c.isIvaEspecial());
        String tarifaTexto = formatearPorcentaje(c.getTarifaIvaEspecial() * 100) + "%";
        boolean existeEnCombo = false;
        for (int i = 0; i < cmbTarifaIvaEspecial.getItemCount(); i++) {
            if (cmbTarifaIvaEspecial.getItemAt(i).equals(tarifaTexto)) { existeEnCombo = true; break; }
        }
        if (!existeEnCombo) {
            cmbTarifaIvaEspecial.insertItemAt(tarifaTexto, cmbTarifaIvaEspecial.getItemCount() - 1);
        }
        ultimaTarifaValida = tarifaTexto;
        cmbTarifaIvaEspecial.setSelectedItem(tarifaTexto);
        cmbTarifaIvaEspecial.setEnabled(c.isIvaEspecial() && chkIvaEspecial.isEnabled());
        validarClave();
        validarNombre();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCancelar)  {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Esta seguro de cancelar?", "Confirmar",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) dispose();
            return;
        }
        if (e.getSource() == btnRestaurar) {
            restaurar();
            return;
        }
        switch (modo) {
            case AGREGAR:    accionGuardar();    break;
            case VER:        dispose();          break;
            case ACTUALIZAR: accionActualizar(); break;
            case INACTIVAR:  accionInactivar();  break;
        }
    }

    private void accionGuardar() {
        Categoria c = leerFormulario();
        if (c == null) return;
        try {
            fachada.agregarCategoria(c);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Categoria agregada correctamente.\nClave: " + c.getCveCategoria(),
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionActualizar() {
        Categoria c = leerFormulario();
        if (c == null) return;
        try {
            fachada.actualizarCategoria(c);
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Categoria actualizada correctamente.",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionInactivar() {
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Desea inactivar la categoria \"" + categoriaOriginal.getNombre() + "\"?\n" +
                        "El registro no se eliminará, solo dejará de estar disponible.",
                "Confirmar inactivación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            fachada.inactivarCategoria(categoriaOriginal.getCveCategoria());
            guardado = true;
            JOptionPane.showMessageDialog(this,
                    "Categoria inactivada correctamente.",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al inactivar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restaurar() {
        if (categoriaOriginal != null) {
            poblarFormulario(categoriaOriginal);
        } else {
            txtNombre.setText("");
            cmbTipo.setSelectedIndex(0);
            chkIvaEspecial.setSelected(false);
            cmbTarifaIvaEspecial.setSelectedItem("0%");
            cmbTarifaIvaEspecial.setEnabled(false);
            validarNombre();
        }
        JOptionPane.showMessageDialog(this,
                "Datos restaurados.",
                "Informacion", JOptionPane.INFORMATION_MESSAGE);
    }

    private Categoria leerFormulario() {
        String clave  = txtClave.getText().trim();
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El nombre es obligatorio.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return null;
        }
        if (nombre.length() < 3) {
            JOptionPane.showMessageDialog(this,
                    "El nombre debe tener al menos 3 caracteres.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return null;
        }

        char tipo = cmbTipo.getSelectedIndex() == 0 ? 'P' : 'N';
        boolean ivaEspecial = chkIvaEspecial.isSelected();
        double tarifaIvaEspecial = 0.0;
        if (ivaEspecial) {
            String seleccion = (String) cmbTarifaIvaEspecial.getSelectedItem();
            tarifaIvaEspecial = Double.parseDouble(seleccion.replace("%", "")) / 100.0;
        }
        return new Categoria(clave, nombre, tipo, ivaEspecial, tarifaIvaEspecial);
    }

    private static String tituloVentana(Modo modo) {
        switch (modo) {
            case AGREGAR:    return "Captura Datos Categoria";
            case VER:        return "La categoria ya existe";
            case ACTUALIZAR: return "Editar Datos Categoria";
            case INACTIVAR:  return "Categoria a Inactivar";
            default:         return "Categoria";
        }
    }
}