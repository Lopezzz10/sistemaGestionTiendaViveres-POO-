package edu.uce.programacion2.tienda.interfaz;

import edu.uce.programacion2.tienda.control.Control;
import edu.uce.programacion2.tienda.control.Tabla;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosCompra;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosFactura;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosInventario;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosProducto;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosProveedor;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosUsuario;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosVenta;
import edu.uce.programacion2.tienda.objetosServicio.Permiso;
import edu.uce.programacion2.tienda.negocio.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class VentanaPrincipal extends JFrame implements ActionListener {

    private final Control control;
    private final Usuario usuarioActual;

    public Control getControl() { return control; }
    public Usuario getUsuarioActual() { return usuarioActual; }

    private final JScrollPane scrollPane;
    private final JLabel      lblEstado;
    private final JLabel      lblTitulo;
    private final JLabel      lblUsuario;
    private final JLabel      lblIvaActual;
    private final JButton     btnCerrarSesion;

    // ── Catalogos ─────────────────────────────────────────────────────────────
    private JMenuItem miAgregarCategoria;
    private JMenuItem miActualizarCategoria;
    private JMenuItem miInactivarCategoria;

    // Productos (unificado)
    private JMenuItem miAgregarProducto;
    private JMenuItem miActualizarProducto;
    private JMenuItem miInactivarProducto;

    private JMenuItem miAgregarInventario;
    private JMenuItem miActualizarInventario;
    private JMenuItem miInactivarInventario;

    private JMenuItem miAgregarProveedor;
    private JMenuItem miActualizarProveedor;
    private JMenuItem miInactivarProveedor;

    private JMenuItem miAgregarVenta;
    private JMenuItem miActualizarVenta;
    private JMenuItem miInactivarVenta;

    private JMenuItem miAgregarCompra;
    private JMenuItem miActualizarCompra;
    private JMenuItem miInactivarCompra;

    private JMenuItem miAgregarUsuario;
    private JMenuItem miActualizarUsuario;
    private JMenuItem miInactivarUsuario;

    // Roles (cargos dinámicos con permisos)
    private JMenuItem miAgregarRol;
    private JMenuItem miActualizarRol;
    private JMenuItem miInactivarRol;

    // Submenús que se ocultan completos según permisos
    private JMenu subProveedores;
    private JMenu subCompras;
    private JMenu subUsuarios;
    private JMenu subRoles;
    private JMenu menuConfiguracion;
    private JMenuItem miConfigurarIva;

    private JMenuItem miSalir;

    // ── Consultas ─────────────────────────────────────────────────────────────
    private JMenuItem miListarProductos;
    private JMenuItem miProductosPorTipo;
    private JMenuItem miProductosVencidos;
    private JMenuItem miProductosProximosAVencer;
    private JMenuItem miProductosConDescuento;
    private JMenuItem miProductosPorCategoria;
    private JMenuItem miProductosPorMarca;
    private JMenuItem miProductosPorPeriodo;

    private JMenuItem miProductosPorRangoPrecio;
    private JMenuItem miProductosBusquedaAvanzada;
    private JMenuItem miProveedoresBusquedaAvanzada;
    private JMenuItem miInventariosBusquedaAvanzada;
    private JMenuItem miVerCatalogo;
    private JMenuItem miListarCategorias;
    private JMenuItem miResumenPorCategoria;
    private JMenuItem miListarInventarios;
    private JMenuItem miInventariosConAlerta;
    private JMenuItem miListarProveedores;
    private JMenuItem miListarVentas;
    private JMenuItem miVentasBusquedaAvanzada;
    private JMenuItem miListarCompras;
    private JMenuItem miComprasBusquedaAvanzada;
    private JMenuItem miListarFacturas;
    private JMenuItem miFacturasBusquedaAvanzada;
    private JMenuItem miUsuariosBusquedaAvanzada;
    private JMenuItem miListarUsuarios;
    private JMenuItem miListarRoles;

    // ── Constructor ───────────────────────────────────────────────────────────

    public VentanaPrincipal() {
        this(new Control(), null);
    }

    /**
     * Constructor usado tras un login exitoso: reutiliza el {@link Control}
     * ya creado (evita recargar los archivos de datos dos veces) y guarda
     * el {@link Usuario} autenticado para restringir el menú por rol.
     */
    public VentanaPrincipal(Control control, Usuario usuario) {
        super("Sistema de Gestion de Tienda de Viveres");
        this.control       = control;
        this.usuarioActual = usuario;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
            catch (Exception ex) {}
        }

        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblTitulo = new JLabel("Sistema de Gestion de Tienda");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(70, 130, 180));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 5));
        topPanel.add(lblTitulo, BorderLayout.WEST);

        // ── Panel derecho: usuario conectado + boton de cerrar sesion ──────
        JPanel panelSesion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelSesion.setOpaque(false);

        lblUsuario = new JLabel();
        lblUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUsuario.setForeground(new Color(90, 90, 90));
        if (usuarioActual != null) {
            String rolInfo = "";
            if (usuarioActual.getRol() != null) {
                rolInfo = " - Rol: " + usuarioActual.getRol().getNombreCargo();
            }
            lblUsuario.setText("Conectado: " + usuarioActual.getNombre()
                    + " (" + usuarioActual.getPermiso() + ")" + rolInfo);
        }
        panelSesion.add(lblUsuario);

        // ── Indicador de IVA vigente ────────────────────────────────────────
        lblIvaActual = new JLabel();
        lblIvaActual.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblIvaActual.setForeground(new Color(255, 255, 255));
        lblIvaActual.setOpaque(true);
        lblIvaActual.setBackground(new Color(70, 130, 180));
        lblIvaActual.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        actualizarLabelIva();
        panelSesion.add(lblIvaActual);

        btnCerrarSesion = new JButton("Cerrar Sesion");
        btnCerrarSesion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setBackground(new Color(180, 80, 80));
        btnCerrarSesion.setForeground(Color.BLACK);
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 80, 80).darker(), 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        btnCerrarSesion.addActionListener(e -> accionCerrarSesion());
        btnCerrarSesion.setVisible(usuarioActual != null);
        panelSesion.add(btnCerrarSesion);

        topPanel.add(panelSesion, BorderLayout.EAST);
        contentPanel.add(topPanel, BorderLayout.NORTH);

        scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(new Color(245, 245, 245));
        JLabel welcomeLabel = new JLabel(
                "<html><h2 style='color:#4682B4;'>Bienvenido al Sistema de Gestion</h2>" +
                        "<p style='font-size:14px;'>Seleccione una opcion del menu para comenzar</p>" +
                        "<p style='font-size:12px;color:gray;'>Categorias | Productos | Consultas</p></html>"
        );
        welcomePanel.add(welcomeLabel);
        scrollPane.setViewportView(welcomePanel);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        lblEstado = new JLabel("Sistema listo.");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        contentPanel.add(lblEstado, BorderLayout.SOUTH);

        getContentPane().add(contentPanel);
        initMenuBar();
        aplicarRestriccionesPorRol();

        SwingUtilities.invokeLater(() ->
                lblEstado.setText("Sistema listo. " +
                        new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()))
        );
    }

    // ── Verificación de permisos ─────────────────────────────────────────────

    /**
     * Verifica si el usuario actual tiene un permiso específico.
     * @param permiso el permiso a verificar
     * @return true si el usuario tiene el permiso, false en caso contrario
     */
    private boolean tienePermiso(Permiso permiso) {
        if (usuarioActual == null) return false;
        return usuarioActual.tienePermiso(permiso);
    }

    // ── Menú ──────────────────────────────────────────────────────────────────

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(240, 240, 240));

        // ── Menú Catalogos ────────────────────────────────────────────────────
        JMenu menuCatalogos = new JMenu("Catalogos");
        menuCatalogos.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Categorías
        JMenu subCat = new JMenu("Categorias");
        miAgregarCategoria = item("Agregar Categoria");
        miActualizarCategoria = item("Actualizar Categoria");
        miInactivarCategoria = item("Inactivar Categoria");
        subCat.add(miAgregarCategoria);
        subCat.add(miActualizarCategoria);
        subCat.add(miInactivarCategoria);

        // Productos
        JMenu subProductos = new JMenu("Productos");
        miAgregarProducto = item("Agregar Producto");
        miActualizarProducto = item("Actualizar Producto");
        miInactivarProducto = item("Inactivar Producto");
        subProductos.add(miAgregarProducto);
        subProductos.add(miActualizarProducto);
        subProductos.add(miInactivarProducto);

        // Inventarios
        JMenu subInv = new JMenu("Inventarios");
        miAgregarInventario = item("Agregar Inventario");
        miActualizarInventario = item("Actualizar Inventario");
        miInactivarInventario = item("Inactivar Inventario");
        subInv.add(miAgregarInventario);
        subInv.add(miActualizarInventario);
        subInv.add(miInactivarInventario);

        // Proveedores
        subProveedores = new JMenu("Proveedores");
        miAgregarProveedor = item("Agregar Proveedor");
        miActualizarProveedor = item("Actualizar Proveedor");
        miInactivarProveedor = item("Inactivar Proveedor");
        subProveedores.add(miAgregarProveedor);
        subProveedores.add(miActualizarProveedor);
        subProveedores.add(miInactivarProveedor);

        // Ventas
        JMenu subVen = new JMenu("Ventas");
        miAgregarVenta = item("Agregar Venta");
        miActualizarVenta = item("Actualizar Venta");
        miInactivarVenta = item("Inactivar Venta");
        subVen.add(miAgregarVenta);
        subVen.add(miActualizarVenta);
        subVen.add(miInactivarVenta);

        // Compras
        subCompras = new JMenu("Compras");
        miAgregarCompra = item("Agregar Compra");
        miActualizarCompra = item("Actualizar Compra");
        miInactivarCompra = item("Inactivar Compra");
        subCompras.add(miAgregarCompra);
        subCompras.add(miActualizarCompra);
        subCompras.add(miInactivarCompra);

        // Usuarios
        subUsuarios = new JMenu("Usuarios");
        miAgregarUsuario = item("Agregar Usuario");
        miActualizarUsuario = item("Actualizar Usuario");
        miInactivarUsuario = item("Inactivar Usuario");
        subUsuarios.add(miAgregarUsuario);
        subUsuarios.add(miActualizarUsuario);
        subUsuarios.add(miInactivarUsuario);

        // Roles
        subRoles = new JMenu("Roles");
        miAgregarRol = item("Agregar Rol");
        miActualizarRol = item("Actualizar Rol");
        miInactivarRol = item("Inactivar Rol");
        subRoles.add(miAgregarRol);
        subRoles.add(miActualizarRol);
        subRoles.add(miInactivarRol);

        miSalir = item("Salir");
        miSalir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));

        menuCatalogos.add(subCat);
        menuCatalogos.add(subProductos);
        menuCatalogos.add(subInv);
        menuCatalogos.add(subProveedores);
        menuCatalogos.add(subVen);
        menuCatalogos.add(subCompras);
        menuCatalogos.add(subUsuarios);
        menuCatalogos.add(subRoles);
        menuCatalogos.addSeparator();
        menuCatalogos.add(miSalir);

        // ── Menú Consultas ────────────────────────────────────────────────────
        JMenu menuConsultas = new JMenu("Consultas");
        menuConsultas.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Productos — submenú de consultas unificado
        JMenu subConsultaProductos = new JMenu("Productos");
        miListarProductos = item("Todos");
        miProductosPorTipo = item("Por Tipo");
        miProductosVencidos = item("Vencidos");
        miProductosProximosAVencer = item("Proximos a Vencer");
        miProductosConDescuento = item("Con Descuento (>5kg)");
        miProductosPorCategoria = item("Por Categoria");
        miProductosPorMarca = item("Por Marca");
        miProductosPorPeriodo = item("Por Periodo");
        subConsultaProductos.add(miListarProductos);
        subConsultaProductos.add(miProductosPorTipo);
        subConsultaProductos.addSeparator();
        subConsultaProductos.add(miProductosVencidos);
        subConsultaProductos.add(miProductosProximosAVencer);
        subConsultaProductos.add(miProductosConDescuento);
        subConsultaProductos.addSeparator();
        miProductosPorRangoPrecio = item("Por Rango de Precio");
        subConsultaProductos.add(miProductosPorCategoria);
        subConsultaProductos.add(miProductosPorMarca);
        subConsultaProductos.add(miProductosPorPeriodo);
        subConsultaProductos.add(miProductosPorRangoPrecio);

        miVerCatalogo = item("Catalogo Completo");
        miListarCategorias = item("Listar Categorias");
        miResumenPorCategoria = item("Resumen por Categoria");
        miListarInventarios = item("Listar Inventarios");
        miInventariosConAlerta = item("Inventarios con Alerta");
        miListarProveedores = item("Listar Proveedores");
        miListarVentas = item("Listar Ventas");
        miListarCompras = item("Listar Compras");
        miListarFacturas = item("Listar Facturas");
        miListarUsuarios = item("Listar Usuarios");
        miListarRoles = item("Listar Roles");

        menuConsultas.add(subConsultaProductos);
        menuConsultas.addSeparator();
        menuConsultas.add(miVerCatalogo);
        menuConsultas.add(miListarCategorias);
        menuConsultas.add(miResumenPorCategoria);
        menuConsultas.addSeparator();
        menuConsultas.add(miListarInventarios);
        menuConsultas.add(miInventariosConAlerta);
        menuConsultas.addSeparator();
        menuConsultas.add(miListarProveedores);
        menuConsultas.add(miListarVentas);
        menuConsultas.add(miListarCompras);
        menuConsultas.add(miListarFacturas);
        menuConsultas.addSeparator();
        menuConsultas.add(miListarUsuarios);
        menuConsultas.add(miListarRoles);

        // ── Menú Consultas con Filtro ────────────────────────────────────────
        JMenu menuConsultasFiltro = new JMenu("Consultas con Filtro");
        menuConsultasFiltro.setFont(new Font("Segoe UI", Font.BOLD, 13));

        miProductosBusquedaAvanzada = item("Productos - Busqueda Avanzada");
        miProveedoresBusquedaAvanzada = item("Proveedores - Busqueda Avanzada");
        miInventariosBusquedaAvanzada = item("Inventarios - Busqueda Avanzada");
        miVentasBusquedaAvanzada = item("Ventas - Busqueda Avanzada");
        miComprasBusquedaAvanzada = item("Compras - Busqueda Avanzada");
        miFacturasBusquedaAvanzada = item("Facturas - Busqueda Avanzada");
        miUsuariosBusquedaAvanzada = item("Usuarios - Busqueda Avanzada");

        menuConsultasFiltro.add(miProductosBusquedaAvanzada);
        menuConsultasFiltro.add(miProveedoresBusquedaAvanzada);
        menuConsultasFiltro.add(miInventariosBusquedaAvanzada);
        menuConsultasFiltro.add(miVentasBusquedaAvanzada);
        menuConsultasFiltro.add(miComprasBusquedaAvanzada);
        menuConsultasFiltro.add(miFacturasBusquedaAvanzada);
        menuConsultasFiltro.add(miUsuariosBusquedaAvanzada);

        // ── Menú Configuración ────────────────────────────────────────────────
        menuConfiguracion = new JMenu("Configurar IVA");
        menuConfiguracion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        menuConfiguracion.setForeground(Color.BLACK);
        miConfigurarIva = new JMenuItem("Configurar IVA");
        miConfigurarIva.addActionListener(e -> configurarIva());
        menuConfiguracion.add(miConfigurarIva);

        // ── Menú Ayuda ────────────────────────────────────────────────────────
        JMenu menuAyuda = new JMenu("Ayuda");
        menuAyuda.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JMenuItem miAcerca = new JMenuItem("Acerca de");
        miAcerca.addActionListener(e -> mostrarAcercaDe());
        menuAyuda.add(miAcerca);

        JMenuItem miAyuda = new JMenuItem("Ayuda");
        miAyuda.addActionListener(e -> mostrarAyuda());
        miAyuda.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        menuAyuda.add(miAyuda);

        menuBar.add(menuCatalogos);
        menuBar.add(menuConsultas);
        menuBar.add(menuConsultasFiltro);
        menuBar.add(menuConfiguracion);
        menuBar.add(menuAyuda);
        setJMenuBar(menuBar);
    }

    private JMenuItem item(String texto) {
        JMenuItem mi = new JMenuItem(texto);
        mi.addActionListener(this);
        mi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return mi;
    }

    // ── Restricciones por rol ────────────────────────────────────────────────

    /**
     * Restringe el menú según el rol del usuario autenticado.
     * Utiliza el sistema de permisos dinámicos basado en roles.
     */
    private void aplicarRestriccionesPorRol() {
        if (usuarioActual == null) return;

        // ── Verificar permisos individuales ──────────────────────────────────

        // Categorías
        boolean puedeGestionarCategorias = tienePermiso(Permiso.GESTIONAR_CATEGORIAS);
        miAgregarCategoria.setVisible(puedeGestionarCategorias);
        miActualizarCategoria.setVisible(puedeGestionarCategorias);
        miInactivarCategoria.setVisible(puedeGestionarCategorias);

        // Productos
        boolean puedeGestionarProductos = tienePermiso(Permiso.GESTIONAR_PRODUCTOS);
        miAgregarProducto.setVisible(puedeGestionarProductos);
        miActualizarProducto.setVisible(puedeGestionarProductos);
        miInactivarProducto.setVisible(puedeGestionarProductos);

        // Inventarios
        boolean puedeGestionarInventario = tienePermiso(Permiso.GESTIONAR_INVENTARIO);
        miAgregarInventario.setVisible(puedeGestionarInventario);
        miActualizarInventario.setVisible(puedeGestionarInventario);
        miInactivarInventario.setVisible(puedeGestionarInventario);

        // Proveedores
        boolean puedeGestionarProveedores = tienePermiso(Permiso.GESTIONAR_PROVEEDORES);
        miAgregarProveedor.setVisible(puedeGestionarProveedores);
        miActualizarProveedor.setVisible(puedeGestionarProveedores);
        miInactivarProveedor.setVisible(puedeGestionarProveedores);
        subProveedores.setVisible(puedeGestionarProveedores);

        // Ventas
        boolean puedeGestionarVentas = tienePermiso(Permiso.GESTIONAR_VENTAS);
        miAgregarVenta.setVisible(puedeGestionarVentas);
        miActualizarVenta.setVisible(puedeGestionarVentas);
        miInactivarVenta.setVisible(puedeGestionarVentas);

        // Compras
        boolean puedeGestionarCompras = tienePermiso(Permiso.GESTIONAR_COMPRAS);
        miAgregarCompra.setVisible(puedeGestionarCompras);
        miActualizarCompra.setVisible(puedeGestionarCompras);
        miInactivarCompra.setVisible(puedeGestionarCompras);
        subCompras.setVisible(puedeGestionarCompras);

        // Usuarios
        boolean puedeGestionarUsuarios = tienePermiso(Permiso.GESTIONAR_USUARIOS);
        miAgregarUsuario.setVisible(puedeGestionarUsuarios);
        miActualizarUsuario.setVisible(puedeGestionarUsuarios);
        miInactivarUsuario.setVisible(puedeGestionarUsuarios);
        subUsuarios.setVisible(puedeGestionarUsuarios);

        // Roles
        boolean puedeGestionarRoles = tienePermiso(Permiso.GESTIONAR_ROLES);
        miAgregarRol.setVisible(puedeGestionarRoles);
        miActualizarRol.setVisible(puedeGestionarRoles);
        miInactivarRol.setVisible(puedeGestionarRoles);
        subRoles.setVisible(puedeGestionarRoles);

        // Reportes
        boolean puedeVerReportes = tienePermiso(Permiso.VER_REPORTES);
        miResumenPorCategoria.setVisible(puedeVerReportes);

        // Configuración del sistema (IVA)
        boolean puedeConfigurar = tienePermiso(Permiso.CONFIGURAR_SISTEMA);
        menuConfiguracion.setVisible(puedeConfigurar);
    }

    // ── Cerrar sesion ─────────────────────────────────────────────────────────

    private void accionCerrarSesion() {
        int ok = JOptionPane.showConfirmDialog(this,
                "Esta seguro de cerrar la sesion actual?", "Cerrar Sesion",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        dispose();
        SwingUtilities.invokeLater(() -> mostrarLogin(control));
    }

    public static void mostrarLogin(Control control) {
        DlgLogin login = new DlgLogin(null, control);
        login.setVisible(true);

        Usuario usuario = login.getUsuarioAutenticado();
        if (usuario == null) {
            System.exit(0);
            return;
        }

        VentanaPrincipal ventana = new VentanaPrincipal(control, usuario);
        ventana.setVisible(true);
    }

    // ── Eventos ───────────────────────────────────────────────────────────────

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        // Categorías
        if (src == miAgregarCategoria) { control.agregarCategoria(this); accionListarCategorias(); }
        else if (src == miActualizarCategoria) { control.actualizarCategoria(this); accionListarCategorias(); }
        else if (src == miInactivarCategoria) { control.inactivarCategoria(this); accionListarCategorias(); }

        // Productos CRUD
        else if (src == miAgregarProducto) { control.agregarProducto(this); accionListarProductos(); }
        else if (src == miActualizarProducto) { control.actualizarProducto(this); accionListarProductos(); }
        else if (src == miInactivarProducto) { control.inactivarProducto(this); accionListarProductos(); }

        // Inventarios
        else if (src == miAgregarInventario) { control.agregarInventario(this); accionListarInventarios(); }
        else if (src == miActualizarInventario) { control.actualizarInventario(this); accionListarInventarios(); }
        else if (src == miInactivarInventario) { control.inactivarInventario(this); accionListarInventarios(); }

        // Proveedores
        else if (src == miAgregarProveedor) { control.agregarProveedor(this); accionListarProveedores(); }
        else if (src == miActualizarProveedor) { control.actualizarProveedor(this); accionListarProveedores(); }
        else if (src == miInactivarProveedor) { control.inactivarProveedor(this); accionListarProveedores(); }

        // Ventas
        else if (src == miAgregarVenta) { control.agregarVenta(this); accionListarVentas(); }
        else if (src == miActualizarVenta) { control.actualizarVenta(this); accionListarVentas(); }
        else if (src == miInactivarVenta) { control.inactivarVenta(this); accionListarVentas(); }

        // Compras
        else if (src == miAgregarCompra) { control.agregarCompra(this); accionListarCompras(); }
        else if (src == miActualizarCompra) { control.actualizarCompra(this); accionListarCompras(); }
        else if (src == miInactivarCompra) { control.inactivarCompra(this); accionListarCompras(); }

        // Usuarios
        else if (src == miAgregarUsuario) { control.agregarUsuario(this); accionListarUsuarios(); }
        else if (src == miActualizarUsuario) { control.actualizarUsuario(this); accionListarUsuarios(); }
        else if (src == miInactivarUsuario) { control.inactivarUsuario(this); accionListarUsuarios(); }

        // Roles
        else if (src == miAgregarRol) { control.agregarRol(this); accionListarRoles(); }
        else if (src == miActualizarRol) { control.actualizarRol(this); accionListarRoles(); }
        else if (src == miInactivarRol) { control.inactivarRol(this); accionListarRoles(); }

        // Consultas de Productos
        else if (src == miListarProductos) accionListarProductos();
        else if (src == miProductosPorTipo) accionProductosPorTipo();
        else if (src == miProductosVencidos) mostrarTabla(control.getTablaVencidos(this), "vencido(s)");
        else if (src == miProductosProximosAVencer) mostrarTabla(control.getTablaProximosAVencer(this), "proximo(s) a vencer");
        else if (src == miProductosConDescuento) mostrarTabla(control.getTablaConDescuento(this), "con descuento");
        else if (src == miProductosPorCategoria) accionProductosPorCategoria();
        else if (src == miProductosPorMarca) accionProductosPorMarca();
        else if (src == miProductosPorRangoPrecio) accionProductosPorRangoPrecio();
        else if (src == miProductosPorPeriodo) accionProductosPorPeriodo();

            // Otras consultas
        else if (src == miVerCatalogo) accionVerCatalogo();
        else if (src == miListarCategorias) accionListarCategorias();
        else if (src == miResumenPorCategoria) accionResumenPorCategoria();
        else if (src == miListarInventarios) accionListarInventarios();
        else if (src == miInventariosConAlerta) accionInventariosConAlerta();
        else if (src == miListarProveedores) accionListarProveedores();
        else if (src == miListarVentas) accionListarVentas();
        else if (src == miListarCompras) accionListarCompras();
        else if (src == miListarFacturas) accionListarFacturas();
        else if (src == miListarUsuarios) accionListarUsuarios();
        else if (src == miListarRoles) accionListarRoles();

            // Consultas con Filtro
        else if (src == miProductosBusquedaAvanzada) accionProductosBusquedaAvanzada();
        else if (src == miProveedoresBusquedaAvanzada) accionProveedoresBusquedaAvanzada();
        else if (src == miInventariosBusquedaAvanzada) accionInventariosBusquedaAvanzada();
        else if (src == miVentasBusquedaAvanzada) accionVentasBusquedaAvanzada();
        else if (src == miComprasBusquedaAvanzada) accionComprasBusquedaAvanzada();
        else if (src == miFacturasBusquedaAvanzada) accionFacturasBusquedaAvanzada();
        else if (src == miUsuariosBusquedaAvanzada) accionUsuariosBusquedaAvanzada();

        else if (src == miSalir) {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Esta seguro de salir?", "Confirmar salida",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) System.exit(0);
        }
    }

    // ── Acciones de productos ─────────────────────────────────────────────────

    private void accionListarProductos() {
        mostrarTabla(control.getTablaProductos(this), "producto(s)");
    }

    private void accionProductosPorTipo() {
        String[] opciones = {"Perecible", "No Perecible"};
        String tipo = (String) JOptionPane.showInputDialog(this,
                "Seleccione el tipo:", "Filtrar por Tipo",
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (tipo != null)
            mostrarTabla(control.getTablaProductosPorTipo(this, tipo), "producto(s) - Tipo: " + tipo);
    }

    private void accionProductosPorCategoria() {
        String clave = JOptionPane.showInputDialog(this,
                "Ingrese la clave de la categoria:", "Buscar por Categoria",
                JOptionPane.QUESTION_MESSAGE);
        if (clave == null || clave.trim().isEmpty()) return;
        mostrarTabla(control.getTablaProductosPorCategoria(this, clave.trim()),
                "producto(s) - Cat: " + clave.trim());
    }

    private void accionProductosPorMarca() {
        String marca = JOptionPane.showInputDialog(this,
                "Ingrese la marca:", "Buscar por Marca",
                JOptionPane.QUESTION_MESSAGE);
        if (marca == null || marca.trim().isEmpty()) return;
        mostrarTabla(control.getTablaProductosPorMarca(this, marca.trim()),
                "producto(s) - Marca: " + marca.trim());
    }

    private void accionProductosPorRangoPrecio() {
        String minStr = JOptionPane.showInputDialog(this,
                "Precio mínimo:", "Buscar por Rango de Precio", JOptionPane.QUESTION_MESSAGE);
        if (minStr == null || minStr.trim().isEmpty()) return;

        String maxStr = JOptionPane.showInputDialog(this,
                "Precio máximo:", "Buscar por Rango de Precio", JOptionPane.QUESTION_MESSAGE);
        if (maxStr == null || maxStr.trim().isEmpty()) return;

        try {
            double min = Double.parseDouble(minStr.trim());
            double max = Double.parseDouble(maxStr.trim());
            mostrarTabla(control.getTablaProductosPorRangoPrecio(this, min, max),
                    "producto(s) - Precio entre " + min + " y " + max);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese valores numéricos válidos.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionProductosPorPeriodo() {
        Tabla t = control.getTablaProductosPorPeriodo(this);
        if (t != null) mostrarTabla(t, "producto(s) - Periodo");
    }

    // ── Búsquedas Avanzadas ───────────────────────────────────────────────────

    private void accionProductosBusquedaAvanzada() {
        JTextField txtNombre = new JTextField();
        JTextField txtCategoria = new JTextField();
        JTextField txtMarca = new JTextField();
        JTextField txtPrecioMin = new JTextField();
        JTextField txtPrecioMax = new JTextField();
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{"(Cualquiera)", "Perecible", "No Perecible"});
        JCheckBox chkSoloActivos = new JCheckBox("Solo activos", true);
        JCheckBox chkSoloConDescuento = new JCheckBox("Solo con descuento vigente");

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Nombre contiene:")); panel.add(txtNombre);
        panel.add(new JLabel("Clave de categoria:")); panel.add(txtCategoria);
        panel.add(new JLabel("Tipo:")); panel.add(cbTipo);
        panel.add(new JLabel("Marca:")); panel.add(txtMarca);
        panel.add(new JLabel("Precio minimo:")); panel.add(txtPrecioMin);
        panel.add(new JLabel("Precio maximo:")); panel.add(txtPrecioMax);
        panel.add(chkSoloActivos); panel.add(chkSoloConDescuento);

        int ok = JOptionPane.showConfirmDialog(this, panel,
                "Busqueda Avanzada de Productos",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            CriteriosProducto criterios = new CriteriosProducto()
                    .nombre(txtNombre.getText())
                    .categoria(txtCategoria.getText())
                    .tipo(cbTipo.getSelectedIndex() == 0 ? null : (String) cbTipo.getSelectedItem())
                    .marca(txtMarca.getText())
                    .precioMin(parseOpcional(txtPrecioMin.getText()))
                    .precioMax(parseOpcional(txtPrecioMax.getText()))
                    .soloActivos(chkSoloActivos.isSelected())
                    .soloConDescuento(chkSoloConDescuento.isSelected());

            if (criterios.estaVacio()) {
                JOptionPane.showMessageDialog(this,
                        "Ingrese al menos un criterio de busqueda.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            mostrarTabla(control.getTablaProductosPorCriterios(this, criterios),
                    "producto(s) - Busqueda Avanzada");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese valores numericos validos en el rango de precio.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionUsuariosBusquedaAvanzada() {
        JTextField txtTexto = new JTextField(20);
        txtTexto.setToolTipText("Nombre o email (busqueda parcial)");

        JComboBox<String> cmbRol = new JComboBox<>();
        cmbRol.addItem("(Cualquiera)");
        cmbRol.addItem("ADMINISTRADOR");
        cmbRol.addItem("CAJERO");
        for (edu.uce.programacion2.tienda.negocio.Rol r : control.getFachada().listarRolesActivos())
            cmbRol.addItem(r.getNombreCargo());

        JCheckBox chkSoloActivos = new JCheckBox("Solo activos", true);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Nombre o email contiene:")); panel.add(txtTexto);
        panel.add(new JLabel("Rol:")); panel.add(cmbRol);
        panel.add(chkSoloActivos); panel.add(new JLabel());

        int ok = JOptionPane.showConfirmDialog(this, panel,
                "Busqueda Avanzada de Usuarios",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        CriteriosUsuario criterios = new CriteriosUsuario()
                .texto(txtTexto.getText())
                .rol(cmbRol.getSelectedIndex() == 0 ? null : (String) cmbRol.getSelectedItem())
                .soloActivos(chkSoloActivos.isSelected());

        if (criterios.estaVacio()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese al menos un criterio de busqueda.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        mostrarTabla(control.getTablaUsuariosPorCriterios(this, criterios),
                "usuario(s) - Busqueda Avanzada");
    }

    private void accionProveedoresBusquedaAvanzada() {
        JTextField txtNombre = new JTextField();
        JTextField txtRuc = new JTextField();
        JTextField txtTelefono = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtDireccion = new JTextField();
        JCheckBox chkSoloActivos = new JCheckBox("Solo activos", true);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Nombre contiene:")); panel.add(txtNombre);
        panel.add(new JLabel("RUC contiene:")); panel.add(txtRuc);
        panel.add(new JLabel("Telefono contiene:")); panel.add(txtTelefono);
        panel.add(new JLabel("Email contiene:")); panel.add(txtEmail);
        panel.add(new JLabel("Direccion contiene:")); panel.add(txtDireccion);
        panel.add(chkSoloActivos); panel.add(new JLabel());

        int ok = JOptionPane.showConfirmDialog(this, panel,
                "Busqueda Avanzada de Proveedores",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        CriteriosProveedor criterios = new CriteriosProveedor()
                .nombre(txtNombre.getText())
                .ruc(txtRuc.getText())
                .telefono(txtTelefono.getText())
                .email(txtEmail.getText())
                .direccion(txtDireccion.getText())
                .soloActivos(chkSoloActivos.isSelected());

        if (criterios.estaVacio()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese al menos un criterio de busqueda.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        mostrarTabla(control.getTablaProveedoresPorCriterios(this, criterios),
                "proveedor(es) - Busqueda Avanzada");
    }

    private void accionInventariosBusquedaAvanzada() {
        JTextField txtProducto = new JTextField();
        JTextField txtCategoria = new JTextField();
        JTextField txtStockMin = new JTextField();
        JTextField txtStockMax = new JTextField();
        JTextField txtFechaDesde = new JTextField();
        JTextField txtFechaHasta = new JTextField();
        JCheckBox chkSoloConAlerta = new JCheckBox("Solo con alerta de stock");
        JCheckBox chkSoloActivos = new JCheckBox("Solo activos", true);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Producto (nombre o codigo):")); panel.add(txtProducto);
        panel.add(new JLabel("Clave de categoria:")); panel.add(txtCategoria);
        panel.add(new JLabel("Stock minimo:")); panel.add(txtStockMin);
        panel.add(new JLabel("Stock maximo:")); panel.add(txtStockMax);
        panel.add(new JLabel("Fecha actualizacion desde (dd/MM/yyyy):")); panel.add(txtFechaDesde);
        panel.add(new JLabel("Fecha actualizacion hasta (dd/MM/yyyy):")); panel.add(txtFechaHasta);
        panel.add(chkSoloConAlerta); panel.add(chkSoloActivos);

        int ok = JOptionPane.showConfirmDialog(this, panel,
                "Busqueda Avanzada de Inventarios",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            CriteriosInventario criterios = new CriteriosInventario()
                    .producto(txtProducto.getText())
                    .categoria(txtCategoria.getText())
                    .stockMin(parseEnteroOpcional(txtStockMin.getText()))
                    .stockMax(parseEnteroOpcional(txtStockMax.getText()))
                    .fechaDesde(parseFechaOpcional(txtFechaDesde.getText()))
                    .fechaHasta(parseFechaOpcional(txtFechaHasta.getText()))
                    .soloConAlerta(chkSoloConAlerta.isSelected())
                    .soloActivos(chkSoloActivos.isSelected());

            if (criterios.estaVacio()) {
                JOptionPane.showMessageDialog(this,
                        "Ingrese al menos un criterio de busqueda.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            mostrarTabla(control.getTablaInventariosPorCriterios(this, criterios),
                    "inventario(s) - Busqueda Avanzada");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese valores numericos validos en el rango de stock.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese fechas validas en formato dd/MM/yyyy.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionVentasBusquedaAvanzada() {
        JTextField txtFechaDesde = new JTextField();
        JTextField txtFechaHasta = new JTextField();
        JTextField txtCliente = new JTextField();
        JTextField txtMetodoPago = new JTextField();
        JTextField txtTotalMin = new JTextField();
        JTextField txtTotalMax = new JTextField();
        JTextField txtProducto = new JTextField();
        JTextField txtCategoria = new JTextField();
        JCheckBox chkSoloConDescuento = new JCheckBox("Solo con descuento aplicado");

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Fecha desde (dd/MM/yyyy):")); panel.add(txtFechaDesde);
        panel.add(new JLabel("Fecha hasta (dd/MM/yyyy):")); panel.add(txtFechaHasta);
        panel.add(new JLabel("Cliente (nombre o email):")); panel.add(txtCliente);
        panel.add(new JLabel("Metodo de pago:")); panel.add(txtMetodoPago);
        panel.add(new JLabel("Total minimo:")); panel.add(txtTotalMin);
        panel.add(new JLabel("Total maximo:")); panel.add(txtTotalMax);
        panel.add(new JLabel("Producto (nombre o codigo):")); panel.add(txtProducto);
        panel.add(new JLabel("Clave de categoria:")); panel.add(txtCategoria);
        panel.add(chkSoloConDescuento); panel.add(new JLabel());

        int ok = JOptionPane.showConfirmDialog(this, panel,
                "Busqueda Avanzada de Ventas",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            CriteriosVenta criterios = new CriteriosVenta()
                    .fechaDesde(parseFechaOpcional(txtFechaDesde.getText()))
                    .fechaHasta(parseFechaOpcional(txtFechaHasta.getText()))
                    .cliente(txtCliente.getText())
                    .metodoPago(txtMetodoPago.getText())
                    .totalMin(parseOpcional(txtTotalMin.getText()))
                    .totalMax(parseOpcional(txtTotalMax.getText()))
                    .soloConDescuento(chkSoloConDescuento.isSelected())
                    .producto(txtProducto.getText())
                    .categoria(txtCategoria.getText());

            if (criterios.estaVacio()) {
                JOptionPane.showMessageDialog(this,
                        "Ingrese al menos un criterio de busqueda.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            mostrarTabla(control.getTablaVentasPorCriterios(this, criterios),
                    "venta(s) - Busqueda Avanzada");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese valores numericos validos en el rango de total.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese fechas validas en formato dd/MM/yyyy.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionComprasBusquedaAvanzada() {
        JTextField txtProveedor = new JTextField(20);
        txtProveedor.setToolTipText("Nombre o RUC del proveedor");
        JTextField txtFechaDesde = new JTextField(12);
        txtFechaDesde.setToolTipText("Formato: dd/MM/yyyy");
        JTextField txtFechaHasta = new JTextField(12);
        txtFechaHasta.setToolTipText("Formato: dd/MM/yyyy");
        JTextField txtTotalMin = new JTextField(8);
        txtTotalMin.setToolTipText("Total mínimo");
        JTextField txtTotalMax = new JTextField(8);
        txtTotalMax.setToolTipText("Total máximo");
        JTextField txtProducto = new JTextField(20);
        txtProducto.setToolTipText("Nombre o código del producto");
        JTextField txtCategoria = new JTextField(15);
        txtCategoria.setToolTipText("Clave de la categoría");
        JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"(Cualquiera)", "PENDIENTE", "RECIBIDA", "ANULADA"});
        cmbEstado.setEditable(false);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Proveedor (nombre o RUC):")); panel.add(txtProveedor);
        panel.add(new JLabel("Fecha desde (dd/MM/yyyy):")); panel.add(txtFechaDesde);
        panel.add(new JLabel("Fecha hasta (dd/MM/yyyy):")); panel.add(txtFechaHasta);
        panel.add(new JLabel("Total minimo:")); panel.add(txtTotalMin);
        panel.add(new JLabel("Total maximo:")); panel.add(txtTotalMax);
        panel.add(new JLabel("Producto (nombre o codigo):")); panel.add(txtProducto);
        panel.add(new JLabel("Clave de categoria:")); panel.add(txtCategoria);
        panel.add(new JLabel("Estado:")); panel.add(cmbEstado);

        int ok = JOptionPane.showConfirmDialog(this, panel,
                "Busqueda Avanzada de Compras",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            CriteriosCompra criterios = new CriteriosCompra()
                    .proveedor(txtProveedor.getText())
                    .fechaDesde(parseFechaOpcional(txtFechaDesde.getText()))
                    .fechaHasta(parseFechaOpcional(txtFechaHasta.getText()))
                    .totalMin(parseOpcional(txtTotalMin.getText()))
                    .totalMax(parseOpcional(txtTotalMax.getText()))
                    .producto(txtProducto.getText())
                    .categoria(txtCategoria.getText())
                    .estado(obtenerEstadoCompraSeleccionado(cmbEstado));

            if (criterios.estaVacio()) {
                JOptionPane.showMessageDialog(this,
                        "Ingrese al menos un criterio de busqueda.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            mostrarTabla(control.getTablaComprasPorCriterios(this, criterios),
                    "compra(s) - Busqueda Avanzada");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese valores numericos validos en el rango de total.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese fechas validas en formato dd/MM/yyyy.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionFacturasBusquedaAvanzada() {
        JTextField txtFechaDesde = new JTextField(12);
        txtFechaDesde.setToolTipText("Formato: dd/MM/yyyy");
        JTextField txtFechaHasta = new JTextField(12);
        txtFechaHasta.setToolTipText("Formato: dd/MM/yyyy");
        JTextField txtTotalMin = new JTextField(8);
        txtTotalMin.setToolTipText("Total mínimo");
        JTextField txtTotalMax = new JTextField(8);
        txtTotalMax.setToolTipText("Total máximo");
        JTextField txtCliente = new JTextField(20);
        txtCliente.setToolTipText("Nombre o email del cliente");
        JTextField txtNumeroFactura = new JTextField(15);
        txtNumeroFactura.setToolTipText("Número de factura");
        JCheckBox chkSoloConIvaEspecial = new JCheckBox("Solo con IVA especial");
        JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"(Cualquiera)", "EMITIDA", "PAGADA", "ANULADA"});
        cmbEstado.setEditable(false);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Fecha desde (dd/MM/yyyy):")); panel.add(txtFechaDesde);
        panel.add(new JLabel("Fecha hasta (dd/MM/yyyy):")); panel.add(txtFechaHasta);
        panel.add(new JLabel("Total minimo:")); panel.add(txtTotalMin);
        panel.add(new JLabel("Total maximo:")); panel.add(txtTotalMax);
        panel.add(new JLabel("Cliente (nombre o email):")); panel.add(txtCliente);
        panel.add(new JLabel("Numero de factura:")); panel.add(txtNumeroFactura);
        panel.add(new JLabel("Estado:")); panel.add(cmbEstado);
        panel.add(chkSoloConIvaEspecial); panel.add(new JLabel());

        int ok = JOptionPane.showConfirmDialog(this, panel,
                "Busqueda Avanzada de Facturas",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            CriteriosFactura criterios = new CriteriosFactura()
                    .fechaDesde(parseFechaOpcional(txtFechaDesde.getText()))
                    .fechaHasta(parseFechaOpcional(txtFechaHasta.getText()))
                    .totalMin(parseOpcional(txtTotalMin.getText()))
                    .totalMax(parseOpcional(txtTotalMax.getText()))
                    .cliente(txtCliente.getText())
                    .numeroFactura(txtNumeroFactura.getText())
                    .estado(obtenerEstadoFacturaSeleccionado(cmbEstado))
                    .soloConIvaEspecial(chkSoloConIvaEspecial.isSelected());

            if (criterios.estaVacio()) {
                JOptionPane.showMessageDialog(this,
                        "Ingrese al menos un criterio de busqueda.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            mostrarFacturasFiltradas(control.getFachada().buscarFacturas(criterios));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese valores numericos validos en el rango de total.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese fechas validas en formato dd/MM/yyyy.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Double parseOpcional(String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        return Double.parseDouble(texto.trim());
    }

    private Integer parseEnteroOpcional(String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        return Integer.parseInt(texto.trim());
    }

    private java.util.Date parseFechaOpcional(String texto) throws java.text.ParseException {
        if (texto == null || texto.trim().isEmpty()) return null;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        return sdf.parse(texto.trim());
    }

    private String obtenerEstadoCompraSeleccionado(JComboBox<String> cmb) {
        String seleccion = (String) cmb.getSelectedItem();
        if (seleccion == null || "(Cualquiera)".equals(seleccion)) {
            return null;
        }
        return seleccion;
    }

    private String obtenerEstadoFacturaSeleccionado(JComboBox<String> cmb) {
        String seleccion = (String) cmb.getSelectedItem();
        if (seleccion == null || "(Cualquiera)".equals(seleccion)) {
            return null;
        }
        return seleccion;
    }

    // ── Acciones generales ────────────────────────────────────────────────────

    private void accionVerCatalogo() {
        Tabla t = control.getTablaCatalogo(this);
        if (t == null) return;
        scrollPane.setViewportView(new PanelTabla("Catalogo Completo", t));
        lblEstado.setText((t.getFilas() != null ? t.getFilas().length : 0) + " producto(s) en catalogo.");
    }

    private void accionListarCategorias() {
        Tabla t = control.getTablaCategorias(this);
        if (t == null) return;
        scrollPane.setViewportView(new PanelTabla("Categorias", t));
        lblEstado.setText((t.getFilas() != null ? t.getFilas().length : 0) + " categoria(s).");
    }

    private void accionResumenPorCategoria() {
        JOptionPane.showMessageDialog(this,
                control.getResumenPorCategoria(this),
                "Resumen por Categoría",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void accionListarInventarios() {
        Tabla t = control.getTablaInventarios(this);
        if (t == null) return;
        scrollPane.setViewportView(new PanelTabla("Inventarios", t));
        lblEstado.setText((t.getFilas() != null ? t.getFilas().length : 0) + " inventario(s).");
    }

    private void accionInventariosConAlerta() {
        Tabla t = control.getTablaInventariosConAlerta(this);
        if (t == null) return;
        scrollPane.setViewportView(new PanelTabla("Inventarios con Alerta", t));
        lblEstado.setText((t.getFilas() != null ? t.getFilas().length : 0) + " inventario(s) con alerta.");
    }

    private void accionListarProveedores() {
        Tabla t = control.getTablaProveedores(this);
        if (t == null) return;
        scrollPane.setViewportView(new PanelTabla("Proveedores", t));
        lblEstado.setText((t.getFilas() != null ? t.getFilas().length : 0) + " proveedor(es).");
    }

    private void accionListarVentas() {
        Tabla t = control.getTablaVentas(this);
        if (t == null) return;
        scrollPane.setViewportView(new PanelTabla("Ventas", t));
        lblEstado.setText((t.getFilas() != null ? t.getFilas().length : 0) + " venta(s).");
    }

    private void accionListarCompras() {
        Tabla t = control.getTablaCompras(this);
        if (t == null) return;
        scrollPane.setViewportView(new PanelTabla("Compras", t));
        lblEstado.setText((t.getFilas() != null ? t.getFilas().length : 0) + " compra(s).");
    }

    private void accionListarFacturas() {
        java.util.ArrayList<edu.uce.programacion2.tienda.negocio.Factura> lista =
                control.getFachada().listarFacturas();
        scrollPane.setViewportView(new PanelListaFacturas(lista));
        lblEstado.setText(lista.size() + " factura(s).");
    }

    private void accionListarUsuarios() {
        Tabla t = control.getTablaUsuarios(this);
        if (t == null) return;
        scrollPane.setViewportView(new PanelTabla("Usuarios", t));
        lblEstado.setText((t.getFilas() != null ? t.getFilas().length : 0) + " usuario(s).");
    }

    private void accionListarRoles() {
        Tabla t = control.getTablaRoles(this);
        if (t == null) return;
        scrollPane.setViewportView(new PanelTabla("Roles", t));
        lblEstado.setText((t.getFilas() != null ? t.getFilas().length : 0) + " rol(es).");
    }

    // ── Utilitario ────────────────────────────────────────────────────────────

    private void mostrarTabla(Tabla t, String sufijo) {
        if (t == null) return;
        scrollPane.setViewportView(new PanelTabla(null, t));
        int n = t.getFilas() != null ? t.getFilas().length : 0;
        lblEstado.setText(n + " " + sufijo + " encontrado(s). " +
                new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
    }

    /**
     * Muestra el resultado de una busqueda avanzada de facturas usando la
     * misma vista maestro-detalle (lista + factura) que "Listar Facturas",
     * en lugar de la tabla plana generica.
     */
    private void mostrarFacturasFiltradas(java.util.ArrayList<edu.uce.programacion2.tienda.negocio.Factura> lista) {
        if (lista == null || lista.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No se encontraron facturas con esos criterios.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        scrollPane.setViewportView(new PanelListaFacturas(lista));
        lblEstado.setText(lista.size() + " factura(s) - Busqueda Avanzada. " +
                new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
    }

    private void actualizarLabelIva() {
        double porcentaje = edu.uce.programacion2.tienda.objetosServicio.Dinero.getIva() * 100;
        lblIvaActual.setText("IVA vigente: " + formatearPorcentaje(porcentaje) + "%");
    }

    private String formatearPorcentaje(double porcentaje) {
        if (porcentaje == Math.rint(porcentaje)) {
            return String.valueOf((int) porcentaje);
        }
        return String.valueOf(porcentaje);
    }

    private void mostrarAcercaDe() {
        JOptionPane.showMessageDialog(this,
                "Sistema de Gestion de Tienda de Viveres\n" +
                        "Desarrollado con Java Swing\n2026",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }

    private void configurarIva() {
        double actual = edu.uce.programacion2.tienda.objetosServicio.Dinero.getIva() * 100;
        String entrada = JOptionPane.showInputDialog(
                this,
                "Ingrese el nuevo porcentaje de IVA (ejemplo: 15 para 15%):",
                "Configurar IVA",
                JOptionPane.QUESTION_MESSAGE);

        if (entrada == null) return;

        try {
            double nuevoPorcentaje = Double.parseDouble(entrada.trim());
            edu.uce.programacion2.tienda.objetosServicio.Dinero.setIva(nuevoPorcentaje / 100.0);
            actualizarLabelIva();
            JOptionPane.showMessageDialog(this,
                    "IVA actualizado correctamente a " + nuevoPorcentaje + "%.",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Debe ingresar un numero valido (ejemplo: 15).",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarAyuda() {
        JOptionPane.showMessageDialog(this,
                "Ayuda del Sistema\n\n" +
                        "Catalogos:\n" +
                        "  - Agregar/Actualizar/Inactivar categorias y productos\n\n" +
                        "Consultas:\n" +
                        "  - Listar productos por tipo, categoria, marca, periodo\n" +
                        "  - Ver inventarios y alertas\n\n" +
                        "Consultas con Filtro:\n" +
                        "  - Busqueda avanzada con multiples criterios\n" +
                        "  - Productos, Ventas, Compras y Facturas\n\n" +
                        "Atajos:\n" +
                        "  - Ctrl+Q: Salir\n" +
                        "  - F1: Ayuda\n" +
                        "  - Escape: Cerrar dialogos",
                "Ayuda", JOptionPane.INFORMATION_MESSAGE);
    }
}