package edu.uce.programacion2.tienda.fachadas;

import edu.uce.programacion2.tienda.excepciones.CategoriaNoEncontradaException;
import edu.uce.programacion2.tienda.excepciones.FachadaException;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.excepciones.ProductoNoEncontradoException;
import edu.uce.programacion2.tienda.excepciones.StockInsuficienteException;
import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.interfaces.ReglaDescuento;
import edu.uce.programacion2.tienda.negocio.*;
import edu.uce.programacion2.tienda.objetosServicio.*;
import edu.uce.programacion2.tienda.persistencia.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementación de {@link IFachadaTienda} con persistencia en archivos
 * binarios de ACCESO ALEATORIO (registro de tamaño fijo).
 *
 * A diferencia de la version anterior (que cargaba TODO a listas en
 * memoria al arrancar y volvia a escribir el archivo COMPLETO en cada
 * cambio), esta version delega cada operacion directamente al DAO
 * correspondiente, que hace seek() a la posicion exacta del registro. No
 * hay listas en memoria como fuente de verdad: cada metodo lee/escribe el
 * archivo en el momento en que se le pide.
 *
 * Arquitectura de capas:
 *   VentanaPrincipal / Control
 *          ↓
 *   FachadaArchivos   ← único punto de acceso (implementa IFachadaTienda)
 *          ↓
 *   Categorias / Productos / Inventarios / Proveedores / Roles / Usuarios
 *   Ventas+DetallesVenta / Compras+DetallesCompra / Facturas+DetallesFactura
 *          ↓
 *   AccesoAleatorio (RandomAccessFile, seek por numRegistro * tamRegistro)
 *
 * Manejo de excepciones: los DAOs lanzan {@link PersistenciaException}
 * (checked). Los metodos de {@link IFachadaTienda} que la declaran
 * (mayoria de operaciones CRUD) la re-envuelven en {@link FachadaException}.
 * Los metodos de la interfaz que NO declaran ninguna excepcion (las
 * consultas/listados, heredadas del diseño original en memoria que nunca
 * fallaba) no pueden propagar una excepcion checked: si el DAO falla ahi,
 * se re-envuelve en una {@link RuntimeException} (ver {@link #sinChequeo}).
 *
 * @author Equipo08
 */
public class FachadaArchivos implements IFachadaTienda {

    private static final String NOMBRE_TIENDA = "TiendaViveres";

    // ── DAOs de acceso aleatorio ──────────────────────────────────────────
    private final Categorias  categoriasDAO;
    private final Productos   productosDAO;
    private final Inventarios inventariosDAO;
    private final Proveedores proveedoresDAO;
    private final Roles       rolesDAO;
    private final Usuarios    usuariosDAO;
    private final DetallesVenta   detallesVentaDAO;
    private final Ventas          ventasDAO;
    private final DetallesCompra  detallesCompraDAO;
    private final Compras         comprasDAO;
    private final DetallesFactura detallesFacturaDAO;
    private final Facturas        facturasDAO;

    // ── Constructor ──────────────────────────────────────────────────────

    public FachadaArchivos() throws FachadaException {
        // Los constructores de los DAOs no hacen E/S (solo fijan el nombre
        // de archivo y el tamaño de registro), asi que en principio esto
        // no deberia lanzar FachadaException. Se conserva el "throws" para
        // no romper a Control.java, que ya maneja esta excepcion, y por si
        // en el futuro se agrega alguna validacion de arranque aqui.
        categoriasDAO      = new Categorias();
        productosDAO       = new Productos("productos.dat", categoriasDAO);
        inventariosDAO     = new Inventarios("inventarios.dat", productosDAO);
        proveedoresDAO     = new Proveedores();
        rolesDAO           = new Roles();
        usuariosDAO        = new Usuarios("usuarios.dat", rolesDAO);
        detallesVentaDAO   = new DetallesVenta("detallesVenta.dat", productosDAO);
        ventasDAO          = new Ventas("ventas.dat", detallesVentaDAO);
        detallesCompraDAO  = new DetallesCompra("detallesCompra.dat", productosDAO);
        comprasDAO         = new Compras("compras.dat", detallesCompraDAO, proveedoresDAO, usuariosDAO);
        detallesFacturaDAO = new DetallesFactura("detallesFactura.dat", productosDAO);
        facturasDAO        = new Facturas("facturas.dat", detallesFacturaDAO, ventasDAO, usuariosDAO);
    }

    public static String getNombreTienda() { return NOMBRE_TIENDA; }

    // ── Helpers de manejo de excepciones ─────────────────────────────────

    @FunctionalInterface
    private interface Operacion<T> { T ejecutar() throws PersistenciaException; }

    @FunctionalInterface
    private interface OperacionVoid { void ejecutar() throws PersistenciaException; }

    /** Para metodos de la interfaz que SI declaran throws FachadaException. */
    private <T> T conChecked(Operacion<T> op) throws FachadaException {
        try {
            return op.ejecutar();
        } catch (PersistenciaException pe) {
            throw new FachadaException(pe.getMessage(), pe);
        }
    }

    private void conCheckedVoid(OperacionVoid op) throws FachadaException {
        try {
            op.ejecutar();
        } catch (PersistenciaException pe) {
            throw new FachadaException(pe.getMessage(), pe);
        }
    }

    /**
     * Para metodos de la interfaz que NO declaran ninguna excepcion
     * (consultas/listados). Si el DAO falla, se re-envuelve en una
     * excepcion NO checked para no romper la firma heredada del diseño
     * original en memoria.
     */
    private <T> T sinChequeo(Operacion<T> op) {
        try {
            return op.ejecutar();
        } catch (PersistenciaException pe) {
            throw new RuntimeException(pe.getMessage(), pe);
        }
    }

    // ── Categorías ───────────────────────────────────────────────────────────

    @Override
    public void agregarCategoria(Categoria c) throws FachadaException {
        conCheckedVoid(() -> categoriasDAO.agregar(c));
    }

    @Override
    public Categoria buscarCategoria(String cve)
            throws CategoriaNoEncontradaException, FachadaException {
        try {
            return categoriasDAO.buscar(cve);
        } catch (PersistenciaException pe) {
            throw new CategoriaNoEncontradaException(pe.getMessage());
        }
    }

    @Override
    public ArrayList<Categoria> listarCategorias() throws FachadaException {
        return conChecked(categoriasDAO::obtenerTodas);
    }

    @Override
    public void actualizarCategoria(Categoria c) throws FachadaException {
        conCheckedVoid(() -> categoriasDAO.actualizar(c));
    }

    @Override
    public void inactivarCategoria(String cve) throws FachadaException {
        conCheckedVoid(() -> categoriasDAO.inactivar(cve));
    }

    @Override
    public ArrayList<Categoria> listarCategoriasActivas() throws FachadaException {
        return conChecked(categoriasDAO::listarActivas);
    }

    // ── Productos (unificado) ────────────────────────────────────────────────

    @Override
    public void agregarProducto(Producto p) throws FachadaException {
        conCheckedVoid(() -> productosDAO.agregar(p));
    }

    @Override
    public Producto buscarProducto(String codigo)
            throws ProductoNoEncontradoException, FachadaException {
        try {
            return productosDAO.buscar(codigo);
        } catch (PersistenciaException pe) {
            throw new ProductoNoEncontradoException(pe.getMessage());
        }
    }

    @Override
    public void actualizarProducto(Producto p) throws FachadaException {
        conCheckedVoid(() -> productosDAO.actualizar(p));
    }

    @Override
    public void inactivarProducto(String codigo) throws FachadaException {
        conCheckedVoid(() -> productosDAO.inactivar(codigo));
    }

    @Override
    public ArrayList<Producto> listarProductosActivos() throws FachadaException {
        return conChecked(productosDAO::listarActivos);
    }

    @Override
    public ArrayList<Producto> listarProductos() throws FachadaException {
        return conChecked(productosDAO::obtenerTodos);
    }

    @Override
    public ArrayList<Producto> listarProductosPorTipo(String tipo) {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .filter(p -> p.getTipo().equalsIgnoreCase(tipo))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Producto> listarVencidos() {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .filter(p -> "Perecible".equals(p.getTipo()) && p.estaVencido())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Producto> listarProximosAVencer() {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .filter(p -> "Perecible".equals(p.getTipo()) && p.estaProximoAVencer() && !p.estaVencido())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Producto> listarConDescuento() {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .filter(p -> "No Perecible".equals(p.getTipo()) && p.getPesoKg() > 5.0)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Producto> listarProductosPorCategoria(String cve) {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .filter(p -> p.getCategoria() != null &&
                        p.getCategoria().getCveCategoria().equalsIgnoreCase(cve))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Producto> listarProductosPorMarca(String marca) {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .filter(p -> "No Perecible".equals(p.getTipo()) && p.getMarca().equalsIgnoreCase(marca))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Producto> buscarProductosPor(Predicate<Producto> condicion) {
        return sinChequeo(() -> productosDAO.buscarPor(condicion));
    }

    @Override
    public ArrayList<Producto> buscarProductos(CriteriosProducto criterios) {
        return buscarProductosPor(criterios.aPredicate());
    }

    @Override
    public Map<String, List<Producto>> agruparProductosPorCategoria() {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .filter(p -> p.getCategoria() != null)
                .collect(Collectors.groupingBy(p -> p.getCategoria().getCveCategoria()));
    }

    @Override
    public ArrayList<Producto> listarCatalogo() {
        return sinChequeo(productosDAO::obtenerTodos);
    }

    @Override
    public int conteoTotal() {
        return productosDAO.conteo();
    }

    @Override
    public double calcularPrecioPromedio() {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .mapToDouble(Producto::calcularPrecioFinal)
                .average()
                .orElse(0.0);
    }

    @Override
    public double calcularValorTotalInventario() {
        return sinChequeo(inventariosDAO::obtenerTodos).stream()
                .mapToDouble(inv -> inv.getProducto().calcularPrecioFinal() * inv.getCantidadDisponible())
                .sum();
    }

    @Override
    public ArrayList<Producto> listarProductosOrdenadosPorPrecio() {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .sorted(Comparator.comparingDouble(Producto::calcularPrecioFinal)
                        .thenComparing(Producto::getNombre))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Map<Boolean, List<Producto>> particionarPorDescuento() {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .collect(Collectors.partitioningBy(p -> p.calcularDescuento() > 0));
    }

    @Override
    public Map<String, Long> contarProductosPorCategoria() {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .filter(p -> p.getCategoria() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria().getCveCategoria(),
                        Collectors.counting()));
    }

    @Override
    public Optional<Producto> buscarMasCaro() {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .max(Comparator.comparingDouble(Producto::calcularPrecioFinal));
    }

    @Override
    public <R> ArrayList<R> extraerCampo(Function<Producto, R> extractor) {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .map(extractor)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public double calcularTotalDescuentos(ReglaDescuento regla) {
        return sinChequeo(productosDAO::obtenerTodos).stream()
                .mapToDouble(regla::aplicar)
                .sum();
    }

    // ── Inventario ───────────────────────────────────────────────────────────

    @Override
    public void agregarInventario(Inventario inv) throws FachadaException {
        if (inv.getProducto() != null && existeInventarioParaProducto(inv.getProducto().getCodigo())) {
            throw new FachadaException("Ya existe inventario para: " + inv.getProducto().getCodigo());
        }
        conCheckedVoid(() -> inventariosDAO.agregar(inv));
    }

    private boolean existeInventarioParaProducto(String codigoProducto) {
        try {
            return inventariosDAO.buscarPorProducto(codigoProducto) != null;
        } catch (PersistenciaException pe) {
            return false;
        }
    }

    @Override
    public Inventario buscarInventario(int id) throws FachadaException {
        return conChecked(() -> inventariosDAO.buscar(id));
    }

    @Override
    public void actualizarInventario(Inventario inv) throws FachadaException {
        conCheckedVoid(() -> inventariosDAO.actualizar(inv));
    }

    @Override
    public void inactivarInventario(int id) throws FachadaException {
        conCheckedVoid(() -> inventariosDAO.inactivar(id));
    }

    @Override
    public void retirarStock(String codigoProducto, int cantidad)
            throws StockInsuficienteException, FachadaException {
        Inventario inv;
        try {
            inv = inventariosDAO.buscarPorProducto(codigoProducto);
        } catch (PersistenciaException pe) {
            throw new FachadaException("Inventario no encontrado para: " + codigoProducto, pe);
        }
        inv.retirar(cantidad);
        conCheckedVoid(() -> inventariosDAO.actualizar(inv));
    }

    @Override
    public ArrayList<Inventario> listarInventarios() {
        return sinChequeo(inventariosDAO::obtenerTodos);
    }

    @Override
    public ArrayList<Inventario> listarInventariosActivos() {
        return sinChequeo(inventariosDAO::listarActivos);
    }

    @Override
    public ArrayList<Inventario> listarInventariosConAlerta() {
        return sinChequeo(inventariosDAO::listarConAlerta);
    }

    @Override
    public ArrayList<Inventario> buscarInventariosPor(Predicate<Inventario> condicion) {
        return sinChequeo(inventariosDAO::obtenerTodos).stream()
                .filter(condicion)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Inventario> buscarInventarios(CriteriosInventario criterios) {
        return buscarInventariosPor(criterios.aPredicate());
    }

    // ── Ventas ───────────────────────────────────────────────────────────────

    @Override
    public void registrarVenta(Venta v) throws FachadaException {
        conCheckedVoid(() -> ventasDAO.agregar(v));
        v.crear();
    }

    @Override
    public Venta buscarVenta(int idVenta) throws FachadaException {
        return conChecked(() -> ventasDAO.buscar(idVenta));
    }

    @Override
    public void actualizarVenta(Venta v) throws FachadaException {
        conCheckedVoid(() -> ventasDAO.actualizar(v));
    }

    @Override
    public void inactivarVenta(int idVenta) throws FachadaException {
        conCheckedVoid(() -> ventasDAO.inactivar(idVenta));
    }

    @Override
    public ArrayList<Venta> listarVentas() {
        return sinChequeo(ventasDAO::obtenerTodos);
    }

    @Override
    public ArrayList<Venta> listarVentasActivas() {
        return sinChequeo(ventasDAO::listarActivas);
    }

    @Override
    public ArrayList<Venta> listarVentasAnuladas() {
        return sinChequeo(ventasDAO::listarAnuladas);
    }

    // ── Facturas ─────────────────────────────────────────────────────────────

    @Override
    public void emitirFactura(Factura f) throws FachadaException {
        conCheckedVoid(() -> facturasDAO.agregar(f));
        f.crear();
    }

    /** Retorna las líneas fiscales pertenecientes a una factura específica. */
    public ArrayList<edu.uce.programacion2.tienda.negocio.DetalleFactura> listarDetallesPorFactura(int idFactura) {
        return sinChequeo(() -> detallesFacturaDAO.obtenerPorIdFactura(idFactura));
    }

    @Override
    public Factura buscarFactura(String numero) throws FachadaException {
        return conChecked(() -> facturasDAO.buscar(numero));
    }

    @Override
    public ArrayList<Factura> listarFacturas() {
        return sinChequeo(facturasDAO::obtenerTodos);
    }

    @Override
    public ArrayList<Factura> listarFacturasEmitidas() {
        return sinChequeo(facturasDAO::listarEmitidas);
    }

    @Override
    public ArrayList<Factura> listarFacturasAnuladas() {
        return sinChequeo(facturasDAO::listarAnuladas);
    }

    @Override
    public ArrayList<Factura> buscarFacturasPor(Predicate<Factura> condicion) {
        return sinChequeo(() -> facturasDAO.buscarPor(condicion));
    }

    @Override
    public ArrayList<Factura> buscarFacturas(CriteriosFactura criterios) {
        return buscarFacturasPor(criterios.aPredicate());
    }

    /**
     * Búsqueda avanzada de ventas (deprecada, usar buscarFacturas con CriteriosFactura).
     * @deprecated Use {@link #buscarFacturas(CriteriosFactura)} en su lugar.
     */
    @Override
    @Deprecated
    public ArrayList<Factura> buscarFacturas(CriteriosVenta criterios) {
        return buscarFacturasPor(criterios.aPredicate());
    }

    // ── Proveedores ──────────────────────────────────────────────────────────

    @Override
    public void agregarProveedor(Proveedor p) throws FachadaException {
        conCheckedVoid(() -> proveedoresDAO.agregar(p));
    }

    @Override
    public Proveedor buscarProveedor(int id) throws FachadaException {
        return conChecked(() -> proveedoresDAO.buscar(id));
    }

    @Override
    public Proveedor buscarProveedorPorRuc(String ruc) throws FachadaException {
        return conChecked(() -> proveedoresDAO.buscarPorRuc(ruc));
    }

    @Override
    public ArrayList<Proveedor> listarProveedores() {
        return sinChequeo(proveedoresDAO::obtenerTodos);
    }

    @Override
    public ArrayList<Proveedor> listarProveedoresActivos() {
        return sinChequeo(proveedoresDAO::listarActivos);
    }

    @Override
    public ArrayList<Proveedor> buscarProveedoresPor(Predicate<Proveedor> condicion) {
        return sinChequeo(proveedoresDAO::obtenerTodos).stream()
                .filter(condicion)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Proveedor> buscarProveedores(CriteriosProveedor criterios) {
        return buscarProveedoresPor(criterios.aPredicate());
    }

    @Override
    public void actualizarProveedor(Proveedor p) throws FachadaException {
        conCheckedVoid(() -> proveedoresDAO.actualizar(p));
    }

    @Override
    public void inactivarProveedor(int id) throws FachadaException {
        conCheckedVoid(() -> proveedoresDAO.inactivar(id));
    }

    // ── Compras ──────────────────────────────────────────────────────────────

    @Override
    public void registrarCompra(Compra c) throws FachadaException {
        conCheckedVoid(() -> comprasDAO.agregar(c));
        c.crear();
    }

    @Override
    public void recibirCompra(Compra c) throws FachadaException {
        c.recibirCompra();
        for (DetalleCompra dc : c.getDetalles()) {
            if (dc.getProducto() == null) continue;
            try {
                Inventario inv = inventariosDAO.buscarPorProducto(dc.getProducto().getCodigo());
                inv.agregar(dc.getCantidad());
                inventariosDAO.actualizar(inv);
            } catch (PersistenciaException pe) {
                throw new FachadaException(
                        "No se pudo actualizar el inventario de " + dc.getProducto().getCodigo(), pe);
            }
        }
        conCheckedVoid(() -> comprasDAO.actualizar(c));
    }

    @Override
    public void actualizarCompra(Compra c) throws FachadaException {
        conCheckedVoid(() -> comprasDAO.actualizar(c));
    }

    @Override
    public void inactivarCompra(int idCompra) throws FachadaException {
        conCheckedVoid(() -> comprasDAO.inactivar(idCompra));
    }

    @Override
    public ArrayList<Compra> listarCompras() {
        return sinChequeo(comprasDAO::obtenerTodos);
    }

    @Override
    public ArrayList<Compra> listarComprasActivas() {
        return sinChequeo(comprasDAO::listarActivas);
    }

    @Override
    public ArrayList<Compra> listarComprasPendientes() {
        return sinChequeo(comprasDAO::listarPendientes);
    }

    @Override
    public ArrayList<Compra> listarComprasRecibidas() {
        return sinChequeo(comprasDAO::listarRecibidas);
    }

    @Override
    public ArrayList<Compra> buscarComprasPor(Predicate<Compra> condicion) {
        return sinChequeo(comprasDAO::obtenerTodos).stream()
                .filter(condicion)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Compra> buscarCompras(CriteriosCompra criterios) {
        return buscarComprasPor(criterios.aPredicate());
    }

    // ── Usuarios ─────────────────────────────────────────────────────────────

    @Override
    public void agregarUsuario(Usuario u) throws FachadaException {
        if (existeUsuarioConEmail(u.getEmail())) {
            throw new FachadaException("Ya existe un usuario con el email: " + u.getEmail());
        }
        conCheckedVoid(() -> usuariosDAO.agregar(u));
    }

    private boolean existeUsuarioConEmail(String email) {
        try {
            usuariosDAO.buscarPorEmail(email);
            return true;
        } catch (PersistenciaException pe) {
            return false;
        }
    }

    @Override
    public void actualizarUsuario(Usuario u) throws FachadaException {
        conCheckedVoid(() -> usuariosDAO.actualizar(u));
    }

    @Override
    public void inactivarUsuario(int id) throws FachadaException {
        conCheckedVoid(() -> usuariosDAO.inactivar(id));
    }

    @Override
    public Usuario buscarUsuario(int idUsuario) throws FachadaException {
        return conChecked(() -> usuariosDAO.buscar(idUsuario));
    }

    @Override
    public Usuario buscarUsuarioPorEmail(String email) throws FachadaException {
        return conChecked(() -> usuariosDAO.buscarPorEmail(email));
    }

    @Override
    public ArrayList<Usuario> listarUsuarios() {
        return sinChequeo(usuariosDAO::obtenerTodos);
    }

    @Override
    public ArrayList<Usuario> buscarUsuariosPor(Predicate<Usuario> condicion) {
        return sinChequeo(usuariosDAO::obtenerTodos).stream()
                .filter(condicion)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Usuario> buscarUsuarios(CriteriosUsuario criterios) {
        return buscarUsuariosPor(criterios.aPredicate());
    }

    @Override
    public ArrayList<Usuario> listarUsuariosActivos() {
        return sinChequeo(usuariosDAO::listarActivos);
    }

    @Override
    public ArrayList<Usuario> listarUsuariosPorPermiso(String permiso) {
        return sinChequeo(() -> usuariosDAO.listarPorPermiso(permiso));
    }

    @Override
    public Usuario autenticarUsuario(String email, String contrasena) throws FachadaException {
        Usuario u = buscarUsuarioPorEmail(email);

        if (!u.isActivo())
            throw new FachadaException("El usuario está inactivo. Contacte al administrador.");

        if (!u.getContrasena().equals(contrasena))
            throw new FachadaException("Credenciales inválidas.");

        if (!u.autenticar())
            throw new FachadaException("Autenticación fallida para el rol " + u.getPermiso() + ".");

        return u;
    }

    // ── Roles (cargos dinámicos con permisos) ────────────────────────────────

    @Override
    public void agregarRol(Rol r) throws FachadaException {
        if (existeRolConNombre(r.getNombreCargo())) {
            throw new FachadaException("Ya existe un rol con el cargo: " + r.getNombreCargo());
        }
        conCheckedVoid(() -> rolesDAO.agregar(r));
    }

    private boolean existeRolConNombre(String nombreCargo) {
        try {
            rolesDAO.buscarPorNombre(nombreCargo);
            return true;
        } catch (PersistenciaException pe) {
            return false;
        }
    }

    @Override
    public Rol buscarRol(int idRol) throws FachadaException {
        return conChecked(() -> rolesDAO.buscar(idRol));
    }

    @Override
    public void actualizarRol(Rol r) throws FachadaException {
        conCheckedVoid(() -> rolesDAO.actualizar(r));
    }

    @Override
    public void inactivarRol(int idRol) throws FachadaException {
        conCheckedVoid(() -> rolesDAO.inactivar(idRol));
    }

    @Override
    public ArrayList<Rol> listarRoles() {
        return sinChequeo(rolesDAO::obtenerTodos);
    }

    @Override
    public ArrayList<Rol> listarRolesActivos() {
        return sinChequeo(rolesDAO::listarActivos);
    }
}