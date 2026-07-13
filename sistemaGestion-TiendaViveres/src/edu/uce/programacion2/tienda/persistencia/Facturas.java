package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Factura;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.CriteriosVenta;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Capa de persistencia para {@link Factura}.
 * Almacena en memoria todas las facturas emitidas.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Facturas {

    private ArrayList<Factura> facturas;

    public Facturas() {
        this.facturas = new ArrayList<>();
    }

    public void agregar(Factura f) throws PersistenciaException {
        if (f == null || f.getNumeroFactura().isEmpty())
            throw new PersistenciaException("Factura inválida.");
        for (Factura existente : facturas)
            if (existente.getNumeroFactura().equalsIgnoreCase(f.getNumeroFactura()))
                throw new PersistenciaException("Factura ya existe: " + f.getNumeroFactura());
        facturas.add(f);
    }

    public Factura buscar(String numeroFactura) throws PersistenciaException {
        for (Factura f : facturas)
            if (f.getNumeroFactura().equalsIgnoreCase(numeroFactura)) return f;
        throw new PersistenciaException("Factura no encontrada: " + numeroFactura);
    }

    public Factura buscarPorId(int idFactura) throws PersistenciaException {
        for (Factura f : facturas)
            if (f.getIdFactura() == idFactura) return f;
        throw new PersistenciaException("Factura no encontrada: id=" + idFactura);
    }

    public void actualizar(Factura f) throws PersistenciaException {
        for (int i = 0; i < facturas.size(); i++) {
            if (facturas.get(i).getNumeroFactura()
                    .equalsIgnoreCase(f.getNumeroFactura())) {
                facturas.set(i, f);
                return;
            }
        }
        throw new PersistenciaException("Factura no encontrada para actualizar.");
    }

    public void eliminar(String numeroFactura) throws PersistenciaException {
        facturas.remove(buscar(numeroFactura));
    }

    /** Retorna solo las facturas con estado EMITIDA. */
    public ArrayList<Factura> listarEmitidas() {
        ArrayList<Factura> resultado = new ArrayList<>();
        for (Factura f : facturas)
            if (f.getEstado() == Factura.EstadoFactura.EMITIDA) resultado.add(f);
        return resultado;
    }

    /** Retorna solo las facturas con estado ANULADA. */
    public ArrayList<Factura> listarAnuladas() {
        ArrayList<Factura> resultado = new ArrayList<>();
        for (Factura f : facturas)
            if (f.getEstado() == Factura.EstadoFactura.ANULADA) resultado.add(f);
        return resultado;
    }

    public ArrayList<Factura> listar() { return facturas; }

    public int conteo() { return facturas.size(); }

    /**
     * Búsqueda genérica por cualquier condición expresada como función
     * (programación funcional): recibe un {@link Predicate}&lt;{@link Factura}&gt;
     * y retorna las facturas que lo cumplen. Es el mecanismo base sobre el
     * que se construyen las consultas combinables de {@link FiltrosVenta}
     * y {@link CriteriosVenta}.
     *
     * @param criterio función que decide si una factura pertenece al resultado
     * @return facturas que cumplen el criterio (lista nueva, no afecta la original)
     */
    public ArrayList<Factura> buscarPor(Predicate<Factura> criterio) {
        return facturas.stream()
                .filter(criterio)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}