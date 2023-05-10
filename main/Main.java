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
            // TO DO: pede o login
            System.out.println("LOGIN!");
        } else {
            // Cadastro admin
            RawUser rawUser = SignUpManager.createRawUser();
        }
    }
}
