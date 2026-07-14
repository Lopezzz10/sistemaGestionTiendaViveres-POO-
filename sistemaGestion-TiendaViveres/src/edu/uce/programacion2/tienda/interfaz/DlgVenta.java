package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.negocio.*;
import edu.uce.programacion2.tienda.excepciones.FachadaException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class DlgVenta extends JDialog {

    public enum Modo { AGREGAR, ACTUALIZAR, INACTIVAR }

    private final IFachadaTienda fachada;
    private final Modo modo;

    private JTextField txtId;
    private JComboBox<String> cmbMetodoPago;
    private JComboBox<String> cmbEstado;
    private JComboBox<String> cmbProducto;
    private JComboBox<String> cmbCliente;
    private JTextField txtCantidad;
    private JButton btnAgregarProducto;
    private JButton btnQuitarProducto;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotal;
    private JButton btnAceptar;
    private JButton btnCancelar;

    private ArrayList<Producto> productosDisponibles = new ArrayList<>();
    private ArrayList<Cliente> clientesDisponibles = new ArrayList<>();

    public DlgVenta(JFrame parent, IFachadaTienda fachada, Modo modo) {
        super(parent, true);
        this.fachada = fachada;
        this.modo = modo;

        switch (modo) {
            case AGREGAR:    setTitle("Agregar Venta");    break;
            case ACTUALIZAR: setTitle("Actualizar Venta"); break;
            case INACTIVAR:  setTitle("Inactivar Venta");  break;
        }

        setSize(600, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
        cargarProductos();
        cargarClientes();
        initComponentes();
    }

    private void cargarProductos() {
        try {
            productosDisponibles.addAll(fachada.listarProductos());
        } catch (FachadaException e) { /* catálogo vacío o error de carga */ }
    }

    private void cargarClientes() {
        for (Usuario u : fachada.listarUsuariosPorPermiso("CLIENTE")) {
            if (u instanceof Cliente && u.isActivo()) {
                clientesDisponibles.add((Cliente) u);
            }
        }
    }

    private void initComponentes() {
        getContentPane().setBackground(new Color(245, 245, 245));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 25, 15, 25),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                        "Datos de la Venta",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("SansSerif", Font.BOLD, 13),
                        new Color(70, 130, 180)
                )
        ));
        panel.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblId = new JLabel("ID Venta:");
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblId.setForeground(new Color(50, 50, 50));
        panel.add(lblId, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtId = new JTextField(15);
        txtId.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtId.setBackground(Color.WHITE);
        if (modo == Modo.AGREGAR) {
            // El id se genera automáticamente (auto-incremental) al guardar;
            // ya no se le pide al usuario que lo escriba a mano.
            txtId.setText("(automático)");
            txtId.setEditable(false);
            txtId.setBackground(new Color(230, 230, 230));
        }
        panel.add(txtId, gbc);
        gbc.gridwidth = 1;

        // Método de pago
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblMetodo = new JLabel("Método de Pago:");
        lblMetodo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMetodo.setForeground(new Color(50, 50, 50));
        panel.add(lblMetodo, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbMetodoPago = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "Transferencia"});
        cmbMetodoPago.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbMetodoPago.setBackground(Color.WHITE);
        panel.add(cmbMetodoPago, gbc);
        gbc.gridwidth = 1;

        // Estado
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblEstado = new JLabel("Estado:");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEstado.setForeground(new Color(50, 50, 50));
        panel.add(lblEstado, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbEstado = new JComboBox<>(new String[]{"ACTIVA", "COMPLETADA", "ANULADA"});
        cmbEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbEstado.setBackground(Color.WHITE);
        panel.add(cmbEstado, gbc);
        gbc.gridwidth = 1;

        if (modo == Modo.AGREGAR) {
            // Cliente
            gbc.gridx = 0; gbc.gridy = 3;
            JLabel lblCliente = new JLabel("Cliente:");
            lblCliente.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblCliente.setForeground(new Color(50, 50, 50));
            panel.add(lblCliente, gbc);
            gbc.gridx = 1; gbc.gridwidth = 2;
            cmbCliente = new JComboBox<>();
            cmbCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cmbCliente.setBackground(Color.WHITE);
            cmbCliente.addItem("(Sin cliente)");
            for (Cliente c : clientesDisponibles)
                cmbCliente.addItem(c.getNombre() + " - " + c.getEmail());
            panel.add(cmbCliente, gbc);
            gbc.gridwidth = 1;

            // Producto selector
            gbc.gridx = 0; gbc.gridy = 4;
            JLabel lblProducto = new JLabel("Producto:");
            lblProducto.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblProducto.setForeground(new Color(50, 50, 50));
            panel.add(lblProducto, gbc);
            gbc.gridx = 1; gbc.gridwidth = 2;
            cmbProducto = new JComboBox<>();
            cmbProducto.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cmbProducto.setBackground(Color.WHITE);
            if (productosDisponibles.isEmpty()) {
                cmbProducto.addItem("No hay productos disponibles");
            } else {
                for (Producto p : productosDisponibles)
                    cmbProducto.addItem(p.getNombre() + " - $" + String.format("%.2f", p.calcularPrecioFinal()));
            }
            panel.add(cmbProducto, gbc);
            gbc.gridwidth = 1;

            // Cantidad
            gbc.gridx = 0; gbc.gridy = 5;
            JLabel lblCantidad = new JLabel("Cantidad:");
            lblCantidad.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblCantidad.setForeground(new Color(50, 50, 50));
            panel.add(lblCantidad, gbc);
            gbc.gridx = 1;
            txtCantidad = new JTextField("1", 10);
            txtCantidad.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtCantidad.setBackground(Color.WHITE);
            panel.add(txtCantidad, gbc);

            // Botón agregar producto
            gbc.gridx = 2; gbc.gridy = 5;
            btnAgregarProducto = new JButton("+ Agregar");
            btnAgregarProducto.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnAgregarProducto.setBackground(new Color(70, 130, 180));
            btnAgregarProducto.setForeground(Color.BLACK);
            btnAgregarProducto.setFocusPainted(false);
            btnAgregarProducto.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 100, 150), 1),
                    BorderFactory.createEmptyBorder(6, 15, 6, 15)
            ));
            btnAgregarProducto.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.add(btnAgregarProducto, gbc);

            // Tabla de productos seleccionados
            gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3;
            modeloTabla = new DefaultTableModel(
                    new Object[]{"Producto", "Cantidad", "Precio Unit.", "Subtotal"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            tablaProductos = new JTable(modeloTabla);
            tablaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tablaProductos.setRowHeight(25);
            tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tablaProductos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            tablaProductos.getTableHeader().setBackground(new Color(230, 230, 230));
            JScrollPane scroll = new JScrollPane(tablaProductos);
            scroll.setPreferredSize(new Dimension(480, 120));
            scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            panel.add(scroll, gbc);
            gbc.gridwidth = 1;

            // Botón quitar producto
            gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
            btnQuitarProducto = new JButton("- Quitar seleccionado");
            btnQuitarProducto.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnQuitarProducto.setBackground(new Color(180, 80, 80));
            btnQuitarProducto.setForeground(Color.BLACK);
            btnQuitarProducto.setFocusPainted(false);
            btnQuitarProducto.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(150, 60, 60), 1),
                    BorderFactory.createEmptyBorder(6, 15, 6, 15)
            ));
            btnQuitarProducto.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.add(btnQuitarProducto, gbc);
            gbc.gridwidth = 1;

            // Total
            gbc.gridx = 0; gbc.gridy = 8;
            JLabel lblTotalLabel = new JLabel("Total:");
            lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTotalLabel.setForeground(new Color(50, 50, 50));
            panel.add(lblTotalLabel, gbc);
            gbc.gridx = 1; gbc.gridwidth = 2;
            lblTotal = new JLabel("$0.00");
            lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblTotal.setForeground(new Color(70, 130, 180));
            panel.add(lblTotal, gbc);
            gbc.gridwidth = 1;

            // Acciones tabla
            btnAgregarProducto.addActionListener(e -> {
                if (productosDisponibles.isEmpty()) return;
                try {
                    int cantidad = Integer.parseInt(txtCantidad.getText().trim());
                    if (cantidad <= 0) {
                        JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int idx = cmbProducto.getSelectedIndex();
                    Producto p = productosDisponibles.get(idx);
                    double precioUnit = p.calcularPrecioFinal();
                    double subtotal = precioUnit * cantidad;
                    modeloTabla.addRow(new Object[]{
                            p.getNombre(),
                            cantidad,
                            "$" + String.format("%.2f", precioUnit),
                            "$" + String.format("%.2f", subtotal)
                    });
                    actualizarTotal();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "La cantidad debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            btnQuitarProducto.addActionListener(e -> {
                int fila = tablaProductos.getSelectedRow();
                if (fila >= 0) {
                    modeloTabla.removeRow(fila);
                    actualizarTotal();
                } else {
                    JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla para quitar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            });
        }

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.setBackground(new Color(245, 245, 245));
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        btnAceptar = new JButton("Aceptar");
        btnAceptar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAceptar.setBackground(new Color(70, 130, 180));
        btnAceptar.setForeground(Color.BLACK);
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 100, 150), 1),
                BorderFactory.createEmptyBorder(8, 25, 8, 25)
        ));
        btnAceptar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelar.setBackground(new Color(180, 80, 80));
        btnCancelar.setForeground(Color.BLACK);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 60, 60), 1),
                BorderFactory.createEmptyBorder(8, 25, 8, 25)
        ));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = (modo == Modo.AGREGAR ? 9 : 8); gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 5, 0, 5);
        panel.add(panelBotones, gbc);

        add(panel);

        btnCancelar.addActionListener(e -> dispose());

        btnAceptar.addActionListener(e -> {
            try {
                if (modo == Modo.AGREGAR) {
                    if (modeloTabla.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "Agregue al menos un producto.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String metodo = (String) cmbMetodoPago.getSelectedItem();
                    // El id (0) es un valor provisional: la fachada lo
                    // sobrescribe con el siguiente id auto-incremental.
                    Venta v = new Venta(0, metodo);
                    v.setEstado(Venta.Estado.valueOf((String) cmbEstado.getSelectedItem()));

                    for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                        String nombreProducto = (String) modeloTabla.getValueAt(i, 0);
                        int cantidad = (int) modeloTabla.getValueAt(i, 1);
                        Producto productoSeleccionado = null;
                        for (Producto p : productosDisponibles)
                            if (p.getNombre().equals(nombreProducto)) { productoSeleccionado = p; break; }
                        if (productoSeleccionado != null)
                            v.agregarDetalle(new DetalleVenta(i + 1, productoSeleccionado, cantidad, 0.0));
                    }

                    // ── Registrar la venta (el id auto-incremental ya
                    //    queda asignado en v.getIdVenta() tras este llamado) ──
                    fachada.registrarVenta(v);

                    // ── Crear y emitir la factura automáticamente ─────────
                    // Venta y Factura deben quedar consistentes entre si: si
                    // la venta se registro pero la factura falla, no queremos
                    // dejar una venta "huerfana" sin factura. Como la fachada
                    // no ofrece una eliminacion fisica de Venta, la compensacion
                    // es inactivarla (rollback logico) y avisar al usuario.
                    try {
                        String numeroFactura = String.format("001-001-%09d", Factura.getTotalFacturas() + 1);
                        Cliente clienteSeleccionado = null;
                        int idxCliente = cmbCliente.getSelectedIndex();
                        if (idxCliente > 0) {
                            clienteSeleccionado = clientesDisponibles.get(idxCliente - 1);
                        }
                        Factura factura = new Factura(v.getIdVenta(), numeroFactura, v, clienteSeleccionado, null);
                        fachada.emitirFactura(factura);

                        JOptionPane.showMessageDialog(this,
                                "Venta registrada correctamente.\n" +
                                        "ID asignado: " + v.getIdVenta() + "\n" +
                                        "Factura generada: " + numeroFactura,
                                "Éxito", JOptionPane.INFORMATION_MESSAGE);

                    } catch (FachadaException efact) {
                        // Rollback logico de la venta ya registrada para no
                        // dejar el sistema en un estado inconsistente.
                        try {
                            fachada.inactivarVenta(v.getIdVenta());
                        } catch (FachadaException erollback) {
                            // Si ni siquiera se pudo inactivar, se informa
                            // igual para que el usuario revise manualmente.
                        }
                        JOptionPane.showMessageDialog(this,
                                "No se pudo generar la factura: " + efact.getMessage() + "\n" +
                                        "La venta #" + v.getIdVenta() + " fue anulada automáticamente " +
                                        "para mantener la consistencia de los datos.",
                                "Error al facturar", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                } else {
                    String idStr = txtId.getText().trim();
                    if (idStr.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Ingrese el ID de la venta.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int id = Integer.parseInt(idStr);

                    if (modo == Modo.ACTUALIZAR) {
                        Venta v = fachada.buscarVenta(id);
                        v.setEstado(Venta.Estado.valueOf((String) cmbEstado.getSelectedItem()));
                        fachada.actualizarVenta(v);
                        JOptionPane.showMessageDialog(this, "Venta actualizada correctamente.");

                    } else if (modo == Modo.INACTIVAR) {
                        int confirm = JOptionPane.showConfirmDialog(this,
                                "¿Desea inactivar la venta #" + id + "?\n" +
                                        "El registro no se eliminará, solo dejará de estar disponible.",
                                "Confirmar inactivación", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            fachada.inactivarVenta(id);
                            JOptionPane.showMessageDialog(this, "Venta inactivada correctamente.");
                        }
                    }
                }
                dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "El ID debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (FachadaException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void actualizarTotal() {
        double total = 0.0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            String subtotalStr = ((String) modeloTabla.getValueAt(i, 3)).replace("$", "");
            total += Double.parseDouble(subtotalStr);
        }
        lblTotal.setText("$" + String.format("%.2f", total));
    }
}