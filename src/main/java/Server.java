import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase principal del servidor que acepta conexiones de clientes.
 * Utiliza un pool de hilos para manejar múltiples clientes.
 *
 * @author Rodrigo
 * @date 18 febrero, 2025
 */
public class Server {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 10;
    private static final AtomicInteger clientCount = new AtomicInteger(0);

    /**
     * Método principal que inicia el servidor.
     *
     * @param args Argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado y esperando conexiones...");


            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (clientCount.get() < MAX_CLIENTS) {
                    //Incrementa el contador de clientes y maneja la conexión en un nuevo hilo
                    clientCount.incrementAndGet();
                    System.out.println("Se ha conectado un nuevo cliente, actualmente hay: " + clientCount.get() + " clientes conectados.");
                    pool.execute(new ServerThread(clientSocket, clientCount));
                } else {
                    //informa al cliente que está intentando conectarse de que el servidor está lleno y cierra su conexión
                    try (DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {
                        dos.writeUTF("SERVER_FULL");
                    }
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}