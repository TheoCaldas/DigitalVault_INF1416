package DigitalVault_INF1416.main;

import java.security.cert.CertificateException;
import java.sql.SQLException;

import DigitalVault_INF1416.db.*;
import DigitalVault_INF1416.main.UIManager.UIAction;

public class Main {
    public static void main(String[] args) throws CertificateException, SQLException {
        // Inicia BD
        DBQueries.start();

        // Verificacao de usuarios
        System.out.println("Verificando a existência de usuários...");
        boolean hasUsers;
        try{
            hasUsers = DBQueries.hasUsers();
        }catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }

        if(hasUsers) {
           UIAction action = hasUsersFlow();
           while(action != UIAction.STOP_PROGRAM) {
                action = hasUsersFlow();
           }
        } else {
            // Cadastro admin
            System.out.println("Nenhum usuário! Por favor, cadastre o administrador.");
            SignUpManager.signUp();
        }
    }

    public static UIAction hasUsersFlow() throws SQLException, CertificateException {
        User loggedUser = LoginManager.getInstance().login();
        UIAction userFinalInput = loggedUser == null ? UIAction.STOP_PROGRAM : UIAction.BACK_TO_MENU;
        while(userFinalInput == UIAction.BACK_TO_MENU) {
            if (loggedUser != null) {
                DBQueries.updateUserLoginCount(loggedUser);
    
                UIAction userAction = UIAction.INVALID_INPUT;
                while (userAction == UIAction.INVALID_INPUT) {
                    if (loggedUser.gid == DBQueries.ADMIN_GID) {
                        userAction = UIManager.AdminFlow1(loggedUser);
                    } else {
                        userAction = UIManager.UserFlow1(loggedUser);
                    }
    
                    if (userAction == UIAction.INVALID_INPUT) {
                        System.out.println("Entrada Inválida");
                    }
                }
    
                switch (userAction) {
                    case SIGNUP:
                        UIAction signUpAction = SignUpManager.signUp();
                        userFinalInput = signUpAction;
                        break;
                    case QUERY:
                        System.out.println("\nLendo o cofre");
                        DBQueries.updateUserQueriesCount(loggedUser);
                        UIAction readVaultAction = VaultManager.readVault(loggedUser);
                        userFinalInput = readVaultAction;
                        break;
                    case SIGNOUT:
                        UIAction signOutAction = UIManager.signOutFlow(loggedUser);
                        userFinalInput = signOutAction;
                        break;
                    default:
                        userFinalInput = UIAction.BACK_TO_MENU;
                        break;
                }
            }
        }
        return userFinalInput;
    }
}
