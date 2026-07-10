package edu.uce.programacion2.tienda.fachadas;

import edu.uce.programacion2.tienda.excepciones.CategoriaNoEncontradaException;
import edu.uce.programacion2.tienda.excepciones.FachadaException;
import edu.uce.programacion2.tienda.excepciones.ProductoNoEncontradoException;
import edu.uce.programacion2.tienda.excepciones.StockInsuficienteException;
import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.interfaces.ReglaDescuento;
import edu.uce.programacion2.tienda.negocio.*;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;
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
 * Implementación de {@link IFachadaTienda} con persistencia en archivos de datos.
 *
 * Arquitectura de capas:
 *   VentanaPrincipal / Control
 *          ↓
 *   FachadaArchivos   ← único punto de acceso (implementa IFachadaTienda)
 *          ↓
 *   CategoriasArchivo / ProductosArchivo / InventariosArchivo
 *   ProveedoresArchivo / UsuariosArchivo / VentasArchivo
 *   ComprasArchivo / FacturasArchivo
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class FachadaArchivos implements IFachadaTienda {

    private static final String NOMBRE_TIENDA = "TiendaViveres";

    // ── Objetos de acceso a archivos ─────────────────────────────────────────
    private final CategoriasArchivo  categoriasArchivo;
    private final ProductosArchivo   productosArchivo;
    private final InventariosArchivo inventariosArchivo;
    private final ProveedoresArchivo proveedoresArchivo;
    private final UsuariosArchivo    usuariosArchivo;
    private final VentasArchivo      ventasArchivo;
    private final ComprasArchivo     comprasArchivo;
    private final FacturasArchivo    facturasArchivo;
    private final RolesArchivo       rolesArchivo;
    private final DetalleFacturaArchivo detalleFacturaArchivo;

    // ── Listas en memoria ────────────────────────────────────────────────────
    private ArrayList<Categoria>  categorias;
    private ArrayList<Producto>   productos;
    private ArrayList<Inventario> inventarios;
    private ArrayList<Proveedor>  proveedores;
    private ArrayList<Usuario>    usuarios;
    private ArrayList<Venta>      ventas;
    private ArrayList<Compra>     compras;
    private ArrayList<Factura>    facturas;
    private ArrayList<Rol>        roles;
    private ArrayList<edu.uce.programacion2.tienda.persistencia.DetalleFactura.Registro> detallesFactura;

    // ── Constructor ──────────────────────────────────────────────────────────

    public FachadaArchivos() throws FachadaException {
        categoriasArchivo  = new CategoriasArchivo();
        productosArchivo   = new ProductosArchivo();
        inventariosArchivo = new InventariosArchivo();
        proveedoresArchivo = new ProveedoresArchivo();
        usuariosArchivo    = new UsuariosArchivo();
        ventasArchivo      = new VentasArchivo();
        comprasArchivo     = new ComprasArchivo();
        facturasArchivo    = new FacturasArchivo();
        rolesArchivo       = new RolesArchivo();
        detalleFacturaArchivo = new DetalleFacturaArchivo();
        try {
            categorias  = categoriasArchivo.cargar();
            productos   = productosArchivo.cargar();
            inventarios = inventariosArchivo.cargar(productos);
            proveedores = proveedoresArchivo.cargar();
            usuarios    = usuariosArchivo.cargar();
            ventas      = ventasArchivo.cargar(productos);
            compras     = comprasArchivo.cargar(productos, proveedores);
            facturas    = facturasArchivo.cargar(productos);
            roles       = rolesArchivo.cargar();
            detallesFactura = detalleFacturaArchivo.cargar(productos);
        } catch (Exception e) {
            throw new FachadaException("Error al cargar los archivos de datos: " + e.getMessage(), e);
        }
    }

    public static String getNombreTienda() { return NOMBRE_TIENDA; }

    // ── Métodos privados de guardado ─────────────────────────────────────────

    private void guardarCategorias() throws FachadaException {
        try { categoriasArchivo.guardar(categorias); }
        catch (Exception e) { throw new FachadaException("Error al guardar categorias.dat", e); }
    }

    private void guardarProductos() throws FachadaException {
        try { productosArchivo.guardar(productos); }
        catch (Exception e) { throw new FachadaException("Error al guardar productos.dat", e); }
    }

    private void guardarInventarios() throws FachadaException {
        try { inventariosArchivo.guardar(inventarios); }
        catch (Exception e) { throw new FachadaException("Error al guardar inventarios.dat", e); }
    }

    private void guardarProveedores() throws FachadaException {
        try { proveedoresArchivo.guardar(proveedores); }
        catch (Exception e) { throw new FachadaException("Error al guardar proveedores.dat", e); }
    }

    private void guardarUsuarios() throws FachadaException {
        try { usuariosArchivo.guardar(usuarios); }
        catch (Exception e) { throw new FachadaException("Error al guardar usuarios.dat", e); }
    }

    private void guardarVentas() throws FachadaException {
        try { ventasArchivo.guardar(ventas); }
        catch (Exception e) { throw new FachadaException("Error al guardar ventas.dat", e); }
    }

    private void guardarCompras() throws FachadaException {
        try { comprasArchivo.guardar(compras); }
        catch (Exception e) { throw new FachadaException("Error al guardar compras.dat", e); }
    }

    private void guardarFacturas() throws FachadaException {
        try { facturasArchivo.guardar(facturas); }
        catch (Exception e) { throw new FachadaException("Error al guardar facturas.dat", e); }
    }

    private void guardarRoles() throws FachadaException {
        try { rolesArchivo.guardar(roles); }
        catch (Exception e) { throw new FachadaException("Error al guardar roles.dat", e); }
    }

    private void guardarDetallesFactura() throws FachadaException {
        try { detalleFacturaArchivo.guardar(detallesFactura); }
        catch (Exception e) { throw new FachadaException("Error al guardar detallefactura.dat", e); }
    }

    // ── Categorías ───────────────────────────────────────────────────────────

    @Override
    public void agregarCategoria(Categoria c) throws FachadaException {
        for (Categoria cat : categorias)
            if (cat.getCveCategoria().equalsIgnoreCase(c.getCveCategoria()))
                throw new FachadaException("Categoría ya existe: " + c.getCveCategoria());
        categorias.add(c);
        guardarCategorias();
    }

    @Override
    public Categoria buscarCategoria(String cve)
            throws CategoriaNoEncontradaException, FachadaException {
        for (Categoria c : categorias)
            if (c.getCveCategoria().equalsIgnoreCase(cve)) return c;
        throw new CategoriaNoEncontradaException("Categoría no encontrada: " + cve);
    }

    @Override
    public ArrayList<Categoria> listarCategorias() throws FachadaException {
        return new ArrayList<>(categorias);
    }

    @Override
    public void actualizarCategoria(Categoria c) throws FachadaException {
        for (int i = 0; i < categorias.size(); i++) {
            if (categorias.get(i).getCveCategoria().equalsIgnoreCase(c.getCveCategoria())) {
                categorias.set(i, c);
                guardarCategorias();
                return;
            }
        }
        throw new FachadaException("Categoría no encontrada para actualizar: " + c.getCveCategoria());
    }

    @Override
    public void inactivarCategoria(String cve) throws FachadaException {
        for (Categoria c : categorias) {
            if (c.getCveCategoria().equalsIgnoreCase(cve)) {
                c.setActivo(false);
                guardarCategorias();
                return;
            }
        }
        throw new FachadaException("Categoría no encontrada para inactivar: " + cve);
    }

    @Override
    public ArrayList<Categoria> listarCategoriasActivas() throws FachadaException {
        ArrayList<Categoria> resultado = new ArrayList<>();
        for (Categoria c : categorias)
            if (c.isActivo()) resultado.add(c);
        return resultado;
    }

    // ── Productos (unificado) ────────────────────────────────────────────────

    @Override
    public void agregarProducto(Producto p) throws FachadaException {
        for (Producto prod : productos)
            if (prod.getCodigo().equalsIgnoreCase(p.getCodigo()))
                throw new FachadaException("Producto ya existe: " + p.getCodigo());
        productos.add(p);
        guardarProductos();
    }

    @Override
    public Producto buscarProducto(String codigo)
            throws ProductoNoEncontradoException, FachadaException {
        for (Producto p : productos)
            if (p.getCodigo().equalsIgnoreCase(codigo)) return p;
        throw new ProductoNoEncontradoException("Producto no encontrado: " + codigo);
    }

    @Override
    public void actualizarProducto(Producto p) throws FachadaException {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getCodigo().equalsIgnoreCase(p.getCodigo())) {
                productos.set(i, p);
                guardarProductos();
                return;
            }
        }
        throw new FachadaException("Producto no encontrado para actualizar: " + p.getCodigo());
    }

    @Override
    public void inactivarProducto(String codigo) throws FachadaException {
        for (Producto p : productos) {
            if (p.getCodigo().equalsIgnoreCase(codigo)) {
                p.setEstado(Producto.ESTADO_INACTIVO);
                guardarProductos();
                return;
            }
        }
        throw new FachadaException("Producto no encontrado para inactivar: " + codigo);
    }

    @Override
    public ArrayList<Producto> listarProductosActivos() throws FachadaException {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (Producto p : productos)
            if (p.isActivo()) resultado.add(p);
        return resultado;
    }

    @Override
    public ArrayList<Producto> listarProductos() throws FachadaException {
        return new ArrayList<>(productos);
    }

    @Override
    public ArrayList<Producto> listarProductosPorTipo(String tipo) {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (Producto p : productos)
            if (p.getTipo().equalsIgnoreCase(tipo)) resultado.add(p);
        return resultado;
    }

    @Override
    public ArrayList<Producto> listarVencidos() {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (Producto p : productos)
            if ("Perecible".equals(p.getTipo()) && p.estaVencido()) resultado.add(p);
        return resultado;
    }

    @Override
    public ArrayList<Producto> listarProximosAVencer() {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (Producto p : productos)
            if ("Perecible".equals(p.getTipo()) && p.estaProximoAVencer() && !p.estaVencido())
                resultado.add(p);
        return resultado;
    }

    @Override
    public ArrayList<Producto> listarConDescuento() {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (Producto p : productos)
            if ("No Perecible".equals(p.getTipo()) && p.getPesoKg() > 5.0) resultado.add(p);
        return resultado;
    }

    @Override
    public ArrayList<Producto> listarProductosPorCategoria(String cve) {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (Producto p : productos)
            if (p.getCategoria() != null &&
                    p.getCategoria().getCveCategoria().equalsIgnoreCase(cve))
                resultado.add(p);
        return resultado;
    }

    @Override
    public ArrayList<Producto> listarProductosPorMarca(String marca) {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (Producto p : productos)
            if ("No Perecible".equals(p.getTipo()) &&
                    p.getMarca().equalsIgnoreCase(marca)) resultado.add(p);
        return resultado;
    }

    @Override
    public ArrayList<Producto> buscarProductosPor(Predicate<Producto> condicion) {
        return productos.stream()
                .filter(condicion)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Producto> buscarProductos(CriteriosProducto criterios) {
        return buscarProductosPor(criterios.aPredicate());
    }

    @Override
    public Map<String, List<Producto>> agruparProductosPorCategoria() {
        return productos.stream()
                .filter(p -> p.getCategoria() != null)
                .collect(Collectors.groupingBy(p -> p.getCategoria().getCveCategoria()));
    }

    @Override
    public ArrayList<Producto> listarCatalogo() {
        return new ArrayList<>(productos);
    }

    @Override
    public int conteoTotal() {
        return productos.size();
    }

    @Override
    public double calcularPrecioPromedio() {
        return productos.stream()
                .mapToDouble(Producto::calcularPrecioFinal)
                .average()
                .orElse(0.0);
    }

    @Override
    public double calcularValorTotalInventario() {
        return inventarios.stream()
                .mapToDouble(inv -> inv.getProducto().calcularPrecioFinal() * inv.getCantidadDisponible())
                .sum();
    }

    @Override
    public ArrayList<Producto> listarProductosOrdenadosPorPrecio() {
        return productos.stream()
                .sorted(Comparator.comparingDouble(Producto::calcularPrecioFinal)
                        .thenComparing(Producto::getNombre))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Map<Boolean, List<Producto>> particionarPorDescuento() {
        return productos.stream()
                .collect(Collectors.partitioningBy(p -> p.calcularDescuento() > 0));
    }

    @Override
    public Map<String, Long> contarProductosPorCategoria() {
        return productos.stream()
                .filter(p -> p.getCategoria() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria().getCveCategoria(),
                        Collectors.counting()));
    }

    @Override
    public Optional<Producto> buscarMasCaro() {
        return productos.stream()
                .max(Comparator.comparingDouble(Producto::calcularPrecioFinal));
    }

    @Override
    public <R> ArrayList<R> extraerCampo(Function<Producto, R> extractor) {
        return productos.stream()
                .map(extractor)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public double calcularTotalDescuentos(ReglaDescuento regla) {
        return productos.stream()
                .mapToDouble(regla::aplicar)
                .sum();
    }

    // ── Inventario ───────────────────────────────────────────────────────────

    @Override
    public void agregarInventario(Inventario inv) throws FachadaException {
        for (Inventario i : inventarios)
            if (i.getProducto() != null &&
                    i.getProducto().getCodigo().equalsIgnoreCase(
                            inv.getProducto().getCodigo()))
                throw new FachadaException("Ya existe inventario para: " +
                        inv.getProducto().getCodigo());
        inv.setIdInventario(GeneradorId.siguienteId(inventarios, Inventario::getIdInventario));
        inventarios.add(inv);
        guardarInventarios();
    }

    @Override
    public Inventario buscarInventario(int id) throws FachadaException {
        for (Inventario inv : inventarios)
            if (inv.getIdInventario() == id) return inv;
        throw new FachadaException("Inventario no encontrado: id=" + id);
    }

    @Override
    public void actualizarInventario(Inventario inv) throws FachadaException {
        for (int i = 0; i < inventarios.size(); i++) {
            if (inventarios.get(i).getIdInventario() == inv.getIdInventario()) {
                inventarios.set(i, inv);
                guardarInventarios();
                return;
            }
        }
        throw new FachadaException("Inventario no encontrado para actualizar: id=" + inv.getIdInventario());
    }

    @Override
    public void inactivarInventario(int id) throws FachadaException {
        for (Inventario inv : inventarios) {
            if (inv.getIdInventario() == id) {
                inv.setActivo(false);
                guardarInventarios();
                return;
            }
        }
        throw new FachadaException("Inventario no encontrado para inactivar: id=" + id);
    }

    @Override
    public void retirarStock(String codigoProducto, int cantidad)
            throws StockInsuficienteException, FachadaException {
        for (Inventario inv : inventarios) {
            if (inv.getProducto() != null &&
                    inv.getProducto().getCodigo().equalsIgnoreCase(codigoProducto)) {
                inv.retirar(cantidad);
                guardarInventarios();
                return;
            }
        }
        throw new FachadaException("Inventario no encontrado para: " + codigoProducto);
    }

    @Override
    public ArrayList<Inventario> listarInventarios() {
        return new ArrayList<>(inventarios);
    }

    @Override
    public ArrayList<Inventario> listarInventariosActivos() {
        ArrayList<Inventario> resultado = new ArrayList<>();
        for (Inventario inv : inventarios)
            if (inv.isActivo()) resultado.add(inv);
        return resultado;
    }

    @Override
    public ArrayList<Inventario> listarInventariosConAlerta() {
        ArrayList<Inventario> resultado = new ArrayList<>();
        for (Inventario inv : inventarios)
            if (inv.requiereAlerta()) resultado.add(inv);
        return resultado;
    }

    // ── Ventas ───────────────────────────────────────────────────────────────

    @Override
    public void registrarVenta(Venta v) throws FachadaException {
        v.setIdVenta(GeneradorId.siguienteId(ventas, Venta::getIdVenta));
        ventas.add(v);
        v.crear();
        guardarVentas();
    }

    @Override
    public Venta buscarVenta(int idVenta) throws FachadaException {
        for (Venta v : ventas)
            if (v.getIdVenta() == idVenta) return v;
        throw new FachadaException("Venta no encontrada: id=" + idVenta);
    }

    @Override
    public void actualizarVenta(Venta v) throws FachadaException {
        for (int i = 0; i < ventas.size(); i++) {
            if (ventas.get(i).getIdVenta() == v.getIdVenta()) {
                ventas.set(i, v);
                guardarVentas();
                return;
            }
        }
        throw new FachadaException("Venta no encontrada para actualizar: id=" + v.getIdVenta());
    }

    @Override
    public void inactivarVenta(int idVenta) throws FachadaException {
        Venta encontrada = buscarVenta(idVenta);
        encontrada.anular();
        guardarVentas();
    }

    @Override
    public ArrayList<Venta> listarVentas() { return new ArrayList<>(ventas); }

    @Override
    public ArrayList<Venta> listarVentasActivas() {
        ArrayList<Venta> resultado = new ArrayList<>();
        for (Venta v : ventas)
            if (v.getEstado() == Venta.Estado.ACTIVA) resultado.add(v);
        return resultado;
    }

    @Override
    public ArrayList<Venta> listarVentasAnuladas() {
        ArrayList<Venta> resultado = new ArrayList<>();
        for (Venta v : ventas)
            if (v.getEstado() == Venta.Estado.ANULADA) resultado.add(v);
        return resultado;
    }

    // ── Facturas ─────────────────────────────────────────────────────────────

    @Override
    public void emitirFactura(Factura f) throws FachadaException {
        facturas.add(f);
        f.crear();
        guardarFacturas();

        for (edu.uce.programacion2.tienda.negocio.DetalleFactura d : f.getDetalles()) {
            detallesFactura.add(new edu.uce.programacion2.tienda.persistencia.DetalleFactura.Registro(
                    f.getIdFactura(), d));
        }
        guardarDetallesFactura();
    }

    /** Retorna todos los registros de detalle de factura (todas las facturas). */
    public ArrayList<edu.uce.programacion2.tienda.persistencia.DetalleFactura.Registro> listarDetallesFactura() {
        return new ArrayList<>(detallesFactura);
    }

    /** Retorna los detalles pertenecientes a una factura específica. */
    public ArrayList<edu.uce.programacion2.tienda.negocio.DetalleFactura> listarDetallesPorFactura(int idFactura) {
        ArrayList<edu.uce.programacion2.tienda.negocio.DetalleFactura> resultado = new ArrayList<>();
        for (edu.uce.programacion2.tienda.persistencia.DetalleFactura.Registro r : detallesFactura)
            if (r.getIdFactura() == idFactura) resultado.add(r.getDetalle());
        return resultado;
    }

    @Override
    public Factura buscarFactura(String numero) throws FachadaException {
        for (Factura f : facturas)
            if (f.getNumeroFactura().equalsIgnoreCase(numero)) return f;
        throw new FachadaException("Factura no encontrada: " + numero);
    }

    @Override
    public ArrayList<Factura> listarFacturas() { return new ArrayList<>(facturas); }

    @Override
    public ArrayList<Factura> listarFacturasEmitidas() {
        ArrayList<Factura> resultado = new ArrayList<>();
        for (Factura f : facturas)
            if (f.getEstado() == Factura.EstadoFactura.EMITIDA) resultado.add(f);
        return resultado;
    }

    @Override
    public ArrayList<Factura> listarFacturasAnuladas() {
        ArrayList<Factura> resultado = new ArrayList<>();
        for (Factura f : facturas)
            if (f.getEstado() == Factura.EstadoFactura.ANULADA) resultado.add(f);
        return resultado;
    }

    /**
     * Búsqueda genérica de facturas por cualquier condición funcional.
     * Análoga a {@link #buscarProductosPor}.
     */
    @Override
    public ArrayList<Factura> buscarFacturasPor(Predicate<Factura> condicion) {
        return facturas.stream()
                .filter(condicion)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Búsqueda avanzada de facturas: recibe varios parámetros opcionales a la
     * vez (rango de fechas, rango de total, cliente, número de factura,
     * IVA especial, estado) empaquetados en {@link CriteriosFactura}.
     */
    @Override
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

    // ── Proveedores ──────────────────────────────────────────────────────────

    @Override
    public void agregarProveedor(Proveedor p) throws FachadaException {
        p.setIdProveedor(GeneradorId.siguienteId(proveedores, Proveedor::getIdProveedor));
        proveedores.add(p);
        p.crear();
        guardarProveedores();
    }

    @Override
    public Proveedor buscarProveedor(int id) throws FachadaException {
        for (Proveedor p : proveedores)
            if (p.getIdProveedor() == id) return p;
        throw new FachadaException("Proveedor no encontrado: id=" + id);
    }

    @Override
    public Proveedor buscarProveedorPorRuc(String ruc) throws FachadaException {
        for (Proveedor p : proveedores)
            if (p.getRuc().equalsIgnoreCase(ruc)) return p;
        throw new FachadaException("Proveedor no encontrado con RUC: " + ruc);
    }

    @Override
    public ArrayList<Proveedor> listarProveedores() { return new ArrayList<>(proveedores); }

    @Override
    public ArrayList<Proveedor> listarProveedoresActivos() {
        ArrayList<Proveedor> resultado = new ArrayList<>();
        for (Proveedor p : proveedores)
            if (p.isActivo()) resultado.add(p);
        return resultado;
    }

    @Override
    public void actualizarProveedor(Proveedor p) throws FachadaException {
        for (int i = 0; i < proveedores.size(); i++) {
            if (proveedores.get(i).getIdProveedor() == p.getIdProveedor()) {
                proveedores.set(i, p);
                guardarProveedores();
                return;
            }
        }
        throw new FachadaException("Proveedor no encontrado para actualizar: id=" + p.getIdProveedor());
    }

    @Override
    public void inactivarProveedor(int id) throws FachadaException {
        for (Proveedor p : proveedores) {
            if (p.getIdProveedor() == id) {
                p.setActivo(false);
                guardarProveedores();
                return;
            }
        }
        throw new FachadaException("Proveedor no encontrado para inactivar: id=" + id);
    }

    // ── Compras ──────────────────────────────────────────────────────────────

    @Override
    public void registrarCompra(Compra c) throws FachadaException {
        c.setIdCompra(GeneradorId.siguienteId(compras, Compra::getIdCompra));
        compras.add(c);
        c.crear();
        guardarCompras();
    }

    @Override
    public void recibirCompra(Compra c) throws FachadaException {
        c.recibirCompra();
        for (DetalleCompra dc : c.getDetalles()) {
            for (Inventario inv : inventarios) {
                if (inv.getProducto() != null &&
                        inv.getProducto().getCodigo().equalsIgnoreCase(
                                dc.getProducto().getCodigo())) {
                    inv.agregar(dc.getCantidad());
                }
            }
        }
        guardarInventarios();
        guardarCompras();
    }

    @Override
    public void actualizarCompra(Compra c) throws FachadaException {
        for (int i = 0; i < compras.size(); i++) {
            if (compras.get(i).getIdCompra() == c.getIdCompra()) {
                compras.set(i, c);
                guardarCompras();
                return;
            }
        }
        throw new FachadaException("Compra no encontrada para actualizar: id=" + c.getIdCompra());
    }

    @Override
    public void inactivarCompra(int idCompra) throws FachadaException {
        Compra encontrada = null;
        for (Compra c : compras)
            if (c.getIdCompra() == idCompra) { encontrada = c; break; }
        if (encontrada == null)
            throw new FachadaException("Compra no encontrada para inactivar: id=" + idCompra);
        encontrada.anular();
        guardarCompras();
    }

    @Override
    public ArrayList<Compra> listarCompras() { return new ArrayList<>(compras); }

    @Override
    public ArrayList<Compra> listarComprasActivas() {
        ArrayList<Compra> resultado = new ArrayList<>();
        for (Compra c : compras)
            if (c.getEstado() != Compra.Estado.ANULADA) resultado.add(c);
        return resultado;
    }

    @Override
    public ArrayList<Compra> listarComprasPendientes() {
        ArrayList<Compra> resultado = new ArrayList<>();
        for (Compra c : compras)
            if (c.getEstado() == Compra.Estado.PENDIENTE) resultado.add(c);
        return resultado;
    }

    @Override
    public ArrayList<Compra> listarComprasRecibidas() {
        ArrayList<Compra> resultado = new ArrayList<>();
        for (Compra c : compras)
            if (c.getEstado() == Compra.Estado.RECIBIDA) resultado.add(c);
        return resultado;
    }

    @Override
    public ArrayList<Compra> buscarComprasPor(Predicate<Compra> condicion) {
        return compras.stream()
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
        for (Usuario existente : usuarios)
            if (existente.getEmail().equalsIgnoreCase(u.getEmail()))
                throw new FachadaException("Ya existe un usuario con el email: " + u.getEmail());
        u.setIdUsuario(GeneradorId.siguienteId(usuarios, Usuario::getIdUsuario));
        usuarios.add(u);
        guardarUsuarios();
    }

    @Override
    public void actualizarUsuario(Usuario u) throws FachadaException {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getIdUsuario() == u.getIdUsuario()) {
                usuarios.set(i, u);
                guardarUsuarios();
                return;
            }
        }
        throw new FachadaException("Usuario no encontrado para actualizar: id=" + u.getIdUsuario());
    }

    @Override
    public void inactivarUsuario(int id) throws FachadaException {
        for (Usuario u : usuarios) {
            if (u.getIdUsuario() == id) {
                u.setActivo(false);
                guardarUsuarios();
                return;
            }
        }
        throw new FachadaException("Usuario no encontrado para inactivar: id=" + id);
    }

    @Override
    public Usuario buscarUsuario(int idUsuario) throws FachadaException {
        for (Usuario u : usuarios)
            if (u.getIdUsuario() == idUsuario) return u;
        throw new FachadaException("Usuario no encontrado: id=" + idUsuario);
    }

    @Override
    public Usuario buscarUsuarioPorEmail(String email) throws FachadaException {
        for (Usuario u : usuarios)
            if (u.getEmail().equalsIgnoreCase(email)) return u;
        throw new FachadaException("Usuario no encontrado con email: " + email);
    }

    @Override
    public ArrayList<Usuario> listarUsuarios() { return new ArrayList<>(usuarios); }

    @Override
    public ArrayList<Usuario> listarUsuariosActivos() {
        ArrayList<Usuario> resultado = new ArrayList<>();
        for (Usuario u : usuarios)
            if (u.isActivo()) resultado.add(u);
        return resultado;
    }

    @Override
    public ArrayList<Usuario> listarUsuariosPorPermiso(String permiso) {
        ArrayList<Usuario> resultado = new ArrayList<>();
        for (Usuario u : usuarios)
            if (u.getPermiso().equalsIgnoreCase(permiso)) resultado.add(u);
        return resultado;
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
        for (Rol existente : roles)
            if (existente.getNombreCargo().equalsIgnoreCase(r.getNombreCargo()))
                throw new FachadaException("Ya existe un rol con el cargo: " + r.getNombreCargo());
        r.setIdRol(GeneradorId.siguienteId(roles, Rol::getIdRol));
        roles.add(r);
        guardarRoles();
    }

    @Override
    public Rol buscarRol(int idRol) throws FachadaException {
        for (Rol r : roles)
            if (r.getIdRol() == idRol) return r;
        throw new FachadaException("Rol no encontrado. Id: " + idRol);
    }

    @Override
    public void actualizarRol(Rol r) throws FachadaException {
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).getIdRol() == r.getIdRol()) {
                roles.set(i, r);
                guardarRoles();
                return;
            }
        }
        throw new FachadaException("Rol no encontrado para actualizar. Id: " + r.getIdRol());
    }

    @Override
    public void inactivarRol(int idRol) throws FachadaException {
        for (Rol r : roles) {
            if (r.getIdRol() == idRol) {
                r.setActivo(false);
                guardarRoles();
                return;
            }
        }
        throw new FachadaException("Rol no encontrado para inactivar. Id: " + idRol);
    }

    @Override
    public ArrayList<Rol> listarRoles() {
        return new ArrayList<>(roles);
    }

    @Override
    public ArrayList<Rol> listarRolesActivos() {
        ArrayList<Rol> resultado = new ArrayList<>();
        for (Rol r : roles)
            if (r.isActivo()) resultado.add(r);
        return resultado;
    }
}