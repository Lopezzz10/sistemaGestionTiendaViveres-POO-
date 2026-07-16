package edu.uce.programacion2.tienda.interfaces;

import edu.uce.programacion2.tienda.excepciones.CategoriaNoEncontradaException;
import edu.uce.programacion2.tienda.excepciones.FachadaException;
import edu.uce.programacion2.tienda.excepciones.ProductoNoEncontradoException;
import edu.uce.programacion2.tienda.excepciones.StockInsuficienteException;
import edu.uce.programacion2.tienda.negocio.*;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosCompra;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosFactura;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosProducto;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosInventario;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosProveedor;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosVenta;

import java.util.ArrayList;

/**
 * Interfaz de la fachada del Sistema de Gestión de Tienda de Víveres.
 *
 * Define todas las operaciones que la capa Control puede invocar,
 * independientemente del mecanismo de almacenamiento utilizado.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public interface IFachadaTienda {

    // ── Categorías ────────────────────────────────────────────────────────

    void                 agregarCategoria(Categoria c)      throws FachadaException;
    Categoria            buscarCategoria(String cve)        throws CategoriaNoEncontradaException, FachadaException;
    ArrayList<Categoria> listarCategorias()                 throws FachadaException;
    ArrayList<Categoria> listarCategoriasActivas()           throws FachadaException;
    void                 actualizarCategoria(Categoria c)   throws FachadaException;
    void                 inactivarCategoria(String cve)     throws FachadaException;

    // ── Productos (unificado) ─────────────────────────────────────────────

    void                 agregarProducto(Producto p)                    throws FachadaException;
    Producto             buscarProducto(String codigo)                  throws ProductoNoEncontradoException, FachadaException;
    void                 actualizarProducto(Producto p)                 throws FachadaException;
    void                 inactivarProducto(String codigo)                throws FachadaException;
    ArrayList<Producto>  listarProductos()                              throws FachadaException;
    ArrayList<Producto>  listarProductosActivos()                       throws FachadaException;
    ArrayList<Producto>  listarProductosPorTipo(String tipo);
    ArrayList<Producto>  listarVencidos();
    ArrayList<Producto>  listarProximosAVencer();
    ArrayList<Producto>  listarConDescuento();
    ArrayList<Producto>  listarProductosPorCategoria(String cve);
    ArrayList<Producto>  listarProductosPorMarca(String marca);
    ArrayList<Producto>  buscarProductosPor(java.util.function.Predicate<Producto> condicion);
    ArrayList<Producto>  buscarProductos(CriteriosProducto criterios);
    java.util.Map<String, java.util.List<Producto>> agruparProductosPorCategoria();
    ArrayList<Producto>  listarCatalogo();
    int                  conteoTotal();
    double               calcularPrecioPromedio();
    double               calcularValorTotalInventario();
    ArrayList<Producto>  listarProductosOrdenadosPorPrecio();
    java.util.Map<Boolean, java.util.List<Producto>> particionarPorDescuento();
    java.util.Map<String, Long> contarProductosPorCategoria();
    java.util.Optional<Producto> buscarMasCaro();
    <R> ArrayList<R> extraerCampo(java.util.function.Function<Producto, R> extractor);
    double               calcularTotalDescuentos(ReglaDescuento regla);

    // ── Inventario ────────────────────────────────────────────────────────

    void                  agregarInventario(Inventario inv)                     throws FachadaException;
    Inventario            buscarInventario(int idInventario)                    throws FachadaException;
    void                  actualizarInventario(Inventario inv)                  throws FachadaException;
    void                  inactivarInventario(int idInventario)                 throws FachadaException;
    void                  retirarStock(String codigoProducto, int cantidad)     throws StockInsuficienteException, FachadaException;
    ArrayList<Inventario> listarInventarios();
    ArrayList<Inventario> listarInventariosActivos();
    ArrayList<Inventario> listarInventariosConAlerta();
    ArrayList<Inventario> buscarInventariosPor(java.util.function.Predicate<Inventario> condicion);
    ArrayList<Inventario> buscarInventarios(CriteriosInventario criterios);

    // ── Ventas ────────────────────────────────────────────────────────────

    void             registrarVenta(Venta v)    throws FachadaException;
    Venta            buscarVenta(int idVenta)   throws FachadaException;
    void             actualizarVenta(Venta v)   throws FachadaException;
    void             inactivarVenta(int idVenta) throws FachadaException;
    ArrayList<Venta> listarVentas();
    ArrayList<Venta> listarVentasActivas();
    ArrayList<Venta> listarVentasAnuladas();

    // ── Facturas ──────────────────────────────────────────────────────────

    void               emitirFactura(Factura f)         throws FachadaException;
    Factura            buscarFactura(String numero)     throws FachadaException;
    ArrayList<Factura> listarFacturas();
    ArrayList<Factura> listarFacturasEmitidas();
    ArrayList<Factura> listarFacturasAnuladas();

    /** Búsqueda genérica de facturas por cualquier condición funcional. */
    ArrayList<Factura> buscarFacturasPor(java.util.function.Predicate<Factura> condicion);

    /** Búsqueda avanzada de facturas con múltiples criterios. */
    ArrayList<Factura> buscarFacturas(CriteriosFactura criterios);

    // ── Ventas (búsqueda avanzada - deprecada) ──────────────────────────

    /**
     * Búsqueda avanzada de ventas (deprecada, usar buscarFacturas con CriteriosVenta).
     * @deprecated Use {@link #buscarFacturas(CriteriosVenta)} en su lugar.
     */
    @Deprecated
    ArrayList<Factura> buscarFacturas(CriteriosVenta criterios);

    // ── Proveedores ───────────────────────────────────────────────────────

    void                  agregarProveedor(Proveedor p)         throws FachadaException;
    Proveedor             buscarProveedor(int id)               throws FachadaException;
    Proveedor             buscarProveedorPorRuc(String ruc)     throws FachadaException;
    void                  actualizarProveedor(Proveedor p)      throws FachadaException;
    void                  inactivarProveedor(int idProveedor)   throws FachadaException;
    ArrayList<Proveedor>  listarProveedores();
    ArrayList<Proveedor>  listarProveedoresActivos();
    ArrayList<Proveedor>  buscarProveedoresPor(java.util.function.Predicate<Proveedor> condicion);
    ArrayList<Proveedor>  buscarProveedores(CriteriosProveedor criterios);

    // ── Compras ───────────────────────────────────────────────────────────

    void              registrarCompra(Compra c)     throws FachadaException;
    void              recibirCompra(Compra c)        throws FachadaException;
    void              actualizarCompra(Compra c)     throws FachadaException;
    void              inactivarCompra(int idCompra)  throws FachadaException;
    ArrayList<Compra> listarCompras();
    ArrayList<Compra> listarComprasActivas();
    ArrayList<Compra> listarComprasPendientes();
    ArrayList<Compra> listarComprasRecibidas();

    /** Búsqueda genérica de compras por cualquier condición funcional. */
    ArrayList<Compra> buscarComprasPor(java.util.function.Predicate<Compra> condicion);

    /** Búsqueda avanzada de compras con múltiples criterios. */
    ArrayList<Compra> buscarCompras(CriteriosCompra criterios);

    // ── Usuarios ──────────────────────────────────────────────────────────

    void               agregarUsuario(Usuario u)                throws FachadaException;
    Usuario            buscarUsuario(int idUsuario)             throws FachadaException;
    Usuario            buscarUsuarioPorEmail(String email)      throws FachadaException;
    void               actualizarUsuario(Usuario u)             throws FachadaException;
    void               inactivarUsuario(int idUsuario)          throws FachadaException;
    ArrayList<Usuario> listarUsuarios();
    ArrayList<Usuario> listarUsuariosActivos();
    ArrayList<Usuario> listarUsuariosPorPermiso(String permiso);

    /**
     * Búsqueda avanzada de usuarios: recibe cualquier {@link java.util.function.Predicate}
     * y filtra la lista completa de usuarios con él.
     * Análoga a {@code buscarProductosPor}.
     */
    ArrayList<Usuario> buscarUsuariosPor(java.util.function.Predicate<Usuario> condicion);

    /**
     * Búsqueda avanzada de usuarios: recibe varios parámetros opcionales
     * a la vez (nombre/email parcial, rol, solo activos) empaquetados en
     * {@link edu.uce.programacion2.tienda.objetosServicio.CriteriosUsuario}.
     */
    ArrayList<Usuario> buscarUsuarios(edu.uce.programacion2.tienda.objetosServicio.CriteriosUsuario criterios);

    /** Autentica por email/contraseña y retorna el Usuario (Administrador o Cajero). */
    Usuario            autenticarUsuario(String email, String contrasena) throws FachadaException;

    // ── Roles (cargos dinámicos con permisos) ───────────────────────────────

    void            agregarRol(Rol r)      throws FachadaException;
    Rol             buscarRol(int idRol)   throws FachadaException;
    void            actualizarRol(Rol r)   throws FachadaException;
    void            inactivarRol(int idRol) throws FachadaException;
    ArrayList<Rol>  listarRoles();
    ArrayList<Rol>  listarRolesActivos();

    // ── Configuración del sistema (IVA) ─────────────────────────────────────

    /**
     * Retorna el porcentaje de IVA vigente (como fracción, ej. 0.15 = 15%).
     * Así la capa de interfaz (VentanaPrincipal) no accede directamente a
     * {@link edu.uce.programacion2.tienda.objetosServicio.Dinero}, sino a
     * través de la fachada, igual que el resto de las operaciones.
     */
    double getIvaVigente();

    /**
     * Configura un nuevo porcentaje de IVA (como fracción, ej. 0.12 para
     * 12%). El nuevo valor queda persistido en disco y sigue vigente aunque
     * se reinicie la aplicación.
     * @throws IllegalArgumentException si nuevoIva no está entre 0 y 1.
     */
    void configurarIva(double nuevoIva);
}