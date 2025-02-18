/**
 * @author Rodrigo
 * @date 18 febrero, 2025
 */
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {

        //Este main se encarga de crear 10 falsos clientes para comprobar que funciona correctamente el manejo de límites del servidor
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
