package DigitalVault_INF1416.main;

import DigitalVault_INF1416.db.*;

import java.util.Scanner;

public class LoginManager {
    private static Scanner scanner;

    public static void login(){
        scanner = new Scanner(System.in);
        while (true){
            System.out.println("======TELA DE LOGIN======");
            
            System.out.print("Digite 1 para iniciar cadastro - Digite 2 para voltar: ");
            String option = scanner.nextLine();

            if (option.equals("2")) break;

            if (firstStep()){
                System.out.println("Login realizado!");
                break;
            }else
                System.out.println("\n\nLogin não realizado. Tente novamente.\n");
        }
    }

    public static boolean firstStep(){
        System.out.print("Email cadastrado: ");
        String email = scanner.nextLine();

        User user;
        try {
            user = DBQueries.selectUser(email);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Erro ao buscar por email!");
            return false;
        }
        if (user == null){
            System.err.println("Email não cadastrado!");
            return false;
        }
        // TO DO: verificar bloqueado

        return true;
    }
}
