import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** Client della variante con una sola richiesta: invio e lettura sono a righe UTF-8. */
public class SimpleClient {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 5013);
             Scanner keyboard = new Scanner(System.in)) {
            socket.setSoTimeout(30_000);

            // Crea un BufferedReader e un PrintWriter per leggere e scrivere righe UTF-8.
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));

            // Chiede all'utente di scrivere un messaggio, lo invia al server e stampa la risposta.
            System.out.println("Scrivi un messaggio:");
            if (!keyboard.hasNextLine()) return; // Nessun input: esci.
            out.println(keyboard.nextLine());   // La newline rende completa la richiesta.
            String response = in.readLine();    // Legge la risposta del server.
            if (response != null) System.out.println("Risposta: " + response);
        }
    }
}

