/**
 * @author Rodrigo
 * @date 18 febrero, 2025
 */

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 10;
    private static final AtomicInteger clientCount = new AtomicInteger(0);

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado y esperando conexiones...");


            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (clientCount.get() < MAX_CLIENTS) {
                    clientCount.incrementAndGet();
                    System.out.println("Se ha conectado un nuevo cliente, actualmente hay: " + clientCount.get() + " clientes conectados.");
                    pool.execute(new ServerThread(clientSocket, clientCount));
                } else {
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