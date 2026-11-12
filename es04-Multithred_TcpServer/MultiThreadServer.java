import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/** Listener sulla porta 5013: accetta un socket distinto e lo trasferisce a un Runnable. */
public class MultiThreadServer {
    public static void main(String[] args) throws IOException {
        try (ServerSocket listener = new ServerSocket(5013)) {
            System.out.println("Server in ascolto sulla porta 5013");
            while (true) {
                Socket socket = listener.accept(); // Solo questa attesa riguarda il listener.
                // Crea un thread per gestire il client e lo avvia immediatamente.
                new Thread(new ClientTask(socket)).start();
            }
        }
    }
}
