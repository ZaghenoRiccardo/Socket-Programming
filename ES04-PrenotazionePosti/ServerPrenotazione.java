
import java.io.*;
import java.net.*;
import java.util.*;

public class ServerPrenotazione {
    private static final int PORTA = 8888;
    private static final int NUM_FILE = 6;
    private static final int NUM_POSTI_PER_FILA = 4;
    private static final char[] LETTERE_POSTI = { 'A', 'B', 'C', 'D' };

    private static Posto[][] posti;

    public static void main(String[] args) {
        System.out.println("=== SERVER PRENOTAZIONE POSTI AEREO ===");

        // Inizializza matrice posti
        inizializzaPosti();

        System.out.println("Server avviato sulla porta " + PORTA);
        System.out.println("Aereo configurato: " + NUM_FILE + " file  " +
                NUM_POSTI_PER_FILA + " posti (" +
                (NUM_FILE * NUM_POSTI_PER_FILA) + " posti totali)");
        System.out.println("In attesa di connessioni...\n");

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }

    private static void inizializzaPosti() {
        posti = new Posto[NUM_FILE][NUM_POSTI_PER_FILA];
        for (int i = 0; i < NUM_FILE; i++) {
            for (int j = 0; j < NUM_POSTI_PER_FILA; j++) {
                posti[i][j] = new Posto(i + 1, LETTERE_POSTI[j]);
            }
        }
    }

    public static synchronized String getMappaPosti(String username) {
        StringBuilder sb = new StringBuilder();
        sb.append("MAPPA\n");

        // Header con lettere
        sb.append("     ");
        for (char lettera : LETTERE_POSTI) {
            sb.append(lettera).append("    ");
        }
        sb.append("\n");

        // Righe con posti
        for (int i = 0; i < NUM_FILE; i++) {
            sb.append(String.format("%-2d", (i + 1))).append("  ");
            for (int j = 0; j < NUM_POSTI_PER_FILA; j++) {
                Posto posto = posti[i][j];
                if (posto.isPrenotato()) {
                    if (posto.getUsernamePrenotazione().equals(username)) {
                        sb.append("[X]  "); // Prenotato dall'utente
                    } else {
                        sb.append("[#]  "); // Prenotato da altri
                    }
                } else {
                    sb.append("[ ]  "); // Libero
                }
            }
            sb.append("\n");
        }

        // Statistiche
        int liberi = contaPostiLiberi();
        int totali = NUM_FILE * NUM_POSTI_PER_FILA;
        sb.append("\nPosti liberi: ").append(liberi).append("/").append(totali);
        sb.append("\n[X] = Tue prenotazioni");
        sb.append("\n[#] = Prenotato da altri");

        return sb.toString();
    }

    public static synchronized String prenotaPosto(int fila, char lettera, String username) {
        // Valida input
        if (fila < 1 || fila > NUM_FILE) {
            return "PRENOTA_FAIL Fila non valida (1-" + NUM_FILE + ")";
        }

        int colonna = trovaColonna(lettera);
        if (colonna == -1) {
            return "PRENOTA_FAIL Posto non valido (A-" + LETTERE_POSTI[NUM_POSTI_PER_FILA - 1] + ")";
        }

        Posto posto = posti[fila - 1][colonna];

        if (posto.prenota(username)) {
            System.out.println("[PRENOTA] " + username + " -> Fila " + fila +
                    ", Posto " + lettera + " - SUCCESSO");
            return "PRENOTA_OK " + posto.getCodice();
        } else {
            System.out.println("[PRENOTA] " + username + " -> Fila " + fila +
                    ", Posto " + lettera + " - ERRORE: Gia prenotato");
            String proprietario = posto.getUsernamePrenotazione();
            return "PRENOTA_FAIL Posto gia prenotato da " + proprietario;
        }
    }

    public static synchronized String cancellaPosto(int fila, char lettera, String username) {
        if (fila < 1 || fila > NUM_FILE) {
            return "CANCELLA_FAIL Fila non valida";
        }

        int colonna = trovaColonna(lettera);
        if (colonna == -1) {
            return "CANCELLA_FAIL Posto non valido";
        }

        Posto posto = posti[fila - 1][colonna];

        if (posto.cancella(username)) {
            System.out.println("[CANCELLA] " + username + " -> Posto " + posto.getCodice());
            return "CANCELLA_OK " + posto.getCodice();
        } else {
            return "CANCELLA_FAIL Non puoi cancellare questo posto";
        }
    }

    public static synchronized String getListaPrenotazioni(String username) {
        StringBuilder sb = new StringBuilder("LISTA\n");
        List<String> prenotazioni = new ArrayList<>();

        for (int i = 0; i < NUM_FILE; i++) {
            for (int j = 0; j < NUM_POSTI_PER_FILA; j++) {
                Posto posto = posti[i][j];
                if (posto.isPrenotato() &&
                        posto.getUsernamePrenotazione().equals(username)) {
                    prenotazioni.add(posto.getCodice());
                }
            }
        }

        if (prenotazioni.isEmpty()) {
            sb.append("Nessuna prenotazione attiva");
        } else {
            sb.append("Le tue prenotazioni:\n");
            for (String codice : prenotazioni) {
                sb.append("- Posto ").append(codice).append("\n");
            }
        }

        return sb.toString();
    }

    private static int trovaColonna(char lettera) {
        for (int i = 0; i < LETTERE_POSTI.length; i++) {
            if (LETTERE_POSTI[i] == Character.toUpperCase(lettera)) {
                return i;
            }
        }
        return -1;
    }

    private static int contaPostiLiberi() {
        int count = 0;
        for (int i = 0; i < NUM_FILE; i++) {
            for (int j = 0; j < NUM_POSTI_PER_FILA; j++) {
                if (!posti[i][j].isPrenotato()) {
                    count++;
                }
            }
        }
        return count;
    }

    public static AutenticazioneManager getAuthManager() {
        return authManager;
    }

    public static void stampaStatistiche() {
        int liberi = contaPostiLiberi();
        int totali = NUM_FILE * NUM_POSTI_PER_FILA;
        System.out.println("\n--- STATISTICHE ---");
        System.out.println("Posti totali: " + totali);
        System.out.println("Posti prenotati: " + (totali - liberi));
        System.out.println("Posti liberi: " + liberi);
        System.out.println("-------------------\n");
    }
}

