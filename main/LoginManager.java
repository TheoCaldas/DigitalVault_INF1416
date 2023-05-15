package DigitalVault_INF1416.main;

import DigitalVault_INF1416.db.*;

import java.util.Date;
import java.util.Scanner;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

public class LoginManager {
    private final static long BLOCKED_WAIT = 120000; //blocked waiting time in miliseconds

    private static Scanner scanner;
    private static long elapsedMiliseconds;
    private static User currentUser;
    

    public static void login(){
        scanner = new Scanner(System.in);
        int error2Count = 0; //count of failed logins on step 2
        int error3Count = 0; //count of failed logins on step 3
        boolean step1Check = false; //has passed on step 1
        boolean step2Check = false; //has passed on step 2

        while (true){
            System.out.println("======TELA DE LOGIN======");
            
            System.out.print("Digite 1 para iniciar login - Digite 2 para voltar: ");
            String option = scanner.nextLine();

            if (option.equals("2")) break;

            if (step1Check || (step1Check = firstStep())){
                if (step2Check || (step2Check = secondStep())){
                    if (thirdStep()){
                        System.out.println("Login realizado!");
                        break;
                    }
                    else
                        error3Count++;
                }else
                    error2Count++;
            }

            if (error2Count >= 3){ 
                if (!blockUser(currentUser)) //bloqueia usuario
                    System.err.println("Falha ao bloquear usuário!");
                else
                    System.err.println("3 falhas consecutivas. Usuário bloqueado!");
                error2Count = 0;
                step1Check = false;
            }

            System.out.println("\n\nLogin não realizado. Tente novamente.\n");
        }
    }

    public static boolean firstStep(){
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

        if (isUserBlocked(user)){
            long waitSeconds = (BLOCKED_WAIT - elapsedMiliseconds) / 1000;
            System.err.println("Usuário está bloqueado! Tente novamente em " + waitSeconds + " segundos.");
            return false;
        }

        currentUser = user;
        return true;
    }

    public static boolean secondStep() {
        String userHash = currentUser.hash;
        String[] passwords = PasswordManager.passwordInput();
        boolean isValidPassword = false;

        for (int i = 0; i < passwords.length; i++) {
            char[] password = passwords[i].toCharArray();
            
            if (OpenBSDBCrypt.checkPassword(userHash, password)) {
                isValidPassword = true;
                break;
            } 
        }

        
        return isValidPassword;
    }

    public static boolean thirdStep(){
        return true;
    }

    private static boolean isUserBlocked(User user){
        if (user.blocked == null)
            return false;
        Date nowDate = new Date();
        Date blockedDate = user.blocked;
        elapsedMiliseconds = (nowDate.getTime() - blockedDate.getTime());
        if (elapsedMiliseconds > BLOCKED_WAIT)
            return false;
        return true;
    }

    private static boolean blockUser(User user) {
        Date nowDate = new Date();
        try {
            return DBQueries.blockUser(user, nowDate);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}
