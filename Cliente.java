package ChatTCP;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {

    private static int contadorUnico = 0;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite o seu nome: ");
        String nome = scanner.nextLine();

        Socket socket = new Socket("localhost", 4000);
        PrintStream saida = new PrintStream(socket.getOutputStream());
        String mensagem;

        int chaveInicial = gerarChaveUnica();
        String mensagemConexao = cifraTransposicao(nome + " conectou-se", chaveInicial);
        saida.println(chaveInicial + "___" + mensagemConexao);

        while (true) {
            mensagem = scanner.nextLine();

            int chave = gerarChaveUnica();

            String mensagemCifrada = cifraTransposicao(nome + ": " + mensagem, chave);
            saida.println(chave + "___" + mensagemCifrada);
        }
    }

    private static int gerarChaveUnica() {
        return contadorUnico++;
    }

    private static String cifraTransposicao(String mensagem, int chave) {
        StringBuilder cifrada = new StringBuilder();

        for (char c : mensagem.toCharArray()) {
            cifrada.append((char) ((c + chave) % 65536));
        }

        return cifrada.toString();
    }
}