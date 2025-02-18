import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rodrigo
 * @date 18 febrero, 2025
 */
class ServerThread implements Runnable {
    private final Socket clientSocket;
    private final AtomicInteger clientCount;

    public ServerThread(Socket socket, AtomicInteger clientCount) {
        this.clientSocket = socket;
        this.clientCount = clientCount;
    }

    @Override
    public void run() {

        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {
            dos.writeUTF("Bienvenido al servidor");
            while (true) {
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
                clientSocket.close();
                clientCount.decrementAndGet();
                System.out.println("Un cliente se ha desconectado, actualmente hay: " + clientCount.get() + " clientes conectados.");
            } catch (IOException e) {
                System.err.println("Error al cerrar el socket: " + e.getMessage());
            }
        }
    }

    private void receiveFile(DataInputStream dis) throws IOException {

        String fileName = dis.readUTF();
        long fileSize = dis.readLong();
        File file = new File("server_files/" + fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs(); // Crear directorios si no existen
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while (fileSize > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                fos.write(buffer, 0, bytesRead);
                fileSize -= bytesRead;
            }
        }
        System.out.println("Archivo " + fileName + " recibido.");
    }

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

    private void sendFile(DataInputStream dis, DataOutputStream dos) throws IOException {
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
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("Archivo " + fileName + " enviado.");
    }
}
