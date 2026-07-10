package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.objetosServicio.Fecha;
import edu.uce.programacion2.tienda.objetosServicio.Periodo;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DlgPeriodo extends JDialog implements ActionListener {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    private JFormattedTextField txtDesde;
    private JFormattedTextField txtHasta;
    private JButton             btnGuardar;
    private JButton             btnRestaurar;
    private JButton             btnCancelar;
    private JLabel              lblDesdeStatus;
    private JLabel              lblHastaStatus;

    private boolean aceptado    = false;
    private Date    fechaInicio = null;
    private Date    fechaFin    = null;

    public DlgPeriodo(Frame owner) {
        super(owner, "Captura Periodo", true);
        initComponents();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setMinimumSize(new Dimension(400, 250));
        getRootPane().setDefaultButton(btnGuardar);

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public boolean isAceptado()    { return aceptado; }
    public Date    getFechaInicio(){ return fechaInicio; }
    public Date    getFechaFin()   { return fechaFin; }

    public Periodo getPeriodo() {
        if (fechaInicio == null || fechaFin == null) return null;
        return new Periodo(Fecha.desdeDate(fechaInicio), Fecha.desdeDate(fechaFin));
    }

    private void initComponents() {
        getContentPane().setBackground(new Color(245, 245, 245));

        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 25, 15, 25),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                        "Seleccione el Periodo",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
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

        try {
            MaskFormatter mask = new MaskFormatter("##/##/####");
            mask.setPlaceholderCharacter('_');
            txtDesde = new JFormattedTextField(mask);
            txtDesde.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtDesde.setToolTipText("Formato: dd/mm/aaaa");
        } catch (ParseException e) {
            txtDesde = new JFormattedTextField();
        }

        lblDesdeStatus = new JLabel(" ");
        lblDesdeStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JPanel desdePanel = new JPanel(new BorderLayout(5, 0));
        desdePanel.setOpaque(false);
        desdePanel.add(txtDesde, BorderLayout.CENTER);
        desdePanel.add(lblDesdeStatus, BorderLayout.EAST);

        lc.gridy = fc.gridy = 0; lc.gridx = 0; fc.gridx = 1;
        JLabel lblDesde = new JLabel("Desde (dd/MM/yyyy):");
        lblDesde.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnl.add(lblDesde, lc);
        pnl.add(desdePanel, fc);

        try {
            MaskFormatter mask = new MaskFormatter("##/##/####");
            mask.setPlaceholderCharacter('_');
            txtHasta = new JFormattedTextField(mask);
            txtHasta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtHasta.setToolTipText("Formato: dd/mm/aaaa");
        } catch (ParseException e) {
            txtHasta = new JFormattedTextField();
        }

        lblHastaStatus = new JLabel(" ");
        lblHastaStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JPanel hastaPanel = new JPanel(new BorderLayout(5, 0));
        hastaPanel.setOpaque(false);
        hastaPanel.add(txtHasta, BorderLayout.CENTER);
        hastaPanel.add(lblHastaStatus, BorderLayout.EAST);

        lc.gridy = fc.gridy = 1;
        JLabel lblHasta = new JLabel("Hasta (dd/MM/yyyy):");
        lblHasta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnl.add(lblHasta, lc);
        pnl.add(hastaPanel, fc);

        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlBot.setBackground(new Color(245, 245, 245));
        pnlBot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        // Botones con texto en NEGRO para mejor visibilidad
        btnGuardar = crearBoton("Aceptar", new Color(70, 130, 180), Color.BLACK);
        btnRestaurar = crearBoton("Restaurar", new Color(100, 100, 100), Color.BLACK);
        btnCancelar = crearBoton("Cancelar", new Color(180, 80, 80), Color.BLACK);

        btnGuardar.addActionListener(this);
        btnRestaurar.addActionListener(this);
        btnCancelar.addActionListener(this);

        pnlBot.add(btnGuardar);
        pnlBot.add(btnRestaurar);
        pnlBot.add(btnCancelar);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pnl,    BorderLayout.CENTER);
        getContentPane().add(pnlBot, BorderLayout.SOUTH);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCancelar) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Esta seguro de cancelar?", "Confirmar",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) dispose();
        } else if (e.getSource() == btnRestaurar) {
            txtDesde.setText("");
            txtHasta.setText("");
            lblDesdeStatus.setText(" ");
            lblHastaStatus.setText(" ");
            JOptionPane.showMessageDialog(this,
                    "Campos restaurados.",
                    "Informacion", JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getSource() == btnGuardar) {
            accionGuardar();
        }
    }

    private void accionGuardar() {
        String desdeStr = txtDesde.getText().trim();
        String hastaStr = txtHasta.getText().trim();

        if (desdeStr.isEmpty() || desdeStr.equals("__/__/____")) {
            lblDesdeStatus.setText("(O)");
            lblDesdeStatus.setToolTipText("La fecha desde es obligatoria");
            JOptionPane.showMessageDialog(this,
                    "La fecha 'Desde' es obligatoria.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            txtDesde.requestFocus();
            return;
        }

        if (hastaStr.isEmpty() || hastaStr.equals("__/__/____")) {
            lblHastaStatus.setText("(O)");
            lblHastaStatus.setToolTipText("La fecha hasta es obligatoria");
            JOptionPane.showMessageDialog(this,
                    "La fecha 'Hasta' es obligatoria.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            txtHasta.requestFocus();
            return;
        }

        try {
            fechaInicio = SDF.parse(desdeStr);
            fechaFin    = SDF.parse(hastaStr);

            if (fechaInicio.after(fechaFin)) {
                JOptionPane.showMessageDialog(this,
                        "La fecha de inicio debe ser anterior a la fecha de fin.",
                        "Validacion", JOptionPane.WARNING_MESSAGE);
                fechaInicio = null;
                fechaFin = null;
                txtDesde.requestFocus();
                return;
            }

            Date hoy = new Date();
            if (fechaFin.before(hoy)) {
                JOptionPane.showMessageDialog(this,
                        "La fecha final es anterior a hoy. Desea continuar?",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
            }

            lblDesdeStatus.setText("(OK)");
            lblHastaStatus.setText("(OK)");
            aceptado = true;
            dispose();

        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Formato de fecha invalido. Use dd/MM/yyyy.\nEjemplo: 25/12/2024",
                    "Error de formato", JOptionPane.ERROR_MESSAGE);
            txtDesde.requestFocus();
        }
    }
}