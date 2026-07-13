package edu.uce.programacion2.tienda.control;

import edu.uce.programacion2.tienda.excepciones.FachadaException;
import edu.uce.programacion2.tienda.fachadas.FachadaArchivos;
import edu.uce.programacion2.tienda.fachadas.FachadaTienda;
import edu.uce.programacion2.tienda.interfaces.IFachadaTienda;
import edu.uce.programacion2.tienda.negocio.*;
import edu.uce.programacion2.tienda.interfaz.*;
import edu.uce.programacion2.tienda.objetosServicio.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Capa de control que actúa como intermediario entre {@link VentanaPrincipal}
 * y {@link FachadaTienda}. Centraliza las operaciones CRUD de catálogos y
 * las consultas, devolviendo objetos {@link Tabla} listos para mostrarse en
 * {@link PanelTabla}.
 *
 * @author Ana
 */
public class Control {

    private IFachadaTienda fachada;
    private Conversiones   conversiones = new Conversiones();

    public Control() {
        try {
            this.fachada = new FachadaArchivos();
        } catch (FachadaException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar archivos de datos: " + e.getMessage() +
                            "\nSe usará almacenamiento en memoria.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            this.fachada = new FachadaTienda();
        }
    }

    public IFachadaTienda getFachada() {
        return fachada;
    }

    // ── CRUD Categorías ─────────────────────────────────────────────────────

