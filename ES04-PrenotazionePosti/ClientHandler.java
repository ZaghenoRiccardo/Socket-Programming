/** Il gestore possiede una sessione: input, output e chiusura appartengono a questa connessione. */
import java.io.*;
import java.net.*;

class ClientHandler implements Runnable {
    private Socket socket;
    private String username;
    private boolean autenticato;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.username = null;
        this.autenticato = false;
    }
    
    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8)
        ) {
            String clientIP = socket.getInetAddress().getHostAddress();
            System.out.println("[CONNESSIONE] Client da " + clientIP);
            
            // Ciclo principale
            String comando;
            while ((comando = in.readLine()) != null) {
                String risposta = processaComando(comando);
                out.println(risposta);
                // END delimita anche una lista vuota: il client può inviare il comando successivo.
                if (comando.equals("VISUALIZZA") || comando.equals("LISTA")) out.println("END");
                
                if (comando.equals("QUIT")) {
                    break;
                }
            }
            
            if (username != null) {
                System.out.println("[DISCONNESSIONE] " + username + " disconnesso");
            } else {
                System.out.println("[DISCONNESSIONE] Client non autenticato disconnesso");
            }
            
        } catch (IOException e) {
            System.err.println("[ERRORE] Gestione client: " + e.getMessage());
        }
    }
    
    private String processaComando(String comando) {
        String[] parti = comando.split(" ", 3);
        String azione = parti[0];
        
        // Comandi che non richiedono autenticazione
        if (azione.equals("AUTH")) {
            if (parti.length < 3) {
                return "AUTH_FAIL Formato: AUTH username password";
            }
            return handleAuth(parti[1], parti[2]);
        }
        
        // Tutti gli altri comandi richiedono autenticazione
        if (!autenticato) {
            return "ERROR Autenticazione richiesta";
        }
        
        switch (azione) {
            case "VISUALIZZA":
                System.out.println("[VISUALIZZA] Mappa inviata a " + username);
                return PrenotazioneServer.getMappaPosti(username);
                
            case "PRENOTA":
                if (parti.length < 3) {
                    return "PRENOTA_FAIL Formato: PRENOTA fila posto";
                }
                return handlePrenota(parti[1], parti[2]);
                
            case "CANCELLA":
                if (parti.length < 3) {
                    return "CANCELLA_FAIL Formato: CANCELLA fila posto";
                }
                return handleCancella(parti[1], parti[2]);
                
            case "LISTA":
                return PrenotazioneServer.getListaPrenotazioni(username);
                
            case "QUIT":
                return "BYE Arrivederci";
                
            default:
                return "ERROR Comando non riconosciuto";
        }
    }
    
    private String handleAuth(String user, String password) {
        System.out.println("[AUTH] Tentativo login: " + user);
        
        if (PrenotazioneServer.getAuthManager().autentica(user, password)) {
            this.username = user;
            this.autenticato = true;
            System.out.println("[AUTH] Login riuscito: " + user);
            return "AUTH_OK Benvenuto " + user;
        } else {
            System.out.println("[AUTH] Login fallito: " + user);
            return "AUTH_FAIL Credenziali non valide";
        }
    }
    
    private String handlePrenota(String filaStr, String postoStr) {
        try {
            int fila = Integer.parseInt(filaStr);
            if (postoStr.length() != 1) return "PRENOTA_FAIL Posto deve essere una lettera A-D";
            char posto = postoStr.toUpperCase(java.util.Locale.ROOT).charAt(0);
            return PrenotazioneServer.prenotaPosto(fila, posto, username);
        } catch (NumberFormatException e) {
            return "PRENOTA_FAIL Fila deve essere un numero";
        } catch (Exception e) {
            return "PRENOTA_FAIL Errore: " + e.getMessage();
        }
    }
    
    private String handleCancella(String filaStr, String postoStr) {
        try {
            int fila = Integer.parseInt(filaStr);
            if (postoStr.length() != 1) return "CANCELLA_FAIL Posto deve essere una lettera A-D";
            char posto = postoStr.toUpperCase(java.util.Locale.ROOT).charAt(0);
            return PrenotazioneServer.cancellaPosto(fila, posto, username);
        } catch (NumberFormatException e) {
            return "CANCELLA_FAIL Fila deve essere un numero";
        } catch (Exception e) {
            return "CANCELLA_FAIL Errore: " + e.getMessage();
        }
    }
}
