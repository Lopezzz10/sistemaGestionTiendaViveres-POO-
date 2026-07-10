package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.*;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;

public class FacturasArchivo {

    private final Path archivo;

    public FacturasArchivo() {
        this.archivo = Paths.get("facturas.dat");
    }

    public void guardar(ArrayList<Factura> facturas) throws Exception {
        try (DataOutputStream salida = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(archivo)))) {

            salida.writeInt(facturas.size());

            for (Factura f : facturas) {
                salida.writeInt(f.getIdFactura());
                salida.writeUTF(f.getNumeroFactura());
                salida.writeLong(f.getFechaEmision().getTime());
                salida.writeUTF(f.getEstado().name());
                salida.writeDouble(f.getSubtotal());
                salida.writeDouble(f.getMontoIva());
                salida.writeDouble(f.getTotal());

                // Cliente
                Cliente cli = f.getCliente();
                salida.writeBoolean(cli != null);
                if (cli != null) {
                    salida.writeInt(cli.getIdUsuario());
                    salida.writeUTF(cli.getNombre());
                    salida.writeUTF(cli.getEmail());
                    salida.writeUTF(cli.getDireccion());
                    salida.writeUTF(cli.getTelefono());
                }

                // Cajero
                Cajero caj = f.getCajero();
                salida.writeBoolean(caj != null);
                if (caj != null) {
                    salida.writeInt(caj.getIdUsuario());
                    salida.writeUTF(caj.getNombre());
                    salida.writeUTF(caj.getEmail());
                }

                // Detalles — ahora también se guarda el NOMBRE del producto
                ArrayList<edu.uce.programacion2.tienda.negocio.DetalleFactura> detalles = f.getDetalles();
                salida.writeInt(detalles.size());
                for (edu.uce.programacion2.tienda.negocio.DetalleFactura d : detalles) {
                    salida.writeUTF(d.getProducto() != null ? d.getProducto().getCodigo() : "");
                    salida.writeUTF(d.getProducto() != null ? d.getProducto().getNombre() : ""); // ← NUEVO
                    salida.writeInt(d.getCantidad());
                    salida.writeDouble(d.getPrecioUnitarioSinIva());
                    salida.writeDouble(d.getDescuento());
                }
            }
        }
    }

    public ArrayList<Factura> cargar(ArrayList<Producto> catalogo) throws Exception {
        ArrayList<Factura> lista = new ArrayList<>();
        if (!Files.exists(archivo)) return lista;

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {

            int cantidad = entrada.readInt();

            for (int i = 0; i < cantidad; i++) {
                int    id     = entrada.readInt();
                String numero = entrada.readUTF();
                Date   fecha  = new Date(entrada.readLong());
                Factura.EstadoFactura estado = Factura.EstadoFactura.valueOf(entrada.readUTF());
                double subtotal  = entrada.readDouble();
                double montoIva  = entrada.readDouble();
                double total     = entrada.readDouble();

                // Cliente
                Cliente cli = null;
                if (entrada.readBoolean()) {
                    cli = new Cliente();
                    cli.setIdUsuario(entrada.readInt());
                    cli.setNombre(entrada.readUTF());
                    cli.setEmail(entrada.readUTF());
                    cli.setDireccion(entrada.readUTF());
                    cli.setTelefono(entrada.readUTF());
                }

                // Cajero
                Cajero caj = null;
                if (entrada.readBoolean()) {
                    caj = new Cajero();
                    caj.setIdUsuario(entrada.readInt());
                    caj.setNombre(entrada.readUTF());
                    caj.setEmail(entrada.readUTF());
                }

                // Detalles
                int cantDet = entrada.readInt();
                ArrayList<edu.uce.programacion2.tienda.negocio.DetalleFactura> detalles = new ArrayList<>();
                for (int j = 0; j < cantDet; j++) {
                    String cod    = entrada.readUTF();
                    String nombre = entrada.readUTF(); // ← NUEVO

                    int    cant = entrada.readInt();
                    double psi  = entrada.readDouble();
                    double desc = entrada.readDouble();

                    // Buscar producto en catálogo; si no existe, crear uno temporal con los datos guardados
                    Producto prod = buscarEnCatalogo(catalogo, cod);
                    if (prod == null) {
                        prod = new Producto();
                        prod.setCodigo(cod);
                        prod.setNombre(nombre.isEmpty() ? "Producto " + cod : nombre);
                    }

                    detalles.add(new edu.uce.programacion2.tienda.negocio.DetalleFactura(j + 1, prod, cant, psi, desc));
                }

                // Reconstruir la factura restaurando TODOS los campos
                Factura f = new Factura();
                f.setIdFactura(id);
                f.setNumeroFactura(numero);
                f.setFechaEmision(fecha);
                f.setEstado(estado);
                f.setCliente(cli);
                f.setCajero(caj);
                f.setSubtotal(subtotal);   // ← NUEVO
                f.setMontoIva(montoIva);   // ← NUEVO
                f.setTotal(total);         // ← NUEVO
                f.getDetalles().addAll(detalles);

                lista.add(f);
            }

        } catch (EOFException e) {
            System.err.println("[FacturasArchivo] Archivo incompleto. Se devuelven las facturas leídas.");
        }
        return lista;
    }

    private Producto buscarEnCatalogo(ArrayList<Producto> catalogo, String codigo) {
        for (Producto p : catalogo) {
            if (p.getCodigo().equals(codigo)) return p;
        }
        return null;
    }
}