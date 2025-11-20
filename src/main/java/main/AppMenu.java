package main;

import entities.CodigoBarras;
import entities.Producto;
import entities.TipoCodigo;
import service.ProductoService;
import service.CodigoBarrasService; 

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.util.InputMismatchException;

// Importaciones para solucionar la codificación
import java.io.PrintStream; 
import java.nio.charset.StandardCharsets; 

public class AppMenu {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ProductoService productoService = new ProductoService();
    private static final CodigoBarrasService codigoService = new CodigoBarrasService();

    public static void main(String[] args) {
        // 🚨 SOLUCIÓN PARA SYSTEM.OUT (Mensajes normales del menú)
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Error al configurar System.out: " + e.getMessage());
        }

        // 🚨 SOLUCIÓN PARA SYSTEM.ERR (Mensajes de error y trazas)
        try {
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            // En este punto, no podemos hacer mucho si falla System.err
        }

        menuPrincipal();
    }

    // ===============================================
    //               MENU PRINCIPAL
    // ===============================================
    private static void menuPrincipal() {
        int opcion = -1;
        do {
            System.out.println("\n--- TPI Productos y Códigos ---");
            System.out.println("1. Crear Nuevo Producto y Código (Transacción)");
            System.out.println("2. Actualizar Producto y Código (Transacción)");
            System.out.println("3. Eliminar Producto y Código (Baja Lógica Transaccional)");
            System.out.println("4. Búsquedas");
            System.out.println("5. Listar Todos los Productos Activos");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");
            
            try {
                opcion = Integer.parseInt(scanner.nextLine()); 
                ejecutarOpcion(opcion);
            } catch (NumberFormatException e) {
                System.err.println("❌ ERROR: Ingrese un número válido.");
            }
        } while (opcion != 0);
        System.out.println("Aplicación terminada. ¡Hasta pronto! 👋");
    }

    // ===============================================
    //             EJECUTAR OPCIONES
    // ===============================================
    private static void ejecutarOpcion(int opcion) {
        try {
            switch (opcion) {
                case 1:
                    crearProductoYCodigo();
                    break;
                case 2:
                    actualizarProductoYCodigo();
                    break;
                case 3:
                    eliminarProductoYCodigo();
                    break;
                case 4:
                    menuBusquedas();
                    break;
                case 5:
                    listarProductos();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } catch (SQLException e) {
            // Manejo de errores de la capa de Servicio/BD (Rollbacks y Validaciones)
            System.err.println("❌ ERROR DE TRANSACCIÓN/VALIDACIÓN: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Detalle: " + e.getCause().getMessage());
            }
        } catch (InputMismatchException | NumberFormatException e) {
             System.err.println("❌ ERROR DE ENTRADA: Debe ingresar un valor numérico para ID, Precio o Peso.");
        } catch (Exception e) {
            System.err.println("❌ ERROR INESPERADO: " + e.getMessage());
        }
    }
    
    // ===============================================
    //       FLUJOS TRANSACCIONALES (Servicio)
    // ===============================================

    private static void crearProductoYCodigo() throws SQLException {
        System.out.println("\n--- CREAR PRODUCTO Y CÓDIGO ---");
        
        // 1. Obtener datos del Producto
        Producto p = new Producto();
        p.setEliminado(false);
        System.out.print("Nombre del Producto: ");
        p.setNombre(scanner.nextLine());
        System.out.print("Marca: ");
        p.setMarca(scanner.nextLine());
        System.out.print("Categoría: ");
        p.setCategoria(scanner.nextLine());
        System.out.print("Precio: ");
        p.setPrecio(Double.parseDouble(scanner.nextLine()));
        
        System.out.print("Peso (kg - dejar vacío para omitir): ");
        String pesoStr = scanner.nextLine();
        if (!pesoStr.trim().isEmpty()) {
            p.setPeso(Double.parseDouble(pesoStr));
        }

        // 2. Obtener datos del Código de Barras
        CodigoBarras cb = new CodigoBarras();
        cb.setEliminado(false);
        cb.setTipo(TipoCodigo.EAN13); // Simplificado
        System.out.print("Valor del Código de Barras: ");
        cb.setValor(scanner.nextLine());
        cb.setFechaAsignacion(LocalDate.now());
        cb.setObservaciones("Creado por AppMenu");
        cb.setProductoId(null); 

        // 3. Llamada Transaccional al Servicio
        productoService.crearProductoConCodigo(p, cb);
        System.out.println("✅ Transacción Completa. Producto ID: " + p.getId() + ", Código ID: " + cb.getId());
    }

    private static void actualizarProductoYCodigo() throws SQLException {
        System.out.println("\n--- ACTUALIZAR PRODUCTO Y CÓDIGO ---");
        System.out.print("Ingrese ID del Producto a actualizar: ");
        Long productoId = Long.parseLong(scanner.nextLine());

        // 1. Cargar el Producto y su Código asociado
        Producto p = productoService.leer(productoId); 
        if (p == null) {
            System.err.println("❌ Producto ID " + productoId + " no encontrado.");
            return;
        }

        // Asumiendo relación 1:1, buscamos el código 
        List<CodigoBarras> codigos = codigoService.buscarPorProductoId(productoId);
        if (codigos.isEmpty()) {
             System.err.println("❌ El producto no tiene código de barras asociado.");
             return;
        }
        CodigoBarras cb = codigos.get(0);

        // 2. Modificar datos
        System.out.println("Nombre actual: " + p.getNombre() + ". Nuevo Nombre (dejar vacío para no cambiar): ");
        String nuevoNombre = scanner.nextLine();
        if (!nuevoNombre.isEmpty()) p.setNombre(nuevoNombre);
        
        System.out.println("Precio actual: " + p.getPrecio() + ". Nuevo Precio: ");
        p.setPrecio(Double.parseDouble(scanner.nextLine()));
        
        System.out.println("Observaciones actuales del Código: " + cb.getObservaciones());
        System.out.print("Nuevas Observaciones: ");
        cb.setObservaciones(scanner.nextLine());

        // 3. Llamada Transaccional al Servicio
        productoService.actualizarProductoConCodigo(p, cb);
        System.out.println("✅ Producto y Código ID " + cb.getId() + " actualizados en una transacción.");
    }

    private static void eliminarProductoYCodigo() throws SQLException {
        System.out.println("\n--- ELIMINAR (BAJA LÓGICA) PRODUCTO Y CÓDIGO ---");
        System.out.print("Ingrese ID del Producto a aplicar Baja Lógica: ");
        Long productoId = Long.parseLong(scanner.nextLine());
        
        // 1. Buscamos el código asociado para obtener su ID
        List<CodigoBarras> codigos = codigoService.buscarPorProductoId(productoId);

        if (codigos.isEmpty()) {
            System.err.println("❌ Producto ID " + productoId + " no encontrado o no tiene código asociado. No se puede eliminar transaccionalmente.");
            return;
        }
        Long codigoId = codigos.get(0).getId();

        // 2. Llamada Transaccional al Servicio
        productoService.eliminarProductoConCodigo(productoId, codigoId);
        System.out.println("✅ Baja lógica aplicada transaccionalmente al Producto ID " + productoId + " y Código ID " + codigoId);
    }

    // ===============================================
    //               BUSQUEDAS Y LISTADOS
    // ===============================================

    private static void listarProductos() throws SQLException {
        System.out.println("\n--- LISTADO DE PRODUCTOS ACTIVOS ---");
        List<Producto> productos = productoService.getAll(); 

        if (productos.isEmpty()) {
            System.out.println("No hay productos activos para mostrar.");
            return;
        }

        for (Producto p : productos) {
            // Se usa el CodigoBarrasService para completar la información
            String codigoInfo = "N/A";
            List<CodigoBarras> codigos = codigoService.buscarPorProductoId(p.getId());
            if (!codigos.isEmpty()) {
                codigoInfo = codigos.get(0).getValor() + " (" + codigos.get(0).getTipo().name() + ")";
            }
            
            System.out.printf("ID: %d | Nombre: %s | Marca: %s | Precio: %.2f | Código: %s%n", 
                              p.getId(), p.getNombre(), p.getMarca(), p.getPrecio(), codigoInfo);
        }
    }

    private static void menuBusquedas() throws SQLException {
        System.out.println("\n--- BÚSQUEDAS ---");
        System.out.println("1. Buscar Código de Barras por Valor");
        System.out.print("Seleccione una opción: ");
        
        int opcion = Integer.parseInt(scanner.nextLine());

        if (opcion == 1) {
            System.out.print("Ingrese valor exacto del código: ");
            String valor = scanner.nextLine();
            CodigoBarras cb = codigoService.buscarPorValor(valor); 

            if (cb != null) {
                System.out.println("✅ Código encontrado:");
                System.out.println(cb);
            } else {
                System.out.println("❌ Código no encontrado.");
            }
        }
    }
}