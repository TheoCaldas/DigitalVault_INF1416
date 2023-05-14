package DigitalVault_INF1416.main;

import DigitalVault_INF1416.db.*;

public class Main {
    public static void main(String[] args) {
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
            LoginManager.login();
        } else {
            // Cadastro admin
            System.out.println("Nenhum usuário! Por favor, cadestre o administrador.");
            SignUpManager.singUp();
        }
    }
}
