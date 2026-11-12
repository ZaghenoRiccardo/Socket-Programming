import java.io.*;
import java.net.*;

public class CalcolatriceServer {
    private static final int PORTA = 8844;

    public static void main(String[] args) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Server avviato sulla porta " + PORTA);
            // Accetta connessioni...
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    client.setSoTimeout(5000);
                    handleRequest(client); // Gestisci comunicazione con client
                } catch (IOException error) {
                    System.err.println("Errore richiesta: " + error.getMessage());
                }
            }
        }

    }

    private static void handleRequest(Socket client) throws IOException {
        InputStream inputStream = client.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(reader);
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);

        String riga = br.readLine();

        if (riga == null) {
            System.out.println("Input nullo, richiesta scaduta.");
        } else {
            // Controllo Formato
            String[] parti = riga.trim().split("\\s+");
            double num1 = Double.parseDouble(parti[0]);
            String operazione = parti[1];
            double num2 = Double.parseDouble(parti[2]);

            if (parti.length != 3) {
                out.println("ERRORE: Formato non valido");
            } else if (!Double.isFinite(num1) || !Double.isFinite(num2)) {
                out.println("ERRORE: Sono ammessi soltanto numeri finiti");
            } else {
                double risultato = calcola(num1, operazione, num2);
                out.println("RISULTATO: " + risultato);
            }

        }
    }

    private static double calcola(double num1, String operazione, double num2) {
        switch (operazione) {
            case "+":
                return num1 + num2;
            case "-":
                return num1 - num2;
            case "*":
                return num1 * num2;
            case "/":
                if (num2 == 0)
                    throw new ArithmeticException("Divisione per zero"); //TODO: sviluppo ArithmeticException
                return num1 / num2;
            default:
                throw new IllegalArgumentException("Operazione non supportata");
        }
    }

    // TO DO: Contatore operazioni eseguite
    // TO DO: Comando "QUIT" per chiusura
    // TO DO: Chiusura corretta delle risorse

}

