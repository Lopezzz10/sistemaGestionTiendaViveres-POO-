package edu.uce.programacion2.tienda.fachadas;

import edu.uce.programacion2.tienda.excepciones.*;
import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.interfaces.ReglaDescuento;
import edu.uce.programacion2.tienda.negocio.*;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosCompra;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosFactura;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosInventario;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosProducto;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosProveedor;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosUsuario;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosVenta;
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
 * Implementación alternativa de {@link IFachadaTienda}, usada por
 * {@code Control} como respaldo cuando {@link FachadaArchivos} no puede
 * inicializarse (por ejemplo si los archivos .dat están corruptos o
 * bloqueados).
 *
 * Paso 2: implementa {@link IFachadaTienda} para permitir intercambiar esta
 * implementación por {@code FachadaArchivos} sin modificar el Control.
 *
 * Paso 6: los DAOs de {@code persistencia} (Categorias, Productos, Ventas,
 * Compras, Facturas, etc.) migraron por completo a archivos binarios de
 * acceso aleatorio: ya no existe una variante "solo en memoria" de estas
 * clases. Por eso esta fachada ya no puede evitar la E/S a disco, pero
 * conserva su rol original: usa los DAOs con sus archivos por defecto
 * (constructores sin argumentos) como plan de contingencia independiente
 * de {@link FachadaArchivos}, y — igual que ella — traduce las
 * {@link PersistenciaException} (checked) que ahora lanzan casi todos los
 * métodos de los DAOs a {@link FachadaException} o, para los métodos de
 * {@link IFachadaTienda} que no declaran ninguna excepción (listados y
 * búsquedas), a una {@link RuntimeException} (ver {@link #sinChequeo}).
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
    private final Administradores administradores;
    private final Cajeros         cajeros;
    private final Clientes        clientes;
    private final Roles       roles;

    public FachadaTienda() {
        this.categorias  = new Categorias();
        this.productos   = new Productos();
        this.inventarios = new Inventarios();
        this.ventas       = new Ventas();
        this.facturas     = new Facturas();
        this.proveedores  = new Proveedores();
        this.compras      = new Compras();
        this.administradores = new Administradores();
        this.cajeros          = new Cajeros();
        this.clientes         = new Clientes();
        this.roles        = new Roles();
    }

    public static String getNombreTienda() { return NOMBRE_TIENDA; }

    // ── Helpers de manejo de excepciones ─────────────────────────────────
    // (mismo patrón que FachadaArchivos, ver esa clase para más detalle)

    @FunctionalInterface
    private interface Operacion<T> { T ejecutar() throws PersistenciaException; }

    @FunctionalInterface
    private interface OperacionVoid { void ejecutar() throws PersistenciaException; }

    /** Para métodos de la interfaz que SÍ declaran throws FachadaException. */
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
     * Para métodos de la interfaz que NO declaran ninguna excepción
     * (consultas/listados). Si el DAO falla, se re-envuelve en una
     * excepción NO checked para no romper la firma heredada del diseño
     * original en memoria.
     */
    private <T> T sinChequeo(Operacion<T> op) {
        try {
            return op.ejecutar();
        } catch (PersistenciaException pe) {
            throw new RuntimeException(pe.getMessage(), pe);
        }
    }

    // ── Categorías ────────────────────────────────────────────────────────

    public void agregarCategoria(Categoria c) throws FachadaException {
        conCheckedVoid(() -> categorias.agregar(c));
    }

    public Categoria buscarCategoria(String cve) throws CategoriaNoEncontradaException, FachadaException {
        try {
            return categorias.buscar(cve);
        } catch (PersistenciaException e) {
            throw new CategoriaNoEncontradaException("Categoría no encontrada: " + cve, e);
        }
    }

    public ArrayList<Categoria> listarCategorias() throws FachadaException {
        return conChecked(categorias::obtenerTodas);
    }

    public void actualizarCategoria(Categoria c) throws FachadaException {
        conCheckedVoid(() -> categorias.actualizar(c));
    }

    @Override
    public void inactivarCategoria(String cve) throws FachadaException {
        conCheckedVoid(() -> categorias.inactivar(cve));
    }

    @Override
    public ArrayList<Categoria> listarCategoriasActivas() throws FachadaException {
        return conChecked(categorias::listarActivas);
    }

    // ── Productos (unificado) ────────────────────────────────────────────

    public void agregarProducto(Producto p) throws FachadaException {
        conCheckedVoid(() -> productos.agregar(p));
    }

    public Producto buscarProducto(String codigo)
            throws ProductoNoEncontradoException, FachadaException {
        try {
            return productos.buscar(codigo);
        } catch (PersistenciaException e) {
            throw new ProductoNoEncontradoException("Producto no encontrado: " + codigo, e);
        }
    }

    public void actualizarProducto(Producto p) throws FachadaException {
        conCheckedVoid(() -> productos.actualizar(p));
    }

    public void inactivarProducto(String codigo) throws FachadaException {
        conCheckedVoid(() -> productos.inactivar(codigo));
    }

    public ArrayList<Producto> listarProductosActivos() throws FachadaException {
        return conChecked(productos::listarActivos);
    }

    public ArrayList<Producto> listarProductos() throws FachadaException {
        return conChecked(productos::obtenerTodos);
    }

    public ArrayList<Producto> listarProductosPorTipo(String tipo) {
        return sinChequeo(productos::obtenerTodos).stream()
                .filter(p -> p.getTipo().equalsIgnoreCase(tipo))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Producto> listarVencidos() {
        return sinChequeo(productos::obtenerTodos).stream()
                .filter(Producto::estaVencido)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Producto> listarProximosAVencer() {
        return sinChequeo(productos::obtenerTodos).stream()
                .filter(p -> p.estaProximoAVencer() && !p.estaVencido())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Producto> listarConDescuento() {
        return sinChequeo(productos::obtenerTodos).stream()
                .filter(p -> p.calcularDescuento() > 0)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Producto> listarProductosPorCategoria(String cve) {
        return sinChequeo(productos::obtenerTodos).stream()
                .filter(p -> p.getCategoria() != null && p.getCategoria().getCveCategoria().equalsIgnoreCase(cve))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Producto> listarProductosPorMarca(String marca) {
        return sinChequeo(productos::obtenerTodos).stream()
                .filter(p -> p.getMarca() != null && p.getMarca().equalsIgnoreCase(marca))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Producto> buscarProductosPor(Predicate<Producto> condicion) {
        return sinChequeo(() -> productos.buscarPor(condicion));
    }

    @Override
    public ArrayList<Producto> buscarProductos(CriteriosProducto criterios) {
        return buscarProductosPor(criterios.aPredicate());
    }

    public Map<String, List<Producto>> agruparProductosPorCategoria() {
        return sinChequeo(productos::obtenerTodos).stream()
                .filter(p -> p.getCategoria() != null)
                .collect(Collectors.groupingBy(p -> p.getCategoria().getCveCategoria()));
    }

    public ArrayList<Producto> listarCatalogo() {
        return sinChequeo(productos::obtenerTodos);
    }

    public int conteoTotal() {
        return productos.conteo();
    }

    @Override
    public double calcularPrecioPromedio() {
        return sinChequeo(productos::obtenerTodos).stream()
                .mapToDouble(Producto::calcularPrecioFinal)
                .average()
                .orElse(0.0);
    }

    @Override
    public double calcularValorTotalInventario() {
        return sinChequeo(inventarios::obtenerTodos).stream()
                .mapToDouble(inv -> inv.getProducto().calcularPrecioFinal() * inv.getCantidadDisponible())
                .sum();
    }

    @Override
    public ArrayList<Producto> listarProductosOrdenadosPorPrecio() {
        return sinChequeo(productos::obtenerTodos).stream()
                .sorted(Comparator.comparingDouble(Producto::calcularPrecioFinal)
                        .thenComparing(Producto::getNombre))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Map<Boolean, List<Producto>> particionarPorDescuento() {
        return sinChequeo(productos::obtenerTodos).stream()
                .collect(Collectors.partitioningBy(p -> p.calcularDescuento() > 0));
    }

    @Override
    public Map<String, Long> contarProductosPorCategoria() {
        return sinChequeo(productos::obtenerTodos).stream()
                .filter(p -> p.getCategoria() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria().getCveCategoria(),
                        Collectors.counting()));
    }

    @Override
    public Optional<Producto> buscarMasCaro() {
        return sinChequeo(productos::obtenerTodos).stream()
                .max(Comparator.comparingDouble(Producto::calcularPrecioFinal));
    }

    @Override
    public <R> ArrayList<R> extraerCampo(Function<Producto, R> extractor) {
        return sinChequeo(productos::obtenerTodos).stream()
                .map(extractor)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public double calcularTotalDescuentos(ReglaDescuento regla) {
        return sinChequeo(productos::obtenerTodos).stream()
                .mapToDouble(regla::aplicar)
                .sum();
    }

    // ── Inventario ────────────────────────────────────────────────────────

    public void agregarInventario(Inventario inv) throws FachadaException {
        conCheckedVoid(() -> inventarios.agregar(inv));
    }

    public Inventario buscarInventario(int id) throws FachadaException {
        return conChecked(() -> inventarios.buscar(id));
    }

    public void actualizarInventario(Inventario inv) throws FachadaException {
        conCheckedVoid(() -> inventarios.actualizar(inv));
    }

    public void inactivarInventario(int id) throws FachadaException {
        conCheckedVoid(() -> inventarios.inactivar(id));
    }

    public void retirarStock(String codigoProducto, int cantidad)
            throws StockInsuficienteException, FachadaException {
        Inventario inv;
        try {
            inv = inventarios.buscarPorProducto(codigoProducto);
        } catch (PersistenciaException e) {
            throw new FachadaException("Inventario no encontrado para: " + codigoProducto, e);
        }
        inv.retirar(cantidad);
        conCheckedVoid(() -> inventarios.actualizar(inv));
    }

    public ArrayList<Inventario> listarInventarios() {
        return sinChequeo(inventarios::obtenerTodos);
    }

    @Override
    public ArrayList<Inventario> listarInventariosActivos() {
        return sinChequeo(inventarios::listarActivos);
    }

    public ArrayList<Inventario> listarInventariosConAlerta() {
        return sinChequeo(inventarios::listarConAlerta);
    }

    public ArrayList<Inventario> buscarInventariosPor(Predicate<Inventario> condicion) {
        return sinChequeo(inventarios::obtenerTodos).stream()
                .filter(condicion)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Inventario> buscarInventarios(CriteriosInventario criterios) {
        return buscarInventariosPor(criterios.aPredicate());
    }

    // ── Ventas ────────────────────────────────────────────────────────────

    public void registrarVenta(Venta v) throws FachadaException {
        conCheckedVoid(() -> ventas.agregar(v));
        v.crear();
    }

    public Venta buscarVenta(int idVenta) throws FachadaException {
        return conChecked(() -> ventas.buscar(idVenta));
    }

    @Override
    public void actualizarVenta(Venta v) throws FachadaException {
        conCheckedVoid(() -> ventas.actualizar(v));
    }

    @Override
    public void inactivarVenta(int idVenta) throws FachadaException {
        conCheckedVoid(() -> ventas.inactivar(idVenta));
    }

    public ArrayList<Venta> listarVentas() {
        return sinChequeo(ventas::obtenerTodos);
    }

    public ArrayList<Venta> listarVentasActivas()  {
        return sinChequeo(ventas::listarActivas);
    }

    public ArrayList<Venta> listarVentasAnuladas() {
        return sinChequeo(ventas::listarAnuladas);
    }

    // ── Facturas ──────────────────────────────────────────────────────────

    public void emitirFactura(Factura f) throws FachadaException {
        conCheckedVoid(() -> facturas.agregar(f));
        f.crear();
    }

    public Factura buscarFactura(String numero) throws FachadaException {
        return conChecked(() -> facturas.buscar(numero));
    }

    public ArrayList<Factura> listarFacturas() {
        return sinChequeo(facturas::obtenerTodos);
    }

    public ArrayList<Factura> listarFacturasEmitidas() {
        return sinChequeo(facturas::listarEmitidas);
    }

    public ArrayList<Factura> listarFacturasAnuladas() {
        return sinChequeo(facturas::listarAnuladas);
    }

    /**
     * Búsqueda genérica de facturas por cualquier condición funcional.
     * Análoga a {@link #buscarProductosPor}.
     */
    public ArrayList<Factura> buscarFacturasPor(Predicate<Factura> condicion) {
        return sinChequeo(() -> facturas.buscarPor(condicion));
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
     * Búsqueda avanzada de ventas (deprecada, usar buscarFacturas con CriteriosFactura).
     * @deprecated Use {@link #buscarFacturas(CriteriosFactura)} en su lugar.
     */
    @Override
    @Deprecated
    public ArrayList<Factura> buscarFacturas(CriteriosVenta criterios) {
        return buscarFacturasPor(criterios.aPredicate());
    }

    // ── Proveedores ───────────────────────────────────────────────────────

    public void agregarProveedor(Proveedor p) throws FachadaException {
        conCheckedVoid(() -> proveedores.agregar(p));
        p.crear();
    }

    public Proveedor buscarProveedor(int id) throws FachadaException {
        return conChecked(() -> proveedores.buscar(id));
    }

    public Proveedor buscarProveedorPorRuc(String ruc) throws FachadaException {
        return conChecked(() -> proveedores.buscarPorRuc(ruc));
    }

    public ArrayList<Proveedor> listarProveedores() {
        return sinChequeo(proveedores::obtenerTodos);
    }

    public void actualizarProveedor(Proveedor p) throws FachadaException {
        conCheckedVoid(() -> proveedores.actualizar(p));
    }

    public void inactivarProveedor(int id) throws FachadaException {
        conCheckedVoid(() -> proveedores.inactivar(id));
    }

    @Override
    public ArrayList<Proveedor> listarProveedoresActivos() {
        return sinChequeo(proveedores::listarActivos);
    }

    /**
     * Búsqueda avanzada de proveedores: recibe cualquier {@link Predicate}
     * y filtra la lista completa de proveedores con él.
     * Análoga a {@link #buscarProductosPor}.
     */
    public ArrayList<Proveedor> buscarProveedoresPor(Predicate<Proveedor> condicion) {
        return sinChequeo(proveedores::obtenerTodos).stream()
                .filter(condicion)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Búsqueda avanzada de proveedores: recibe varios parámetros opcionales
     * a la vez (nombre, RUC, teléfono, email, dirección, solo activos)
     * empaquetados en {@link CriteriosProveedor}.
     */
    public ArrayList<Proveedor> buscarProveedores(CriteriosProveedor criterios) {
        return buscarProveedoresPor(criterios.aPredicate());
    }

    // ── Compras ───────────────────────────────────────────────────────────

    public void registrarCompra(Compra c) throws FachadaException {
        conCheckedVoid(() -> compras.agregar(c));
        c.crear();
    }

    public void recibirCompra(Compra c) throws FachadaException {
        c.recibirCompra();
        for (DetalleCompra dc : c.getDetalles()) {
            if (dc.getProducto() == null) continue;
            try {
                Inventario inv = inventarios.buscarPorProducto(dc.getProducto().getCodigo());
                inv.agregar(dc.getCantidad());
                inventarios.actualizar(inv);
            } catch (PersistenciaException e) {
                throw new FachadaException(
                        "No se pudo actualizar el inventario de " + dc.getProducto().getCodigo(), e);
            }
        }
        conCheckedVoid(() -> compras.actualizar(c));
    }

    @Override
    public void actualizarCompra(Compra c) throws FachadaException {
        conCheckedVoid(() -> compras.actualizar(c));
    }

    @Override
    public void inactivarCompra(int idCompra) throws FachadaException {
        conCheckedVoid(() -> compras.inactivar(idCompra));
    }

    @Override
    public ArrayList<Compra> listarComprasActivas() {
        return sinChequeo(compras::listarActivas);
    }

    public ArrayList<Compra> listarCompras() {
        return sinChequeo(compras::obtenerTodos);
    }

    public ArrayList<Compra> listarComprasPendientes() {
        return sinChequeo(compras::listarPendientes);
    }

    public ArrayList<Compra> listarComprasRecibidas()  {
        return sinChequeo(compras::listarRecibidas);
    }

    /**
     * Búsqueda genérica de compras por cualquier condición funcional.
     * Análoga a {@link #buscarProductosPor}.
     */
    public ArrayList<Compra> buscarComprasPor(Predicate<Compra> condicion) {
        return sinChequeo(compras::obtenerTodos).stream()
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
    //
    // Igual que en FachadaArchivos: no hay una clase "Usuarios" coordinadora.
    // Administrador, Cajero y Cliente viven cada uno en su propio DAO, y
    // esta fachada decide -- via instanceof -- a cual delegar cada
    // operacion. El id sigue siendo unico en todo el sistema (ver
    // #siguienteIdUsuario).

    public void agregarUsuario(Usuario u) throws FachadaException {
        conCheckedVoid(() -> agregarUsuarioInterno(u));
    }

    private void agregarUsuarioInterno(Usuario u) throws PersistenciaException {
        int nuevoId = siguienteIdUsuario();
        if (u instanceof Administrador) {
            administradores.agregar((Administrador) u, nuevoId);
        } else if (u instanceof Cajero) {
            cajeros.agregar((Cajero) u, nuevoId);
        } else if (u instanceof Cliente) {
            clientes.agregar((Cliente) u, nuevoId);
        } else {
            throw new PersistenciaException("Tipo de usuario desconocido: " + u.getClass().getSimpleName());
        }
    }

    private int siguienteIdUsuario() throws PersistenciaException {
        int maxAdmin   = administradores.maxId();
        int maxCajero  = cajeros.maxId();
        int maxCliente = clientes.maxId();
        return Math.max(maxAdmin, Math.max(maxCajero, maxCliente)) + 1;
    }

    public Usuario buscarUsuario(int idUsuario) throws FachadaException {
        return conChecked(() -> buscarUsuarioInterno(idUsuario));
    }

    // Busca en los tres archivos (el id es unico entre los tres, asi que
    // a lo sumo uno lo tiene).
    private Usuario buscarUsuarioInterno(int idUsuario) throws PersistenciaException {
        try {
            return administradores.buscar(idUsuario);
        } catch (PersistenciaException ignorado) { }
        try {
            return cajeros.buscar(idUsuario);
        } catch (PersistenciaException ignorado) { }
        try {
            return clientes.buscar(idUsuario);
        } catch (PersistenciaException ignorado) { }
        throw new PersistenciaException("Usuario no encontrado: id=" + idUsuario);
    }

    public Usuario buscarUsuarioPorEmail(String email) throws FachadaException {
        return conChecked(() -> buscarUsuarioPorEmailInterno(email));
    }

    private Usuario buscarUsuarioPorEmailInterno(String email) throws PersistenciaException {
        try {
            return administradores.buscarPorEmail(email);
        } catch (PersistenciaException ignorado) { }
        try {
            return cajeros.buscarPorEmail(email);
        } catch (PersistenciaException ignorado) { }
        try {
            return clientes.buscarPorEmail(email);
        } catch (PersistenciaException ignorado) { }
        throw new PersistenciaException("Usuario no encontrado con email: " + email);
    }

    public ArrayList<Usuario> listarUsuarios() {
        return sinChequeo(this::obtenerTodosLosUsuarios);
    }

    private ArrayList<Usuario> obtenerTodosLosUsuarios() throws PersistenciaException {
        ArrayList<Usuario> resultado = new ArrayList<>();
        resultado.addAll(administradores.obtenerTodos());
        resultado.addAll(cajeros.obtenerTodos());
        resultado.addAll(clientes.obtenerTodos());
        return resultado;
    }

    /**
     * Búsqueda avanzada de usuarios: recibe cualquier {@link Predicate}
     * y filtra la lista completa de usuarios con él.
     * Análoga a {@link #buscarProductosPor}.
     */
    @Override
    public ArrayList<Usuario> buscarUsuariosPor(Predicate<Usuario> condicion) {
        return sinChequeo(this::obtenerTodosLosUsuarios).stream()
                .filter(condicion)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Búsqueda avanzada de usuarios: recibe varios parámetros opcionales
     * a la vez (nombre/email parcial, rol, solo activos) empaquetados en
     * {@link CriteriosUsuario}.
     */
    @Override
    public ArrayList<Usuario> buscarUsuarios(CriteriosUsuario criterios) {
        return buscarUsuariosPor(criterios.aPredicate());
    }

    public void actualizarUsuario(Usuario u) throws FachadaException {
        conCheckedVoid(() -> {
            if (u instanceof Administrador) {
                administradores.actualizar((Administrador) u);
            } else if (u instanceof Cajero) {
                cajeros.actualizar((Cajero) u);
            } else if (u instanceof Cliente) {
                clientes.actualizar((Cliente) u);
            } else {
                throw new PersistenciaException("Tipo de usuario desconocido: " + u.getClass().getSimpleName());
            }
        });
    }

    public void inactivarUsuario(int id) throws FachadaException {
        conCheckedVoid(() -> {
            Usuario u = buscarUsuarioInterno(id);
            if (u instanceof Administrador) {
                administradores.inactivar(id);
            } else if (u instanceof Cajero) {
                cajeros.inactivar(id);
            } else {
                clientes.inactivar(id);
            }
        });
    }

    @Override
    public ArrayList<Usuario> listarUsuariosActivos() {
        return sinChequeo(() -> {
            ArrayList<Usuario> resultado = new ArrayList<>();
            resultado.addAll(administradores.listarActivos());
            resultado.addAll(cajeros.listarActivos());
            resultado.addAll(clientes.listarActivos());
            return resultado;
        });
    }

    public ArrayList<Usuario> listarUsuariosPorPermiso(String permiso) {
        return buscarUsuariosPor(u -> u.getPermiso().equalsIgnoreCase(permiso));
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
        conCheckedVoid(() -> roles.agregar(r));
    }

    @Override
    public Rol buscarRol(int idRol) throws FachadaException {
        return conChecked(() -> roles.buscar(idRol));
    }

    @Override
    public void actualizarRol(Rol r) throws FachadaException {
        conCheckedVoid(() -> roles.actualizar(r));
    }

    @Override
    public void inactivarRol(int idRol) throws FachadaException {
        conCheckedVoid(() -> roles.inactivar(idRol));
    }

    @Override
    public ArrayList<Rol> listarRoles() {
        return sinChequeo(roles::obtenerTodos);
    }

    @Override
    public ArrayList<Rol> listarRolesActivos() {
        return sinChequeo(roles::listarActivos);
    }
}