    public void agregarCategoria(JFrame frame) {
        String clave = JOptionPane.showInputDialog(frame, "Clave de la categoría:", "Agregar Categoría", JOptionPane.QUESTION_MESSAGE);
        if (clave == null || clave.trim().isEmpty()) return;
        Categoria existente = buscarCategoriaSilencioso(clave.trim());
        if (existente == null) {
            DlgCategoria dlg = new DlgCategoria(frame, fachada, DlgCategoria.Modo.AGREGAR, clave.trim());
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame, "La categoría ya existe.", "Ya existe", JOptionPane.INFORMATION_MESSAGE);
            DlgCategoria dlg = new DlgCategoria(frame, fachada, DlgCategoria.Modo.VER, existente);
            dlg.setVisible(true);
        }
    }

    public void actualizarCategoria(JFrame frame) {
        String clave = JOptionPane.showInputDialog(frame, "Clave de la categoría:", "Actualizar Categoría", JOptionPane.QUESTION_MESSAGE);
        if (clave == null || clave.trim().isEmpty()) return;
        Categoria existente = buscarCategoriaSilencioso(clave.trim());
        if (existente != null) {
            DlgCategoria dlg = new DlgCategoria(frame, fachada, DlgCategoria.Modo.ACTUALIZAR, existente);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame, "La categoría no existe.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void inactivarCategoria(JFrame frame) {
        String clave = JOptionPane.showInputDialog(frame, "Clave de la categoría:", "Inactivar Categoría", JOptionPane.QUESTION_MESSAGE);
        if (clave == null || clave.trim().isEmpty()) return;
        Categoria existente = buscarCategoriaSilencioso(clave.trim());
        if (existente != null) {
            DlgCategoria dlg = new DlgCategoria(frame, fachada, DlgCategoria.Modo.INACTIVAR, existente);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame, "La categoría no existe.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── CRUD Productos (unificado) ───────────────────────────────────────────

    public void agregarProducto(JFrame frame) {
        String codigo = JOptionPane.showInputDialog(frame, "Código del producto:", "Agregar Producto", JOptionPane.QUESTION_MESSAGE);
        if (codigo == null || codigo.trim().isEmpty()) return;
        Producto existente = buscarProductoSilencioso(codigo.trim());
        if (existente == null) {
            DlgProducto dlg = new DlgProducto(frame, fachada, DlgProducto.Modo.AGREGAR, codigo.trim());
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame, "El producto ya está en el catálogo.", "Ya existe", JOptionPane.INFORMATION_MESSAGE);
            DlgProducto dlg = new DlgProducto(frame, fachada, DlgProducto.Modo.VER, existente);
            dlg.setVisible(true);
        }
    }

    public void actualizarProducto(JFrame frame) {
        String codigo = JOptionPane.showInputDialog(frame, "Código del producto:", "Actualizar Producto", JOptionPane.QUESTION_MESSAGE);
        if (codigo == null || codigo.trim().isEmpty()) return;
        Producto existente = buscarProductoSilencioso(codigo.trim());
        if (existente != null) {
            DlgProducto dlg = new DlgProducto(frame, fachada, DlgProducto.Modo.ACTUALIZAR, existente);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame, "El producto no existe.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void inactivarProducto(JFrame frame) {
        String codigo = JOptionPane.showInputDialog(frame, "Código del producto:", "Inactivar Producto", JOptionPane.QUESTION_MESSAGE);
        if (codigo == null || codigo.trim().isEmpty()) return;
        Producto existente = buscarProductoSilencioso(codigo.trim());
        if (existente != null) {
            DlgProducto dlg = new DlgProducto(frame, fachada, DlgProducto.Modo.INACTIVAR, existente);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame, "El producto no existe.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Inventario ───────────────────────────────────────────────────────────

    public void agregarInventario(JFrame frame) {
        DlgInventario dlg = new DlgInventario(frame, fachada, DlgInventario.Modo.AGREGAR);
        dlg.setVisible(true);
    }

    public void actualizarInventario(JFrame frame) {
        String entrada = JOptionPane.showInputDialog(frame,
                "ID del inventario a actualizar:", "Actualizar Inventario",
                JOptionPane.QUESTION_MESSAGE);
        if (entrada == null || entrada.trim().isEmpty()) return;
        Inventario existente = buscarInventarioSilencioso(entrada.trim());
        if (existente != null) {
            DlgInventario dlg = new DlgInventario(frame, fachada, DlgInventario.Modo.ACTUALIZAR, existente);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "No existe un inventario con ID: " + entrada.trim(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void inactivarInventario(JFrame frame) {
        String entrada = JOptionPane.showInputDialog(frame,
                "ID del inventario a inactivar:", "Inactivar Inventario",
                JOptionPane.QUESTION_MESSAGE);
        if (entrada == null || entrada.trim().isEmpty()) return;
        Inventario existente = buscarInventarioSilencioso(entrada.trim());
        if (existente != null) {
            DlgInventario dlg = new DlgInventario(frame, fachada, DlgInventario.Modo.INACTIVAR, existente);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "No existe un inventario con ID: " + entrada.trim(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Proveedores ──────────────────────────────────────────────────────────

    public void agregarProveedor(JFrame frame) {
        DlgProveedor dlg = new DlgProveedor(frame, fachada, DlgProveedor.Modo.AGREGAR);
        dlg.setVisible(true);
    }

    public void actualizarProveedor(JFrame frame) {
        String entrada = JOptionPane.showInputDialog(frame,
                "ID del proveedor a actualizar:", "Actualizar Proveedor",
                JOptionPane.QUESTION_MESSAGE);
        if (entrada == null || entrada.trim().isEmpty()) return;
        Proveedor existente = buscarProveedorSilencioso(entrada.trim());
        if (existente != null) {
            DlgProveedor dlg = new DlgProveedor(frame, fachada, DlgProveedor.Modo.ACTUALIZAR, existente);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "No existe un proveedor con ID: " + entrada.trim(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void inactivarProveedor(JFrame frame) {
        String entrada = JOptionPane.showInputDialog(frame,
                "ID del proveedor a inactivar:", "Inactivar Proveedor",
                JOptionPane.QUESTION_MESSAGE);
        if (entrada == null || entrada.trim().isEmpty()) return;
        Proveedor existente = buscarProveedorSilencioso(entrada.trim());
        if (existente != null) {
            DlgProveedor dlg = new DlgProveedor(frame, fachada, DlgProveedor.Modo.INACTIVAR, existente);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "No existe un proveedor con ID: " + entrada.trim(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Ventas ────────────────────────────────────────────────────────────

    public void agregarVenta(JFrame frame) {
        DlgVenta dlg = new DlgVenta(frame, fachada, DlgVenta.Modo.AGREGAR);
        dlg.setVisible(true);
    }

    public void actualizarVenta(JFrame frame) {
        DlgVenta dlg = new DlgVenta(frame, fachada, DlgVenta.Modo.ACTUALIZAR);
        dlg.setVisible(true);
    }

    public void inactivarVenta(JFrame frame) {
        DlgVenta dlg = new DlgVenta(frame, fachada, DlgVenta.Modo.INACTIVAR);
        dlg.setVisible(true);
    }

    // ── Compras ───────────────────────────────────────────────────────────

    public void agregarCompra(JFrame frame) {
        DlgCompra dlg = new DlgCompra(frame, fachada, DlgCompra.Modo.AGREGAR);
        dlg.setVisible(true);
    }

    public void actualizarCompra(JFrame frame) {
        DlgCompra dlg = new DlgCompra(frame, fachada, DlgCompra.Modo.ACTUALIZAR);
        dlg.setVisible(true);
    }

    public void inactivarCompra(JFrame frame) {
        DlgCompra dlg = new DlgCompra(frame, fachada, DlgCompra.Modo.INACTIVAR);
        dlg.setVisible(true);
    }

    // ── Usuarios ─────────────────────────────────────────────────────────────

    public void agregarUsuario(JFrame frame) {
        DlgUsuario dlg = new DlgUsuario(frame, fachada, DlgUsuario.Modo.AGREGAR);
        dlg.setVisible(true);
    }

    public void actualizarUsuario(JFrame frame) {
        String entrada = JOptionPane.showInputDialog(frame,
                "ID del usuario a actualizar:", "Actualizar Usuario",
                JOptionPane.QUESTION_MESSAGE);
        if (entrada == null || entrada.trim().isEmpty()) return;
        Usuario existente = buscarUsuarioSilencioso(entrada.trim());
        if (existente != null) {
            DlgUsuario dlg = new DlgUsuario(frame, fachada, DlgUsuario.Modo.ACTUALIZAR, existente);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "No existe un usuario con ID: " + entrada.trim(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void inactivarUsuario(JFrame frame) {
        String entrada = JOptionPane.showInputDialog(frame,
                "ID del usuario a inactivar:", "Inactivar Usuario",
                JOptionPane.QUESTION_MESSAGE);
        if (entrada == null || entrada.trim().isEmpty()) return;
        Usuario existente = buscarUsuarioSilencioso(entrada.trim());
        if (existente != null) {
            DlgUsuario dlg = new DlgUsuario(frame, fachada, DlgUsuario.Modo.INACTIVAR, existente);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "No existe un usuario con ID: " + entrada.trim(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Roles ────────────────────────────────────────────────────────────────

    public void agregarRol(JFrame frame) {
        DlgRol dlg = new DlgRol(frame, fachada, DlgRol.Modo.AGREGAR);
        dlg.setVisible(true);
    }

    public void actualizarRol(JFrame frame) {
        String nombreCargo = JOptionPane.showInputDialog(frame,
                "Nombre del cargo a actualizar:", "Actualizar Rol",
                JOptionPane.QUESTION_MESSAGE);
        if (nombreCargo == null || nombreCargo.trim().isEmpty()) return;
        Rol existente = buscarRolSilencioso(nombreCargo.trim());
        if (existente != null) {
            DlgRol dlg = new DlgRol(frame, fachada, DlgRol.Modo.ACTUALIZAR, existente);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "No existe un rol con el cargo: " + nombreCargo.trim(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void inactivarRol(JFrame frame) {
        String nombreCargo = JOptionPane.showInputDialog(frame,
                "Nombre del cargo a inactivar:", "Inactivar Rol",
                JOptionPane.QUESTION_MESSAGE);
        if (nombreCargo == null || nombreCargo.trim().isEmpty()) return;
        Rol existente = buscarRolSilencioso(nombreCargo.trim());
        if (existente != null) {
            DlgRol dlg = new DlgRol(frame, fachada, DlgRol.Modo.INACTIVAR, existente);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "No existe un rol con el cargo: " + nombreCargo.trim(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Consultas → retornan Tabla ──────────────────────────────────────────

    /** Tabla de productos ACTIVOS (consulta de negocio típica). */
    public Tabla getTablaProductos(JFrame frame) {
        try {
            return conversiones.catalogoATabla(fachada.listarProductosActivos());
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Tabla con TODOS los productos, incluidos inactivos (para "Ver todos"/auditoría). */
    public Tabla getTablaTodosProductos(JFrame frame) {
        try {
            return conversiones.catalogoATabla(fachada.listarProductos());
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public Tabla getTablaProductosPorTipo(JFrame frame, String tipo) {
        return conversiones.catalogoATabla(fachada.listarProductosPorTipo(tipo));
    }

    public Tabla getTablaVencidos(JFrame frame) {
        return conversiones.catalogoATabla(fachada.listarVencidos());
    }

    public Tabla getTablaProximosAVencer(JFrame frame) {
        return conversiones.catalogoATabla(fachada.listarProximosAVencer());
    }

    public Tabla getTablaConDescuento(JFrame frame) {
        return conversiones.catalogoATabla(fachada.listarConDescuento());
    }

    public Tabla getTablaProductosPorCategoria(JFrame frame, String clave) {
        return conversiones.catalogoATabla(fachada.listarProductosPorCategoria(clave));
    }

    public Tabla getTablaProductosPorMarca(JFrame frame, String marca) {
        return conversiones.catalogoATabla(fachada.listarProductosPorMarca(marca));
    }

    public Tabla getTablaProductosPorRangoPrecio(JFrame frame, double min, double max) {
        return conversiones.catalogoATabla(
                fachada.buscarProductosPor(p -> p.getPrecioUnitario() >= min && p.getPrecioUnitario() <= max));
    }

    /**
     * Búsqueda avanzada: recibe varios parámetros opcionales a la vez
     * (nombre, categoría, tipo, marca, rango de precio, solo activos,
     * solo con descuento) empaquetados en {@link CriteriosProducto}.
     */
    public Tabla getTablaProductosPorCriterios(JFrame frame, CriteriosProducto criterios) {
        return conversiones.catalogoATabla(fachada.buscarProductos(criterios));
    }

    public String getResumenPorCategoria(JFrame frame) {
        Map<String, java.util.List<Producto>> grupos = fachada.agruparProductosPorCategoria();
        StringBuilder sb = new StringBuilder("Productos por categoría:\n\n");
        for (Map.Entry<String, java.util.List<Producto>> entrada : grupos.entrySet()) {
            sb.append(entrada.getKey())
                    .append(": ")
                    .append(entrada.getValue().size())
                    .append(" producto(s)\n");
        }
        return sb.toString();
    }

    /**
     * Muestra el diálogo de selección de período y, si el usuario acepta,
     * retorna la tabla de productos cuya fecha de vencimiento cae en ese
     * rango. Si el usuario cancela, retorna {@code null}.
     */
    public Tabla getTablaProductosPorPeriodo(JFrame frame) {
        DlgPeriodo dlg = new DlgPeriodo(frame);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) return null;
        try {
            ArrayList<Producto> lista = filtrarPorPeriodo(
                    fachada.listarProductos(), dlg.getPeriodo());
            return conversiones.catalogoATabla(lista);
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public Tabla getTablaCatalogo(JFrame frame) {
        return conversiones.catalogoATabla(fachada.listarCatalogo());
    }

    /** Tabla de categorías ACTIVAS (consulta de negocio típica). */
    public Tabla getTablaCategorias(JFrame frame) {
        try {
            return conversiones.categoriasATabla(fachada.listarCategoriasActivas());
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Tabla con TODAS las categorías, incluidas inactivas (para "Ver todos"/auditoría). */
    public Tabla getTablaTodasCategorias(JFrame frame) {
        try {
            return conversiones.categoriasATabla(fachada.listarCategorias());
        } catch (FachadaException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Tabla de inventarios ACTIVOS (consulta de negocio típica). */
    public Tabla getTablaInventarios(JFrame frame) {
        return conversiones.inventariosATabla(fachada.listarInventariosActivos());
    }

    /** Tabla con TODOS los inventarios, incluidos inactivos (para "Ver todos"/auditoría). */
    public Tabla getTablaTodosInventarios(JFrame frame) {
        return conversiones.inventariosATabla(fachada.listarInventarios());
    }

    /**
     * Retorna la tabla de inventarios con alerta, o {@code null} si la
     * lista está vacía (y se notifica al usuario).
     */
    public Tabla getTablaInventariosConAlerta(JFrame frame) {
        ArrayList<Inventario> lista = fachada.listarInventariosConAlerta();
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No hay inventarios con alerta.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return conversiones.inventariosATabla(lista);
    }

    /**
     * Búsqueda avanzada de inventarios: recibe varios parámetros opcionales
     * a la vez (producto, categoría, rango de stock, rango de fechas, solo
     * con alerta, solo activos) empaquetados en {@link CriteriosInventario}.
     * Análoga a {@link #getTablaProductosPorCriterios}.
     */
    public Tabla getTablaInventariosPorCriterios(JFrame frame, CriteriosInventario criterios) {
        return conversiones.inventariosATabla(fachada.buscarInventarios(criterios));
    }

    /** Tabla de proveedores ACTIVOS (consulta de negocio típica). */
    public Tabla getTablaProveedores(JFrame frame) {
        return conversiones.proveedoresATabla(fachada.listarProveedoresActivos());
    }

    /** Tabla con TODOS los proveedores, incluidos inactivos (para "Ver todos"/auditoría). */
    public Tabla getTablaTodosProveedores(JFrame frame) {
        return conversiones.proveedoresATabla(fachada.listarProveedores());
    }

    /**
     * Búsqueda avanzada de proveedores: recibe varios parámetros opcionales
     * a la vez (nombre, RUC, teléfono, email, dirección, solo activos)
     * empaquetados en {@link CriteriosProveedor}.
     * Análoga a {@link #getTablaProductosPorCriterios}.
     */
    public Tabla getTablaProveedoresPorCriterios(JFrame frame, CriteriosProveedor criterios) {
        return conversiones.proveedoresATabla(fachada.buscarProveedores(criterios));
    }

    public Tabla getTablaVentas(JFrame frame) {
        return conversiones.ventasATabla(fachada.listarVentas());
    }

    public Tabla getTablaCompras(JFrame frame) {
        return conversiones.comprasATabla(fachada.listarCompras());
    }

    /**
     * Búsqueda avanzada de compras: recibe varios parámetros opcionales a la
     * vez (proveedor, rango de fechas, rango de total, producto, categoría,
     * estado) empaquetados en {@link CriteriosCompra}.
     * Análoga a {@link #getTablaProductosPorCriterios}.
     */
    public Tabla getTablaComprasPorCriterios(JFrame frame, CriteriosCompra criterios) {
        return conversiones.comprasATabla(fachada.buscarCompras(criterios));
    }

    public Tabla getTablaFacturas(JFrame frame) {
        return conversiones.facturasATabla(fachada.listarFacturas());
    }

    /**
     * Búsqueda avanzada de facturas: recibe varios parámetros opcionales a la
     * vez (rango de fechas, rango de total, cliente, número de factura,
     * IVA especial, estado) empaquetados en {@link CriteriosFactura}.
     * Análoga a {@link #getTablaProductosPorCriterios}.
     */
    public Tabla getTablaFacturasPorCriterios(JFrame frame, CriteriosFactura criterios) {
        return conversiones.facturasATabla(fachada.buscarFacturas(criterios));
    }

    /**
     * Búsqueda avanzada de ventas: recibe varios parámetros opcionales a la
     * vez (rango de fechas, cliente, método de pago, rango de total, solo
     * con descuento, producto/categoría incluidos) empaquetados en
     * {@link CriteriosVenta}. Análoga a {@link #getTablaProductosPorCriterios}.
     */
    public Tabla getTablaVentasPorCriterios(JFrame frame, CriteriosVenta criterios) {
        return conversiones.facturasATabla(fachada.buscarFacturas(criterios));
    }

    /** Tabla de usuarios ACTIVOS (consulta de negocio típica). */
    public Tabla getTablaUsuarios(JFrame frame) {
        return conversiones.usuariosATabla(fachada.listarUsuariosActivos());
    }

    /** Tabla con TODOS los usuarios, incluidos inactivos (para "Ver todos"/auditoría). */
    public Tabla getTablaTodosUsuarios(JFrame frame) {
        return conversiones.usuariosATabla(fachada.listarUsuarios());
    }

    /**
     * Búsqueda avanzada de usuarios: recibe varios parámetros opcionales a
     * la vez (nombre/email parcial, rol, solo activos) empaquetados en
     * {@link CriteriosUsuario}. Análoga a {@link #getTablaProductosPorCriterios}.
     */
    public Tabla getTablaUsuariosPorCriterios(JFrame frame, CriteriosUsuario criterios) {
        return conversiones.usuariosATabla(fachada.buscarUsuarios(criterios));
    }

    /** Tabla de roles ACTIVOS. */
    public Tabla getTablaRoles(JFrame frame) {
        return conversiones.rolesATabla(fachada.listarRolesActivos());
    }

    /** Tabla con TODOS los roles, incluidos inactivos. */
    public Tabla getTablaTodosRoles(JFrame frame) {
        return conversiones.rolesATabla(fachada.listarRoles());
    }

    // ── Utilidades ───────────────────────────────────────────────────────────

    private Categoria buscarCategoriaSilencioso(String clave) {
        try { return fachada.buscarCategoria(clave); }
        catch (Exception e) { return null; }
    }

    private Rol buscarRolSilencioso(String nombreCargo) {
        for (Rol r : fachada.listarRoles()) {
            if (r.getNombreCargo().equalsIgnoreCase(nombreCargo)) return r;
        }
        return null;
    }

    private Producto buscarProductoSilencioso(String codigo) {
        try { return fachada.buscarProducto(codigo); }
        catch (Exception e) { return null; }
    }

    private Inventario buscarInventarioSilencioso(String idStr) {
        try {
            int id = Integer.parseInt(idStr);
            return fachada.buscarInventario(id);
        } catch (Exception e) { return null; }
    }

    private Proveedor buscarProveedorSilencioso(String idStr) {
        try {
            int id = Integer.parseInt(idStr);
            return fachada.buscarProveedor(id);
        } catch (Exception e) { return null; }
    }

    private Usuario buscarUsuarioSilencioso(String idStr) {
        try {
            int id = Integer.parseInt(idStr);
            return fachada.buscarUsuario(id);
        } catch (Exception e) { return null; }
    }

    public Usuario autenticarUsuario(java.awt.Component parent, String email, String contrasena) {
        try {
            return fachada.autenticarUsuario(email, contrasena);
        } catch (FachadaException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Error de acceso", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Filtra productos cuya fecha de vencimiento cae dentro del período. */
    private ArrayList<Producto> filtrarPorPeriodo(
            ArrayList<Producto> lista, Periodo periodo) {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (Producto p : lista) {
            Date fv = p.getFechaVencimiento();
            if (fv != null && periodo.contiene(Fecha.desdeDate(fv)))
                resultado.add(p);
        }
        return resultado;
    }
}