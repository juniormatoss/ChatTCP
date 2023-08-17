package ChatTCP;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Servidor {

    private static final List<PrintStream> FLUXOS_SAIDA_CLIENTE = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket socketServidor = new ServerSocket(4000);
        System.out.println("Aguardando conexões...");

        while (true) {
            Socket socket = socketServidor.accept();
            System.out.println("Conectado: " + socket.getInetAddress());

            PrintStream saidaCliente = new PrintStream(socket.getOutputStream());
            FLUXOS_SAIDA_CLIENTE.add(saidaCliente);

            Thread thread = new Thread(new ManipuladorCliente(socket, saidaCliente));
            thread.start();
        }
    }

    private static class ManipuladorCliente implements Runnable {
        private Socket socket;
        private PrintStream saidaCliente;

        public ManipuladorCliente(Socket socket, PrintStream saidaCliente) {
            this.socket = socket;
            this.saidaCliente = saidaCliente;
        }

        @Override
        public void run() {
            try {
                BufferedReader leitor = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String mensagem;
                while ((mensagem = leitor.readLine()) != null) {
                    String[] partes = mensagem.split("___");
                    int chave = Integer.parseInt(partes[0]);
                    mensagem = partes[1];

                    System.out.println("Mensagem criptografada: " + mensagem);

                    String mensagemDecifrada = decifrarTransposicao(mensagem, chave);

                    synchronized (FLUXOS_SAIDA_CLIENTE) {
                        for (PrintStream cliente : FLUXOS_SAIDA_CLIENTE) {
                            if (cliente != saidaCliente) {
                                cliente.println(chave + "___" + mensagemDecifrada);
                            }
                        }
                    }
                }

                leitor.close();
                socket.close();

                synchronized (FLUXOS_SAIDA_CLIENTE) {
                    FLUXOS_SAIDA_CLIENTE.remove(saidaCliente);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String decifrarTransposicao(String mensagemCifrada, int chave) {
        StringBuilder decifrada = new StringBuilder();

        for (char c : mensagemCifrada.toCharArray()) {
            decifrada.append((char) ((c - chave + 65536) % 65536));
        }

        return decifrada.toString();
    }
}