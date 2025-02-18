import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase principal que crea múltiples clientes para probar el servidor.
 *
 * @author Rodrigo
 * @date 18 febrero, 2025
 */
public class Main {
    /**
     * Método principal que crea 10 clientes falsos para probar el servidor.
     *
     * @param args Argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        ExecutorService clientPool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            clientPool.execute(() -> {
                try {
                    Client.main(new String[]{});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Cerrar el pool de hilos
        clientPool.shutdown();
    }
}
