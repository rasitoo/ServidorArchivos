/**
 * @author Rodrigo
 * @date 18 febrero, 2025
 */

import java.io.*;
import java.net.Socket;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado al servidor");

            while (true) {
                System.out.print("Ingrese comando (UPLOAD/DOWNLOAD/EXIT): ");
                String command = console.readLine().trim().toUpperCase();

                if (command.equals("EXIT")) {
                    break;
                }

                dos.writeUTF(command);

                if (command.equals("UPLOAD")) {
                    System.out.print("Ingrese la ruta del archivo a subir: ");
                    String filePath = console.readLine();
                    String fileName = new File(filePath).getName();
                    dos.writeUTF(fileName);
                    sendFile(filePath, dos);
                } else if (command.equals("DOWNLOAD")) {
                    listFiles(dis);
                    System.out.print("Ingrese el nombre del archivo a descargar: ");
                    String fileName = console.readLine();
                    dos.writeUTF(fileName);
                    receiveFile(fileName, dis);
                } else {
                    System.out.println("Comando desconocido");
                }
            }
        } catch (IOException e) {
            System.err.println("Error en el cliente: " + e.getMessage());
        }
    }

    private static void listFiles(DataInputStream dis) throws IOException {
        int fileCount = dis.readInt();
        System.out.println("Archivos disponibles en el servidor:");
        for (int i = 0; i < fileCount; i++) {
            System.out.println(dis.readUTF());
        }
    }

    private static void sendFile(String fileName, DataOutputStream dos) throws IOException {
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("Archivo no encontrado");
            return;
        }
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

    private static void receiveFile(String fileName, DataInputStream dis) throws IOException {
        String status = dis.readUTF();
        if (status.equals("FILE_NOT_FOUND")) {
            System.out.println("Archivo no encontrado en el servidor");
            return;
        }
        long fileSize = dis.readLong();
        File dir = new File(".\\client_files\\" + fileName);
        try (FileOutputStream fos = new FileOutputStream(dir)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while (fileSize > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                fos.write(buffer, 0, bytesRead);
                fileSize -= bytesRead;
            }
        }
        System.out.println("Archivo " + fileName + " descargado.");
    }
}
