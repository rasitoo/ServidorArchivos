import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase que maneja la comunicación con un cliente en un hilo separado.
 * Implementa la interfaz Runnable.
 *
 * @author Rodrigo
 * @date 18 febrero, 2025
 */
class ServerThread implements Runnable {
    private final Socket clientSocket;
    private final AtomicInteger clientCount;

    /**
     * Constructor de la clase ServerThread.
     *
     * @param socket El socket del cliente.
     * @param clientCount El contador atómico de clientes conectados, al ser atómico permite estar conectado con el valor hilo del servidor sin peligros de concurrencia.
     */
    public ServerThread(Socket socket, AtomicInteger clientCount) {
        this.clientSocket = socket;
        this.clientCount = clientCount;
    }

    /**
     * Método que se ejecuta cuando el hilo comienza.
     * Maneja la comunicación con el cliente.
     */
    @Override
    public void run() {

        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {
            //Envia un mensaje de bienvenida al hiilo cliente, el cliente al iniciarse espera un mensaje del servidor, por lotanto sin esto el cliente no funcionaria
            dos.writeUTF("Bienvenido al servidor");
            while (true) {
                //Espera a que el cliente de una instruccion
                String command = dis.readUTF();
                if (command.equals("UPLOAD")) {
                    receiveFile(dis);
                } else if (command.equals("DOWNLOAD")) {
                    listFiles(dos);
                    sendFile(dis, dos);
                } else {
                    System.out.println("Se ha recibido un comando desconocido");
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("Error: Archivo no encontrado - " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error de IO: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error general: " + e.getMessage());
        } finally {
            try {
                //Cierra el socket y se quita del contador
                clientSocket.close();
                clientCount.decrementAndGet();
                System.out.println("Un cliente se ha desconectado, actualmente hay: " + clientCount.get() + " clientes conectados.");
            } catch (IOException e) {
                System.err.println("Error al cerrar el socket: " + e.getMessage());
            }
        }
    }

    /**
     * Método que recibe un archivo del cliente.
     *
     * @param dis El DataInputStream para leer datos del cliente.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void receiveFile(DataInputStream dis) throws IOException {
        //Lee nombre y tamaño del archivo
        String fileName = dis.readUTF();
        long fileSize = dis.readLong();
        File file = new File("server_files/" + fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs(); //Crea directorios si no existen
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            //Lee y escribe el archivo en el servidor
            while (fileSize > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                fos.write(buffer, 0, bytesRead);
                fileSize -= bytesRead;
            }
        }
        System.out.println("Archivo " + fileName + " recibido.");
    }

    /**
     * Método que lista los archivos disponibles en el servidor.
     *
     * @param dos El DataOutputStream para enviar datos al cliente.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void listFiles(DataOutputStream dos) throws IOException {
        File dir = new File("./server_files");
        File[] files = dir.listFiles();
        if (files != null) {
            dos.writeInt(files.length);
            for (File file : files) {
                dos.writeUTF(file.getName());
            }
        } else {
            dos.writeInt(0);
        }
    }

    /**
     * Método que envía un archivo al cliente.
     *
     * @param dis El DataInputStream para leer datos del cliente.
     * @param dos El DataOutputStream para enviar datos al cliente.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void sendFile(DataInputStream dis, DataOutputStream dos) throws IOException {
        //Lee el nombre del archivo que quiere el usuario
        String fileName = dis.readUTF();
        File file = new File("./server_files/" + fileName);
        if (!file.exists()) {
            dos.writeUTF("FILE_NOT_FOUND");
            return;
        }
        dos.writeUTF("FILE_FOUND");
        dos.writeLong(file.length());
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            //Se lo envia por bytes el archivo al cliente
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("Archivo " + fileName + " enviado.");
    }
}
