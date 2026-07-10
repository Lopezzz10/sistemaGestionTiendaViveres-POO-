package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.negocio.*;
import edu.uce.programacion2.tienda.excepciones.FachadaException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class DlgCompra extends JDialog {

    public enum Modo { AGREGAR, ACTUALIZAR, INACTIVAR }

    private final IFachadaTienda fachada;
    private final Modo modo;

    private JTextField txtId;
    private JComboBox<String> cmbMetodoPago;
    private JComboBox<String> cmbEstado;
    private JComboBox<String> cmbProveedor;
    private JComboBox<String> cmbProducto;
    private JTextField txtCantidad;
    private JTextField txtPrecioCompra;
    private JButton btnAgregarProducto;
    private JButton btnQuitarProducto;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotal;
    private JButton btnAceptar;
    private JButton btnCancelar;

    private ArrayList<Proveedor> proveedoresDisponibles = new ArrayList<>();
    private ArrayList<Producto> productosDisponibles = new ArrayList<>();

    public DlgCompra(JFrame parent, IFachadaTienda fachada, Modo modo) {
        super(parent, true);
        this.fachada = fachada;
        this.modo = modo;

        switch (modo) {
            case AGREGAR:    setTitle("Agregar Compra");    break;
            case ACTUALIZAR: setTitle("Actualizar Compra"); break;
            case INACTIVAR:  setTitle("Inactivar Compra");  break;
        }

        setSize(620, 680);
        setLocationRelativeTo(parent);
        setResizable(false);
        cargarDatos();
        initComponentes();
    }

    private void cargarDatos() {
        proveedoresDisponibles.addAll(fachada.listarProveedores());
        try {
            productosDisponibles.addAll(fachada.listarProductos());
        } catch (FachadaException e) { /* catálogo vacío o error de carga */ }
    }

    private void initComponentes() {
        getContentPane().setBackground(new Color(245, 245, 245));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 25, 15, 25),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                        "Datos de la Compra",
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
        JLabel lblId = new JLabel("ID Compra:");
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
        cmbEstado = new JComboBox<>(new String[]{"PENDIENTE", "RECIBIDA", "ANULADA"});
        cmbEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbEstado.setBackground(Color.WHITE);
        panel.add(cmbEstado, gbc);
        gbc.gridwidth = 1;

        // Proveedor
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblProveedor = new JLabel("Proveedor:");
        lblProveedor.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblProveedor.setForeground(new Color(50, 50, 50));
        panel.add(lblProveedor, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbProveedor = new JComboBox<>();
        cmbProveedor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbProveedor.setBackground(Color.WHITE);
        if (proveedoresDisponibles.isEmpty()) {
            cmbProveedor.addItem("No hay proveedores disponibles");
        } else {
            for (Proveedor p : proveedoresDisponibles)
                cmbProveedor.addItem(p.getNombre() + " - RUC: " + p.getRuc());
        }
        panel.add(cmbProveedor, gbc);
        gbc.gridwidth = 1;

        if (modo == Modo.AGREGAR) {
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

            // Precio de compra
            gbc.gridx = 0; gbc.gridy = 6;
            JLabel lblPrecio = new JLabel("Precio Compra:");
            lblPrecio.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblPrecio.setForeground(new Color(50, 50, 50));
            panel.add(lblPrecio, gbc);
            gbc.gridx = 1;
            txtPrecioCompra = new JTextField("0.00", 10);
            txtPrecioCompra.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtPrecioCompra.setBackground(Color.WHITE);
            panel.add(txtPrecioCompra, gbc);

            // Botón agregar producto - TEXTO EN NEGRO
            gbc.gridx = 2; gbc.gridy = 5;
            gbc.gridheight = 2;
            btnAgregarProducto = new JButton("+ Agregar");
            btnAgregarProducto.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnAgregarProducto.setBackground(new Color(70, 130, 180));
            btnAgregarProducto.setForeground(Color.BLACK);  // Cambiado a NEGRO
            btnAgregarProducto.setFocusPainted(false);
            btnAgregarProducto.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 100, 150), 1),
                    BorderFactory.createEmptyBorder(6, 15, 6, 15)
            ));
            btnAgregarProducto.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.add(btnAgregarProducto, gbc);
            gbc.gridheight = 1;

            // Tabla de productos seleccionados
            gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
            modeloTabla = new DefaultTableModel(
                    new Object[]{"Producto", "Cantidad", "Precio Compra", "Subtotal"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            tablaProductos = new JTable(modeloTabla);
            tablaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tablaProductos.setRowHeight(25);
            tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tablaProductos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            tablaProductos.getTableHeader().setBackground(new Color(230, 230, 230));
            JScrollPane scroll = new JScrollPane(tablaProductos);
            scroll.setPreferredSize(new Dimension(500, 120));
            scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            panel.add(scroll, gbc);
            gbc.gridwidth = 1;

            // Botón quitar producto - TEXTO EN NEGRO
            gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 3;
            btnQuitarProducto = new JButton("- Quitar seleccionado");
            btnQuitarProducto.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnQuitarProducto.setBackground(new Color(180, 80, 80));
            btnQuitarProducto.setForeground(Color.BLACK);  // Cambiado a NEGRO
            btnQuitarProducto.setFocusPainted(false);
            btnQuitarProducto.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(150, 60, 60), 1),
                    BorderFactory.createEmptyBorder(6, 15, 6, 15)
            ));
            btnQuitarProducto.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.add(btnQuitarProducto, gbc);
            gbc.gridwidth = 1;

            // Total
            gbc.gridx = 0; gbc.gridy = 9;
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
                    double precioCompra = Double.parseDouble(txtPrecioCompra.getText().trim());
                    if (cantidad <= 0) {
                        JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (precioCompra <= 0) {
                        JOptionPane.showMessageDialog(this, "El precio de compra debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int idx = cmbProducto.getSelectedIndex();
                    Producto p = productosDisponibles.get(idx);
                    double subtotal = precioCompra * cantidad;
                    modeloTabla.addRow(new Object[]{
                            p.getNombre(),
                            cantidad,
                            "$" + String.format("%.2f", precioCompra),
                            "$" + String.format("%.2f", subtotal)
                    });
                    actualizarTotal();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Cantidad y precio deben ser números.", "Error", JOptionPane.ERROR_MESSAGE);
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

        // Botones - TEXTO EN NEGRO
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.setBackground(new Color(245, 245, 245));
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        btnAceptar = new JButton("Aceptar");
        btnAceptar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAceptar.setBackground(new Color(70, 130, 180));
        btnAceptar.setForeground(Color.BLACK);  // Cambiado a NEGRO
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 100, 150), 1),
                BorderFactory.createEmptyBorder(8, 25, 8, 25)
        ));
        btnAceptar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelar.setBackground(new Color(180, 80, 80));
        btnCancelar.setForeground(Color.BLACK);  // Cambiado a NEGRO
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 60, 60), 1),
                BorderFactory.createEmptyBorder(8, 25, 8, 25)
        ));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 5, 0, 5);
        panel.add(panelBotones, gbc);

        add(panel);

        btnCancelar.addActionListener(e -> dispose());

        btnAceptar.addActionListener(e -> {
            try {
                if (modo == Modo.AGREGAR) {
                    if (proveedoresDisponibles.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No hay proveedores disponibles.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (modeloTabla.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "Agregue al menos un producto.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String metodo = (String) cmbMetodoPago.getSelectedItem();
                    Proveedor proveedorSeleccionado = proveedoresDisponibles.get(cmbProveedor.getSelectedIndex());

                    // El id (0) es un valor provisional: la fachada lo
                    // sobrescribe con el siguiente id auto-incremental.
                    Compra c = new Compra(0, metodo, proveedorSeleccionado, null);
                    c.setEstado(Compra.Estado.valueOf((String) cmbEstado.getSelectedItem()));

                    for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                        String nombreProducto = (String) modeloTabla.getValueAt(i, 0);
                        int cantidad = (int) modeloTabla.getValueAt(i, 1);
                        String precioStr = ((String) modeloTabla.getValueAt(i, 2)).replace("$", "");
                        double precioCompra = Double.parseDouble(precioStr);
                        Producto productoSeleccionado = null;
                        for (Producto p : productosDisponibles)
                            if (p.getNombre().equals(nombreProducto)) { productoSeleccionado = p; break; }
                        if (productoSeleccionado != null)
                            c.agregarDetalle(new DetalleCompra(i + 1, productoSeleccionado, cantidad, precioCompra, 0.0));
                    }
                    fachada.registrarCompra(c);
                    JOptionPane.showMessageDialog(this,
                            "Compra registrada correctamente.\nID asignado: " + c.getIdCompra());

                } else {
                    String idStr = txtId.getText().trim();
                    if (idStr.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Ingrese el ID de la compra.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int id = Integer.parseInt(idStr);

                    if (modo == Modo.ACTUALIZAR) {
                    Compra encontrada = null;
                    for (Compra compra : fachada.listarCompras())
                        if (compra.getIdCompra() == id) { encontrada = compra; break; }
                    if (encontrada == null) {
                        JOptionPane.showMessageDialog(this, "Compra no encontrada: id=" + id, "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    encontrada.setEstado(Compra.Estado.valueOf((String) cmbEstado.getSelectedItem()));
                    if (!proveedoresDisponibles.isEmpty())
                        encontrada.setProveedor(proveedoresDisponibles.get(cmbProveedor.getSelectedIndex()));
                    fachada.actualizarCompra(encontrada);
                    JOptionPane.showMessageDialog(this, "Compra actualizada correctamente.");

                    } else if (modo == Modo.INACTIVAR) {
                        int confirm = JOptionPane.showConfirmDialog(this,
                                "¿Desea inactivar la compra #" + id + "?\n" +
                                        "El registro no se eliminará, solo dejará de estar disponible.",
                                "Confirmar inactivación", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            fachada.inactivarCompra(id);
                            JOptionPane.showMessageDialog(this, "Compra inactivada correctamente.");
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