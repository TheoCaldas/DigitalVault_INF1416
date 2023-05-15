package DigitalVault_INF1416.itoken;

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Scanner;
import DigitalVault_INF1416.main.LoginManager;

public class IToken extends LoginManager{
    public static void main(String[] args) {
        IToken itokenApp = new IToken();
        itokenApp.token();
    }

    public void token(){
        scanner = new Scanner(System.in);

        String[] data = readTempFile();
        String hash = data[0];
        String token = data[1];

        String option = intro();
        if (option.equals("2")) return;
        while (!checkPassword(hash)){
            System.err.println("Senha Inválida!");
            option = intro();
            if (option.equals("2")) return;
        }
        if (!printToken(token))
            System.err.println("Falha ao gerar token!");
    }

    public String intro(){
        System.out.println("======APLICATIVO iTOKEN======");
        System.out.print("Digite 1 para acessar - Digite 2 para sair: ");
        return scanner.nextLine();
    }

    public boolean checkPassword(String hash) {
        return validatePassword(hash);
    }

    public boolean printToken(String token) {
        String decryptedUserToken;
        try {
            decryptedUserToken = decryptToken(token, currentPassword);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Falha ao decriptar semente!");
            return false;
        } 

        String finalToken;
        try {
            finalToken = generateFinalToken(decryptedUserToken, 0);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Falha ao gerar tokens!");
            return false;
        }

        System.out.println("Token gerado: " + finalToken);
        return true;
    }

    private String[] readTempFile(){
        String filename = TEMP_FILENAME;
        String[] data = new String[2];

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            data[0] = reader.readLine();
            data[1] = reader.readLine();
        } catch (Exception e) {
            System.err.println("Falha ao ler token.txt!");
            return null;
        }

        return data;
    }
}
