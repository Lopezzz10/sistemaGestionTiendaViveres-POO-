package edu.uce.programacion2.tienda.fachadas;

import edu.uce.programacion2.tienda.excepciones.*;
import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.interfaces.ReglaDescuento;
import edu.uce.programacion2.tienda.negocio.*;
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
 * Única fachada del sistema — punto de acceso exclusivo de la presentación.
 * Encadena excepciones: PersistenciaException → FachadaException.
 * Expone filtros ricos de consulta sobre ArrayList para demostrar colecciones.
 *
 * Paso 2: implementa {@link IFachadaTienda} para permitir intercambiar esta
 * implementación en memoria por {@code FachadaArchivos} sin modificar el Control.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class FachadaTienda implements IFachadaTienda {

    private static final String NOMBRE_TIENDA = "TiendaViveres";

    private final Categorias  categorias;
    private final Productos   productos;
    private final Inventarios inventarios;
    private final Ventas      ventas;
    private final Facturas    facturas;
    private final Proveedores proveedores;
    private final Compras     compras;
    private final Usuarios    usuarios;
    private final Roles       roles;

    public FachadaTienda() {
        this.categorias  = new Categorias();
        this.productos   = new Productos();
        this.inventarios = new Inventarios();
        this.ventas       = new Ventas();
        this.facturas     = new Facturas();
        this.proveedores  = new Proveedores();
        this.compras      = new Compras();
        this.usuarios     = new Usuarios();
        this.roles        = new Roles();
    }

    public static String getNombreTienda() { return NOMBRE_TIENDA; }

    // ── Categorías ────────────────────────────────────────────────────────

    public void agregarCategoria(Categoria c) throws FachadaException {
        try { categorias.agregar(c); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public Categoria buscarCategoria(String cve) throws CategoriaNoEncontradaException, FachadaException {
        try { return categorias.buscar(cve); }
        catch (PersistenciaException e) {
            throw new CategoriaNoEncontradaException("Categoría no encontrada: " + cve, e);
        }
    }

    public ArrayList<Categoria> listarCategorias() throws FachadaException {
        try { return categorias.listar(); }
        catch (Exception e) { throw new FachadaException(e.getMessage(), e); }
    }

    public void actualizarCategoria(Categoria c) throws FachadaException {
        try { categorias.actualizar(c); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    @Override
    public void inactivarCategoria(String cve) throws FachadaException {
        try { categorias.inactivar(cve); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    @Override
    public ArrayList<Categoria> listarCategoriasActivas() throws FachadaException {
        return categorias.listarActivas();
    }

    // ── Productos (unificado) ────────────────────────────────────────────

    public void agregarProducto(Producto p) throws FachadaException {
        try { productos.agregar(p); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public Producto buscarProducto(String codigo)
            throws ProductoNoEncontradoException, FachadaException {
        try { return productos.buscar(codigo); }
        catch (PersistenciaException e) {
            throw new ProductoNoEncontradoException("Producto no encontrado: " + codigo, e);
        }
    }

    public void actualizarProducto(Producto p) throws FachadaException {
        try { productos.actualizar(p); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public void inactivarProducto(String codigo) throws FachadaException {
        try { productos.inactivar(codigo); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public ArrayList<Producto> listarProductosActivos() throws FachadaException {
        return productos.listarActivos();
    }

    public ArrayList<Producto> listarProductos() throws FachadaException {
        return new ArrayList<>(productos.listar());
    }

    public ArrayList<Producto> listarProductosPorTipo(String tipo) {
        return productos.listar().stream()
                .filter(p -> p.getTipo().equalsIgnoreCase(tipo))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Producto> listarVencidos() {
        return productos.listar().stream()
                .filter(Producto::estaVencido)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Producto> listarProximosAVencer() {
        return productos.listar().stream()
                .filter(p -> p.estaProximoAVencer() && !p.estaVencido())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Producto> listarConDescuento() {
        return productos.listar().stream()
                .filter(p -> p.calcularDescuento() > 0)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Producto> listarProductosPorCategoria(String cve) {
        return productos.listar().stream()
                .filter(p -> p.getCategoria() != null && p.getCategoria().getCveCategoria().equalsIgnoreCase(cve))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Producto> listarProductosPorMarca(String marca) {
        return productos.listar().stream()
                .filter(p -> p.getMarca() != null && p.getMarca().equalsIgnoreCase(marca))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Producto> buscarProductosPor(Predicate<Producto> condicion) {
        return productos.listar().stream()
                .filter(condicion)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Producto> buscarProductos(CriteriosProducto criterios) {
        return buscarProductosPor(criterios.aPredicate());
    }

    public Map<String, List<Producto>> agruparProductosPorCategoria() {
        return productos.listar().stream()
                .filter(p -> p.getCategoria() != null)
                .collect(Collectors.groupingBy(p -> p.getCategoria().getCveCategoria()));
    }

    public ArrayList<Producto> listarCatalogo() {
        return new ArrayList<>(productos.listar());
    }

    public int conteoTotal() {
        return productos.conteo();
    }

    @Override
    public double calcularPrecioPromedio() {
        return productos.listar().stream()
                .mapToDouble(Producto::calcularPrecioFinal)
                .average()
                .orElse(0.0);
    }

    @Override
    public double calcularValorTotalInventario() {
        return inventarios.listar().stream()
                .mapToDouble(inv -> inv.getProducto().calcularPrecioFinal() * inv.getCantidadDisponible())
                .sum();
    }

    @Override
    public ArrayList<Producto> listarProductosOrdenadosPorPrecio() {
        return productos.listar().stream()
                .sorted(Comparator.comparingDouble(Producto::calcularPrecioFinal)
                        .thenComparing(Producto::getNombre))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Map<Boolean, List<Producto>> particionarPorDescuento() {
        return productos.listar().stream()
                .collect(Collectors.partitioningBy(p -> p.calcularDescuento() > 0));
    }

    @Override
    public Map<String, Long> contarProductosPorCategoria() {
        return productos.listar().stream()
                .filter(p -> p.getCategoria() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria().getCveCategoria(),
                        Collectors.counting()));
    }

    @Override
    public Optional<Producto> buscarMasCaro() {
        return productos.listar().stream()
                .max(Comparator.comparingDouble(Producto::calcularPrecioFinal));
    }

    @Override
    public <R> ArrayList<R> extraerCampo(Function<Producto, R> extractor) {
        return productos.listar().stream()
                .map(extractor)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public double calcularTotalDescuentos(ReglaDescuento regla) {
        return productos.listar().stream()
                .mapToDouble(regla::aplicar)
                .sum();
    }

    // ── Inventario ────────────────────────────────────────────────────────

    public void agregarInventario(Inventario inv) throws FachadaException {
        try { inventarios.agregar(inv); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public Inventario buscarInventario(int id) throws FachadaException {
        try { return inventarios.buscar(id); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public void actualizarInventario(Inventario inv) throws FachadaException {
        try { inventarios.actualizar(inv); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public void inactivarInventario(int id) throws FachadaException {
        try { inventarios.inactivar(id); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public void retirarStock(String codigoProducto, int cantidad)
            throws StockInsuficienteException, FachadaException {
        Inventario inv = inventarios.buscarPorProducto(codigoProducto);
        if (inv == null)
            throw new FachadaException("Inventario no encontrado para: " + codigoProducto);
        inv.retirar(cantidad);
    }

    public ArrayList<Inventario> listarInventarios() { return inventarios.listar(); }

    @Override
    public ArrayList<Inventario> listarInventariosActivos() {
        return inventarios.listarActivos();
    }

    public ArrayList<Inventario> listarInventariosConAlerta() { return inventarios.listarConAlerta(); }

    // ── Ventas ────────────────────────────────────────────────────────────

    public void registrarVenta(Venta v) throws FachadaException {
        try { ventas.agregar(v); v.crear(); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public Venta buscarVenta(int idVenta) throws FachadaException {
        try { return ventas.buscar(idVenta); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    @Override
    public void actualizarVenta(Venta v) throws FachadaException {
        try {
            ventas.actualizar(v);
        } catch (PersistenciaException e) {
            throw new FachadaException(e.getMessage());
        }
    }

    @Override
    public void inactivarVenta(int idVenta) throws FachadaException {
        try {
            ventas.inactivar(idVenta);
        } catch (PersistenciaException e) {
            throw new FachadaException(e.getMessage());
        }
    }

    public ArrayList<Venta> listarVentas() { return ventas.listar(); }

    public ArrayList<Venta> listarVentasActivas()  { return ventas.listarActivas(); }

    public ArrayList<Venta> listarVentasAnuladas() { return ventas.listarAnuladas(); }

    // ── Facturas ──────────────────────────────────────────────────────────

    public void emitirFactura(Factura f) throws FachadaException {
        try { facturas.agregar(f); f.crear(); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public Factura buscarFactura(String numero) throws FachadaException {
        try { return facturas.buscar(numero); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public ArrayList<Factura> listarFacturas()         { return facturas.listar(); }

    public ArrayList<Factura> listarFacturasEmitidas() { return facturas.listarEmitidas(); }

    public ArrayList<Factura> listarFacturasAnuladas() { return facturas.listarAnuladas(); }

    /**
     * Búsqueda genérica de facturas por cualquier condición funcional.
     * Análoga a {@link #buscarProductosPor}.
     */
    public ArrayList<Factura> buscarFacturasPor(Predicate<Factura> condicion) {
        return facturas.buscarPor(condicion);
    }

    /**
     * Búsqueda avanzada de facturas: recibe varios parámetros opcionales a la
     * vez (rango de fechas, rango de total, cliente, número de factura,
     * IVA especial, estado) empaquetados en {@link CriteriosFactura}.
     */
    public ArrayList<Factura> buscarFacturas(CriteriosFactura criterios) {
        return buscarFacturasPor(criterios.aPredicate());
    }

    /**
     * Búsqueda avanzada de ventas (deprecada, usar buscarFacturas con CriteriosVenta).
     * @deprecated Use {@link #buscarFacturas(CriteriosVenta)} en su lugar.
     */
    @Override
    @Deprecated
    public ArrayList<Factura> buscarFacturas(CriteriosVenta criterios) {
        return buscarFacturasPor(criterios.aPredicate());
    }

    // ── Proveedores ───────────────────────────────────────────────────────

    public void agregarProveedor(Proveedor p) throws FachadaException {
        try { proveedores.agregar(p); p.crear(); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public Proveedor buscarProveedor(int id) throws FachadaException {
        try { return proveedores.buscar(id); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public Proveedor buscarProveedorPorRuc(String ruc) throws FachadaException {
        try { return proveedores.buscarPorRuc(ruc); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public ArrayList<Proveedor> listarProveedores() { return proveedores.listar(); }

    public void actualizarProveedor(Proveedor p) throws FachadaException {
        try { proveedores.actualizar(p); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public void inactivarProveedor(int id) throws FachadaException {
        try { proveedores.inactivar(id); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    @Override
    public ArrayList<Proveedor> listarProveedoresActivos() {
        return proveedores.listarActivos();
    }

    // ── Compras ───────────────────────────────────────────────────────────

    public void registrarCompra(Compra c) throws FachadaException {
        try { compras.agregar(c); c.crear(); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public void recibirCompra(Compra c) throws FachadaException {
        c.recibirCompra();
        for (DetalleCompra dc : c.getDetalles()) {
            Inventario inv = inventarios.buscarPorProducto(dc.getProducto().getCodigo());
            if (inv != null)
                inv.agregar(dc.getCantidad());
            else
                System.out.println("  ⚠ Sin inventario registrado para: " +
                        dc.getProducto().getNombre());
        }
    }

    @Override
    public void actualizarCompra(Compra c) throws FachadaException {
        try {
            compras.actualizar(c);
        } catch (PersistenciaException e) {
            throw new FachadaException(e.getMessage());
        }
    }

    @Override
    public void inactivarCompra(int idCompra) throws FachadaException {
        try {
            compras.inactivar(idCompra);
        } catch (PersistenciaException e) {
            throw new FachadaException(e.getMessage());
        }
    }

    @Override
    public ArrayList<Compra> listarComprasActivas() {
        return compras.listarActivas();
    }

    public ArrayList<Compra> listarCompras()           { return compras.listar(); }

    public ArrayList<Compra> listarComprasPendientes() { return compras.listarPendientes(); }

    public ArrayList<Compra> listarComprasRecibidas()  { return compras.listarRecibidas(); }

    /**
     * Búsqueda genérica de compras por cualquier condición funcional.
     * Análoga a {@link #buscarProductosPor}.
     */
    public ArrayList<Compra> buscarComprasPor(Predicate<Compra> condicion) {
        return compras.listar().stream()
                .filter(condicion)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Búsqueda avanzada de compras: recibe varios parámetros opcionales a la
     * vez (proveedor, rango de fechas, rango de total, producto, categoría,
     * estado) empaquetados en {@link CriteriosCompra}.
     */
    public ArrayList<Compra> buscarCompras(CriteriosCompra criterios) {
        return buscarComprasPor(criterios.aPredicate());
    }

    // ── Usuarios ──────────────────────────────────────────────────────────

    public void agregarUsuario(Usuario u) throws FachadaException {
        try { usuarios.agregar(u); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public Usuario buscarUsuario(int idUsuario) throws FachadaException {
        try { return usuarios.buscar(idUsuario); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public Usuario buscarUsuarioPorEmail(String email) throws FachadaException {
        try { return usuarios.buscarPorEmail(email); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public ArrayList<Usuario> listarUsuarios() { return usuarios.listar(); }

    public void actualizarUsuario(Usuario u) throws FachadaException {
        try { usuarios.actualizar(u); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    public void inactivarUsuario(int id) throws FachadaException {
        try { usuarios.inactivar(id); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    @Override
    public ArrayList<Usuario> listarUsuariosActivos() {
        return usuarios.listarActivos();
    }

    public ArrayList<Usuario> listarUsuariosPorPermiso(String permiso) {
        return usuarios.listarPorPermiso(permiso);
    }

    @Override
    public Usuario autenticarUsuario(String email, String contrasena) throws FachadaException {
        Usuario u;
        try {
            u = usuarios.buscarPorEmail(email);
        } catch (PersistenciaException e) {
            throw new FachadaException("Credenciales inválidas.", e);
        }

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
        try { roles.agregar(r); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    @Override
    public Rol buscarRol(int idRol) throws FachadaException {
        try { return roles.buscar(idRol); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    @Override
    public void actualizarRol(Rol r) throws FachadaException {
        try { roles.actualizar(r); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    @Override
    public void inactivarRol(int idRol) throws FachadaException {
        try { roles.inactivar(idRol); }
        catch (PersistenciaException e) { throw new FachadaException(e.getMessage(), e); }
    }

    @Override
    public ArrayList<Rol> listarRoles() { return roles.listar(); }

    @Override
    public ArrayList<Rol> listarRolesActivos() { return roles.listarActivos(); }
}