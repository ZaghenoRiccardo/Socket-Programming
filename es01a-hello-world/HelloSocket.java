public class HelloSocket {
    public static void main(String[] args) {
        String nome = "Mario";
        String messaggio = "Hello World sotto controllo vesione con Git.";

        System.out.println("Hello, World!");
        System.out.println("Studente: " + nome);
        System.out.println(messaggio);
        System.out.println("PID del processo: " + ProcessHandle.current().pid());
    }
}
