/* INF1416 2023.1 - Trabalho 4
 * Theo Caldas - 1911078    
 * Matheus Kulick - 1911090
 */
package DigitalVault_INF1416.main;

import java.security.cert.*;
import java.sql.SQLException;
import java.util.Scanner;

import DigitalVault_INF1416.db.KeyChain;
import DigitalVault_INF1416.db.LogManager;
import DigitalVault_INF1416.db.DBQueries;
import DigitalVault_INF1416.db.User;

public class UIManager {

    public static Scanner scanner = new Scanner(System.in);

    public enum UIAction {
        SIGNUP, QUERY, SIGNOUT, INVALID_INPUT, STOP_PROGRAM, BACK_TO_MENU, SIGNOUT_CONFIRM
    }

    public static UIAction UserFlow1(User user) throws CertificateException, SQLException {
        header(user);
        body1Access(user);
        UIAction action = body2User(user);
        return action;
    }

    public static UIAction AdminFlow1(User user) throws CertificateException, SQLException {
        header(user);
        body1Access(user);
        UIAction action = body2Admin();
        return action;
    }

    public static void vaultFlow(User user) throws CertificateException, SQLException {
        header(user);
        body1Queries(user);
    }

    public static UIAction signOutFlow(User user) throws CertificateException, SQLException {
        header(user);
        body1Access(user);
        UIAction action = body2SignOut();
        return action;
    }

    public static void header(User user) throws CertificateException, SQLException {
        KeyChain usrCrt = DBQueries.getKeyChain(user.kid); 
        String crtSubject = usrCrt.crt.getSubjectX500Principal().toString();
        String username = CertificateManager.extractCommonName(crtSubject);

        System.out.println(" ____________________________________");
        System.out.println("|Login: " + user.email);
        System.out.println("|Grupo: " + (user.gid == DBQueries.ADMIN_GID ? "ADMIN" : "USER"));
        System.out.println("|Nome: " + username);
    }

    public static void body1Access(User user) {
        System.out.println(" ____________________________________");
        System.out.println("|Total de acessos do usuários: " + user.nLogins);
    }

    public static void body1Queries(User user) {
        System.out.println(" ____________________________________");
        System.out.println("|Total de consultas do usuários: " + user.nLogins);
    }

    public static void body1Users(User user) throws SQLException {
        int nUsers = DBQueries.getUsersCount();
        System.out.println(" ____________________________________");
        System.out.println("|Total de usuários: " + nUsers);
    }

    public static UIAction body2Admin() {
        System.out.println(" ____________________________________");
        System.out.println("|Menu Principal");
        System.out.println("|1 => Cadastrar um novo usuário");
        System.out.println("|2 => Consultar pasta de arquivos secretos do usuário");
        System.out.println("|3 => Sair do Sistema");
        System.out.println(" ____________________________________");

        System.out.print("\nEscolha umas das opções do Menu Principal(1, 2 ou 3): ");
        String input = scanner.next();
        switch (input) {
            case "1":
                return UIAction.SIGNUP;
            case "2":
                return UIAction.QUERY;
            case "3":
                return UIAction.SIGNOUT;
        }
        return UIAction.INVALID_INPUT;
    }

    public static UIAction body2User() {
        System.out.println(" ____________________________________");
        System.out.println("|Menu Principal");
        System.out.println("|1 => Consultar pasta de arquivos secretos do usuário");
        System.out.println("|2 => Sair do Sistema");
        System.out.println(" ____________________________________");

        System.out.print("\nEscolha umas das opções do Menu Principal(1 ou 2): ");
        String input = scanner.next();
        switch (input) {
            case "1":
                return UIAction.QUERY;
            case "2":
                return UIAction.SIGNOUT;
        }
        return UIAction.INVALID_INPUT;
    }

    public static UIAction body2SignOut(User user) {
        LogManager.addRegister(8001, user.email, null);
        System.out.println(" ____________________________________");
        System.out.println("|Saída do Sistema:");
        System.out.println("|Mensagem de Saída.");
        System.out.println("|1 => Encerrar Sessão");
        System.out.println("|2 => Encerrar Sistema");
        System.out.println("|3 => Voltar ao Menu Principal");
        System.out.println(" ____________________________________");

        System.out.print("\nEscolha umas das opções da Saída do Sistema:(1, 2 ou 3): ");
        String input = scanner.next();
        switch (input) {
            case "1":
                LogManager.addRegister(8002, user.email, null);
                return UIAction.SIGNOUT_CONFIRM;
            case "2":
                LogManager.addRegister(8003, user.email, null);
                return UIAction.STOP_PROGRAM;
            case "3":
                LogManager.addRegister(8004, user.email, null);
                return UIAction.BACK_TO_MENU;
        }
        return UIAction.INVALID_INPUT;
    }
}
