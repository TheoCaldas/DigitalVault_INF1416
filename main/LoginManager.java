package DigitalVault_INF1416.main;

import DigitalVault_INF1416.db.*;

import java.util.Scanner;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

public class LoginManager {
    private static Scanner scanner;

    public static void login(){
        scanner = new Scanner(System.in);
        while (true){
            System.out.println("======TELA DE LOGIN======");
            
            System.out.print("Digite 1 para iniciar login - Digite 2 para voltar: ");
            String option = scanner.nextLine();

            if (option.equals("2")) break;

            User user = firstStep();

            if(user != null) {
                if(secondStep(user)) {
                    System.out.println("Senha correta");
                }
            }

        }
    }

    public static User firstStep(){
        System.out.print("Email cadastrado: ");
        String email = scanner.nextLine();

        User user = null;
        try {
            user = DBQueries.selectUser(email);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Erro ao buscar por email!");
        }
        if (user == null){
            System.err.println("Email não cadastrado!");
        }
        // TO DO: verificar bloqueado

        return user;
    }

    public static boolean secondStep(User user) {
        String[] passwords = PasswordManager.passwordInput();
        boolean isValidPassword = false;

        for (int i = 0; i < passwords.length; i++) {
            char[] password = passwords[i].toCharArray();
            String userHash = user.hash;
            if (OpenBSDBCrypt.checkPassword(userHash, password)) {
                isValidPassword = true;
                break;
            } 
        }

        
        return isValidPassword;
    }

    // private static String getHash(String hash) {
    //     String[] parts = hash.split("\\$");
    //     System.out.println(Arrays.toString(parts));
    //     String hashSalt = parts[3];
    //     String decodedHash = "$" + parts[1] + "$" + parts[2] + "$" + Base64.getDecoder().decode(hashSalt).toString();
    //     return decodedHash;
    // }
}
