/* INF1416 2023.1 - Trabalho 4
 * Theo Caldas - 1911078    
 * Matheus Kulick - 1911090
 */
package DigitalVault_INF1416.main;

import java.io.Console;
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

        LogManager.addRegister(1001, null, null);
        boolean hasUsers;
        try{
            hasUsers = DBQueries.hasUsers();
        }catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }

        if(hasUsers) {
            askAdminPK();
            UIAction action = hasUsersFlow();
            while(action != UIAction.STOP_PROGRAM) {
                    action = hasUsersFlow();
            }
        } else {
            // Cadastro admin
            System.out.println("Nenhum usuário! Por favor, cadastre o administrador.");
            SignUpManager.signUp();
        }
        LogManager.addRegister(1002, null, null);
    }

    public static UIAction hasUsersFlow() throws SQLException, CertificateException {
        User loggedUser = LoginManager.getInstance().login();
        UIAction userFinalInput = loggedUser == null ? UIAction.STOP_PROGRAM : UIAction.BACK_TO_MENU;
        if(loggedUser != null) LogManager.addRegister(5001, loggedUser.email, null);
        while(userFinalInput == UIAction.BACK_TO_MENU) {
            if (loggedUser != null) {
                LogManager.addRegister(1003, loggedUser.email, null);
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
                        UIAction readVaultAction = VaultManager.listVault(loggedUser);
                        userFinalInput = readVaultAction;
                        break;
                    case SIGNOUT:
                        UIAction signOutAction = UIManager.signOutFlow(loggedUser);
                        userFinalInput = signOutAction;
                        LogManager.addRegister(1004, loggedUser.email, null);
                        break;
                    default:
                        userFinalInput = UIAction.BACK_TO_MENU;
                        break;
                }
            }
        }
        return userFinalInput;
    }

    private static void askAdminPK(){
        Console console = System.console();
        char[] adminSecret = console.readPassword("Entre com a frase secreta do admin: ");
        VaultManager.ADMIN_SECRET = new String(adminSecret);
    }
}
