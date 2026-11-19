/** Un solo titolare per posto: verifica e assegnazione devono essere indivisibili. */
public class Posto {
    private int fila;
    private char lettera;
    private boolean prenotato;
    private String usernamePrenotazione;
    
    public Posto(int fila, char lettera) {
        this.fila = fila;
        this.lettera = lettera;
        this.prenotato = false;
        this.usernamePrenotazione = null;
    }
    
    // Controllare e assegnare sotto lo stesso monitor evita due vincitori sul medesimo posto.
    public synchronized boolean prenota(String username) {
        if (prenotato) {
            return false; // Già prenotato
        }
        this.prenotato = true;
        this.usernamePrenotazione = username;
        return true;
    }
    
    public synchronized boolean cancella(String username) {
        if (!prenotato) {
            return false; // Non prenotato
        }
        if (!usernamePrenotazione.equals(username)) {
            return false; // Prenotato da altro utente
        }
        this.prenotato = false;
        this.usernamePrenotazione = null;
        return true;
    }
    
    public synchronized boolean isPrenotato() {
        return prenotato;
    }
    
    public synchronized String getUsernamePrenotazione() {
        return usernamePrenotazione;
    }
    
    public int getFila() {
        return fila;
    }
    
    public char getLettera() {
        return lettera;
    }
    
    public String getCodice() {
        return fila + "" + lettera;
    }
    
    @Override
    public synchronized String toString() {
        if (prenotato) {
            return "[X:" + usernamePrenotazione + "]";
        }
        return "[ ]";
    }
}
