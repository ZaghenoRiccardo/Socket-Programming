/** Una sola lettura per stream; le risposte multiriga vanno consumate fino a END. */
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientPrenotazione {
    private static final String HOST = "localhost";
    private static final int PORTA = 8888;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner scanner;
    private String username;
    
    public static void main(String[] args) {
        new PrenotazioneClient().start();
    }
    
    public void start() {
        System.out.println("=== SISTEMA PRENOTAZIONE AEREO ===");
        
        try {
            socket = new Socket(HOST, PORTA);
            socket.setSoTimeout(30_000); // Limita l'attesa delle risposte.
            out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            scanner = new Scanner(System.in);
            
            System.out.println("Connessione al server riuscita!\n");
            
            // Autenticazione
            if (!login()) {
                System.out.println("Autenticazione fallita. Disconnessione...");
                socket.close();
                return;
            }
            
            // Menu principale
            menuPrincipale();
            
            // Chiusura
            out.println("QUIT");
            in.readLine(); // Consuma la conferma BYE prima della chiusura.
            socket.close();
            System.out.println("\nGrazie per aver utilizzato il servizio!");
            
        } catch (java.util.NoSuchElementException e) {
            System.out.println("Fine dell'input da tastiera");
        } catch (IOException e) {
            System.err.println("Errore connessione: " + e.getMessage());
        } finally {
            // Anche un errore o EOF della console passa da qui.
            if (socket != null) try { socket.close(); } catch (IOException ignored) { }
            if (scanner != null) scanner.close();
        }
    }
    
    private boolean login() {
        System.out.println("--- LOGIN ---");
        System.out.print("Username: ");
        username = scanner.nextLine();
        
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        // Invia credenziali
        out.println("AUTH " + username + " " + password);
        
        try {
            String risposta = in.readLine();
            
            if (risposta == null) return false;
            if (risposta.startsWith("AUTH_OK")) {
                System.out.println(" Login effettuato con successo!\n");
                return true;
            } else {
                String motivo = risposta.substring("AUTH_FAIL ".length());
                System.out.println(" Login fallito: " + motivo);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Errore durante login: " + e.getMessage());
            return false;
        }
    }
    
    private void menuPrincipale() {
        while (true) {
            System.out.println("\n--- MENU PRINCIPALE ---");
            System.out.println("1. Visualizza mappa posti");
            System.out.println("2. Prenota posto");
            System.out.println("3. Cancella prenotazione");
            System.out.println("4. Le mie prenotazioni");
            System.out.println("5. Esci");
            System.out.print("\nScelta > ");
            
            String scelta = scanner.nextLine();
            
            switch (scelta) {
                case "1":
                    visualizzaMappa();
                    break;
                case "2":
                    prenotaPosto();
                    break;
                case "3":
                    cancellaPrenotazione();
                    break;
                case "4":
                    visualizzaPrenotazioni();
                    break;
                case "5":
                    System.out.println("\nDisconnessione in corso...");
                    return;
                default:
                    System.out.println(" Scelta non valida");
            }
        }
    }
    
    private void visualizzaMappa() {
        try {
            out.println("VISUALIZZA");
            
            String linea;
            System.out.println("\n=== MAPPA POSTI AEREO ===");
            
            // Salta "MAPPA"
            in.readLine();
            
            // Le righe vuote sono contenuto; solo END termina la risposta.
            while ((linea = in.readLine()) != null && !linea.equals("END")) {
                System.out.println(linea);
            }
            
        } catch (IOException e) {
            System.err.println("Errore visualizzazione mappa: " + e.getMessage());
        }
    }
    
    private void prenotaPosto() {
        System.out.println("\n--- PRENOTA POSTO ---");
        
        System.out.print("Fila (1-6): ");
        String fila = scanner.nextLine();
        
        System.out.print("Posto (A-D): ");
        String posto = scanner.nextLine().toUpperCase();
        
        try {
            out.println("PRENOTA " + fila + " " + posto);
            String risposta = in.readLine();
            
            if (risposta == null) throw new EOFException("Server disconnesso");
            if (risposta.startsWith("PRENOTA_OK")) {
                String codice = risposta.substring("PRENOTA_OK ".length());
                System.out.println("\n Prenotazione confermata!");
                System.out.println("  Posto: " + codice);
                System.out.println("  Intestato a: " + username);
            } else {
                String motivo = risposta.substring("PRENOTA_FAIL ".length());
                System.out.println("\n Errore: " + motivo);
            }
            
        } catch (IOException e) {
            System.err.println("Errore prenotazione: " + e.getMessage());
        }
    }
    
    private void cancellaPrenotazione() {
        System.out.println("\n--- CANCELLA PRENOTAZIONE ---");
        
        System.out.print("Fila (1-6): ");
        String fila = scanner.nextLine();
        
        System.out.print("Posto (A-D): ");
        String posto = scanner.nextLine().toUpperCase();
        
        try {
            out.println("CANCELLA " + fila + " " + posto);
            String risposta = in.readLine();
            
            if (risposta == null) throw new EOFException("Server disconnesso");
            if (risposta.startsWith("CANCELLA_OK")) {
                String codice = risposta.substring("CANCELLA_OK ".length());
                System.out.println("\n Prenotazione cancellata!");
                System.out.println("  Posto: " + codice);
            } else {
                String motivo = risposta.substring("CANCELLA_FAIL ".length());
                System.out.println("\n Errore: " + motivo);
            }
            
        } catch (IOException e) {
            System.err.println("Errore cancellazione: " + e.getMessage());
        }
    }
    
    private void visualizzaPrenotazioni() {
        try {
            out.println("LISTA");
            
            String linea;
            System.out.println("\n--- LE TUE PRENOTAZIONI ---");
            
            // Salta "LISTA"
            in.readLine();
            
            // Consuma tutta la risposta fino al terminatore END.
            while ((linea = in.readLine()) != null && !linea.equals("END")) {
                System.out.println(linea);
            }
            
        } catch (IOException e) {
            System.err.println("Errore visualizzazione prenotazioni: " + e.getMessage());
        }
    }
}
