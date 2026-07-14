package edu.uce.programacion2.tienda.control;

import edu.uce.programacion2.tienda.negocio.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Convierte colecciones de objetos de negocio en {@link Tabla} para
 * su presentación en PanelTabla.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Conversiones {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    // ── Categorías ────────────────────────────────────────────────────────

    public Tabla categoriasATabla(ArrayList<Categoria> lista) {
        String[]   cols  = { "Clave", "Nombre", "Tipo" };
        Object[][] filas = new Object[lista.size()][3];
        for (int i = 0; i < lista.size(); i++) {
            Categoria c = lista.get(i);
            filas[i][0] = c.getCveCategoria();
            filas[i][1] = c.getNombre();
            filas[i][2] = c.getTipo();
        }
        return new Tabla("Categorías", cols, filas);
    }

    // ── Roles ────────────────────────────────────────────────────────────

    public Tabla rolesATabla(ArrayList<Rol> lista) {
        String[]   cols  = { "Id", "Cargo", "Permisos", "Activo" };
        Object[][] filas = new Object[lista.size()][4];
        for (int i = 0; i < lista.size(); i++) {
            Rol r = lista.get(i);
            filas[i][0] = r.getIdRol();
            filas[i][1] = r.getNombreCargo();
            filas[i][2] = r.getPermisosTexto();
            filas[i][3] = r.isActivo() ? "Si" : "No";
        }
        return new Tabla("Roles", cols, filas);
    }

    // ── Catálogo General ──────────────────────────────────────────────────

    public Tabla catalogoATabla(ArrayList<Producto> lista) {
        String[]   cols  = { "Código", "Nombre", "Categoría", "Tipo", "Precio", "Precio Final" };
        Object[][] filas = new Object[lista.size()][6];
        for (int i = 0; i < lista.size(); i++) {
            Producto p = lista.get(i);
            filas[i][0] = p.getCodigo();
            filas[i][1] = p.getNombre();
            filas[i][2] = p.getCategoria() != null ? p.getCategoria().getNombre() : "-";
            filas[i][3] = p.getTipo();
            filas[i][4] = String.format("$%.2f", p.getPrecioUnitario());
            filas[i][5] = String.format("$%.2f", p.calcularPrecioFinal());
        }
        return new Tabla("Catálogo Completo", cols, filas);
    }

    // ── Inventarios ───────────────────────────────────────────────────────

    public Tabla inventariosATabla(ArrayList<Inventario> lista) {
        String[]   cols  = { "ID", "Producto", "Stock Disponible", "Umbral Alerta", "Alerta" };
        Object[][] filas = new Object[lista.size()][5];
        for (int i = 0; i < lista.size(); i++) {
            Inventario inv = lista.get(i);
            filas[i][0] = inv.getIdInventario();
            filas[i][1] = inv.getProducto() != null ? inv.getProducto().getNombre() : "-";
            filas[i][2] = inv.getCantidadDisponible();
            filas[i][3] = Inventario.getUmbralAlerta();
            filas[i][4] = inv.requiereAlerta() ? "⚠ BAJO" : "Normal";
        }
        return new Tabla("Inventarios", cols, filas);
    }

    // ── Proveedores ───────────────────────────────────────────────────────

    public Tabla proveedoresATabla(ArrayList<Proveedor> lista) {
        String[]   cols  = { "ID", "Nombre", "RUC", "Teléfono", "Email", "Dirección" };
        Object[][] filas = new Object[lista.size()][6];
        for (int i = 0; i < lista.size(); i++) {
            Proveedor p = lista.get(i);
            filas[i][0] = p.getIdProveedor();
            filas[i][1] = p.getNombre();
            filas[i][2] = p.getRuc();
            filas[i][3] = p.getTelefono();
            filas[i][4] = p.getEmail();
            filas[i][5] = p.getDireccion();
        }
        return new Tabla("Proveedores", cols, filas);
    }

    // ── Ventas ────────────────────────────────────────────────────────────

    public Tabla ventasATabla(ArrayList<Venta> lista) {
        String[]   cols  = { "ID Venta", "Método Pago", "Fecha", "Total", "Estado" };
        Object[][] filas = new Object[lista.size()][5];
        for (int i = 0; i < lista.size(); i++) {
            Venta v = lista.get(i);
            filas[i][0] = v.getIdVenta();
            filas[i][1] = v.getMetodoPago();
            filas[i][2] = SDF.format(v.getFecha());
            filas[i][3] = String.format("$%.2f", v.getTotal());
            filas[i][4] = v.getEstado().toString();
        }
        return new Tabla("Ventas", cols, filas);
    }

    // ── Facturas ──────────────────────────────────────────────────────────

    public Tabla facturasATabla(ArrayList<Factura> lista) {
        String[] cols = { "Número", "ID Venta", "Fecha", "Producto", "Cantidad",
                "P. S/IVA", "IVA Línea", "Subtotal Línea", "Total Factura", "Estado" };

        int totalFilas = 0;
        for (Factura f : lista) {
            if (!f.getDetalles().isEmpty()) {
                totalFilas += f.getDetalles().size();
            } else if (f.getVenta() != null && !f.getVenta().getDetalles().isEmpty()) {
                totalFilas += f.getVenta().getDetalles().size();
            } else {
                totalFilas += 1;
            }
        }

        Object[][] filas = new Object[totalFilas][10];
        int fila = 0;

        for (Factura f : lista) {
            String nro     = f.getNumeroFactura();
            String idVenta = f.getVenta() != null ? String.valueOf(f.getVenta().getIdVenta()) : "-";
            String fecha   = SDF.format(f.getFechaEmision());
            String total   = String.format("$%.2f", f.getTotal());
            String estado  = f.getEstado().toString();

            if (!f.getDetalles().isEmpty()) {
                for (DetalleFactura d : f.getDetalles()) {
                    String producto = d.getProducto() != null ? d.getProducto().getNombre() : "-";
                    filas[fila][0] = nro;
                    filas[fila][1] = idVenta;
                    filas[fila][2] = fecha;
                    filas[fila][3] = producto;
                    filas[fila][4] = d.getCantidad();
                    filas[fila][5] = String.format("$%.2f", d.getPrecioUnitarioSinIva());
                    filas[fila][6] = String.format("$%.2f", d.getMontoIva());
                    filas[fila][7] = String.format("$%.2f", d.getSubtotal());
                    filas[fila][8] = total;
                    filas[fila][9] = estado;
                    fila++;
                }
            } else if (f.getVenta() != null && !f.getVenta().getDetalles().isEmpty()) {
                for (DetalleVenta d : f.getVenta().getDetalles()) {
                    String producto = d.getProducto() != null ? d.getProducto().getNombre() : "-";
                    filas[fila][0] = nro;
                    filas[fila][1] = idVenta;
                    filas[fila][2] = fecha;
                    filas[fila][3] = producto;
                    filas[fila][4] = d.getCantidad();
                    filas[fila][5] = String.format("$%.2f", d.getPrecioUnitario());
                    filas[fila][6] = "-";
                    filas[fila][7] = String.format("$%.2f", d.calcularSubtotal());
                    filas[fila][8] = total;
                    filas[fila][9] = estado;
                    fila++;
                }
            } else {
                filas[fila][0] = nro;
                filas[fila][1] = idVenta;
                filas[fila][2] = fecha;
                filas[fila][3] = "(sin productos)";
                filas[fila][4] = 0;
                filas[fila][5] = "$0.00";
                filas[fila][6] = "$0.00";
                filas[fila][7] = "$0.00";
                filas[fila][8] = total;
                filas[fila][9] = estado;
                fila++;
            }
        }
        return new Tabla("Facturas", cols, filas);
    }

    // ── Compras ───────────────────────────────────────────────────────────

    public Tabla comprasATabla(ArrayList<Compra> lista) {
        String[]   cols  = { "ID Compra", "Proveedor", "Fecha", "Total", "Estado" };
        Object[][] filas = new Object[lista.size()][5];
        for (int i = 0; i < lista.size(); i++) {
            Compra c = lista.get(i);
            filas[i][0] = c.getIdCompra();
            filas[i][1] = c.getProveedor() != null ? c.getProveedor().getNombre() : "-";
            filas[i][2] = SDF.format(c.getFecha());
            filas[i][3] = String.format("$%.2f", c.getTotal());
            filas[i][4] = c.getEstado().toString();
        }
        return new Tabla("Compras", cols, filas);
    }

    // ── Usuarios ──────────────────────────────────────────────────────────

    public Tabla usuariosATabla(ArrayList<Usuario> lista) {
        String[]   cols  = { "ID", "Nombre", "Email", "Permiso", "Tipo" };
        Object[][] filas = new Object[lista.size()][5];
        for (int i = 0; i < lista.size(); i++) {
            Usuario u  = lista.get(i);
            String  tipo;
            if (u instanceof Administrador)      tipo = "Administrador";
            else if (u instanceof Cliente)        tipo = "Cliente";
            else                                   tipo = "Cajero";
            filas[i][0] = u.getIdUsuario();
            filas[i][1] = u.getNombre();
            filas[i][2] = u.getEmail();
            filas[i][3] = u.getPermiso();
            filas[i][4] = tipo;
        }
        return new Tabla("Usuarios", cols, filas);
    }
}