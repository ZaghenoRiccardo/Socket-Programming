import java.io.*;
import java.net.*;

public class CalcolatriceServer {
    private static final int PORTA = 8844;
    private static int operazioniEseguite = 0;
    private static boolean attivo = true;

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Server avviato sulla porta " + PORTA);

            while (attivo) {
                try (Socket client = serverSocket.accept()) {
                    handleRequest(client);
                } catch (IOException error) {
                    System.err.println("Errore richiesta: " + error.getMessage());
                }
            }
        }
        System.out.println("Server fermato correttamente. Operazioni eseguite: " + operazioniEseguite);
    }

    private static void handleRequest(Socket client) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);

        String riga = br.readLine();

        if (riga == null) {
            System.out.println("Input nullo, richiesta scaduta.");
            return;
        } else{

            riga = riga.trim();

            if (riga.equals("quit")) {
                attivo = false;
                out.println("Server in chiusura");
                return;
            } else{

                String[] parti = riga.split("\\s+");
                if (parti.length != 3) {
                    out.println("ERRORE: Formato non valido");
                    return;
                }

                try {
                    double num1 = Double.parseDouble(parti[0]);
                    String operazione = parti[1];
                    double num2 = Double.parseDouble(parti[2]);

                    if (!Double.isFinite(num1) || !Double.isFinite(num2)) {
                        out.println("ERRORE: Sono ammessi soltanto numeri finiti");
                        return;
                    }

                    double risultato = calcola(num1, operazione, num2);
                    out.println("RISULTATO: " + risultato);

                } catch (NumberFormatException e) {
                    out.println("ERRORE: Numero non valido");
                } catch (ArithmeticException | IllegalArgumentException e) {
                    out.println("ERRORE: " + e.getMessage());
                }
            }
            
        }

    }

    private static double calcola(double num1, String operazione, double num2) {
        double risultato;
        switch (operazione) {
            case "+": risultato = num1 + num2; break;
            case "-": risultato = num1 - num2; break;
            case "*": risultato = num1 * num2; break;
            case "/":
                if (num2 == 0) throw new ArithmeticException("Divisione per zero");
                risultato = num1 / num2;
                break;
            default:
                throw new IllegalArgumentException("Operazione non supportata");
        }
        operazioniEseguite++;
        return risultato;
    }
}
