package main;

import dao.CodigoBarrasDaoImpl;
import dao.ProductoDaoImpl;
import entities.CodigoBarras;
import entities.Producto;
import entities.TipoCodigo;
import service.ProductoService;

import java.time.LocalDate;

public class AppTest {
    public static void main(String[] args) {
        try {
            // --- PRUEBA DE CODIGO DE BARRAS ---
            System.out.println("--- PRUEBA DE CODIGO DE BARRAS ---");
            CodigoBarrasDaoImpl codigoDao = new CodigoBarrasDaoImpl();

            String valorUnico = "CB" + System.currentTimeMillis();
            CodigoBarras cb = new CodigoBarras(null, false, TipoCodigo.EAN13, valorUnico,
                    LocalDate.now(), "Producto de prueba", null);

            codigoDao.crear(cb);
            System.out.println("Insertado con ID: " + cb.getId());
            System.out.println("Leído: " + codigoDao.leer(cb.getId()));

            System.out.println("Listado de códigos:");
            for (CodigoBarras item : codigoDao.leerTodos()) {
                System.out.println(item);
            }

            cb.setObservaciones("Producto actualizado");
            codigoDao.actualizar(cb);
            System.out.println("Actualizado: " + codigoDao.leer(cb.getId()));

            codigoDao.eliminar(cb.getId());
            System.out.println("Eliminado (baja lógica): " + cb.getId());

            // --- PRUEBA DE PRODUCTO ---
            System.out.println("\n--- PRUEBA DE PRODUCTO ---");
            ProductoDaoImpl productoDao = new ProductoDaoImpl();

            Producto p = new Producto(null, false, "Yerba Mate", "Taragüi", "Alimentos", 4600.0, 1.0, null);
            productoDao.crear(p);
            System.out.println("Producto insertado con ID: " + p.getId());
            System.out.println("Producto leído: " + productoDao.leer(p.getId()));

            System.out.println("Listado de productos:");
            for (Producto item : productoDao.leerTodos()) {
                System.out.println(item);
            }

            p.setMarca("Rosamonte");
            productoDao.actualizar(p);
            System.out.println("Producto actualizado: " + productoDao.leer(p.getId()));

            productoDao.eliminar(p.getId());
            System.out.println("Producto eliminado (baja lógica): " + p.getId());

            // --- PRUEBA DE SERVICE CON TRANSACCIONES ---
            System.out.println("\n--- PRUEBA DE SERVICE CON TRANSACCIONES ---");
            ProductoService service = new ProductoService();

            Producto productoTx = new Producto(null, false, "Yerba Mate", "Taragüi", "Alimentos", 4600.0, 1.0, null);
            CodigoBarras codigoTx = new CodigoBarras(null, false, TipoCodigo.EAN13, "CB" + System.currentTimeMillis(),
                    LocalDate.now(), "Código asignado", null);

            // Crear ambos en una transacción
            service.crearProductoConCodigo(productoTx, codigoTx);

            // Actualizar ambos en una transacción
            productoTx.setMarca("Rosamonte");
            codigoTx.setObservaciones("Código actualizado");
            service.actualizarProductoConCodigo(productoTx, codigoTx);

            // Eliminar ambos en una transacción
            service.eliminarProductoConCodigo(productoTx.getId(), codigoTx.getId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}