package DigitalVault_INF1416.main;

public class Main {
    public static void main(String[] args) {
        
        // Verificacao de usuarios
        System.out.println("Verificando a existência de usuários...");

        if(hasUsers()) {
            // pede o login
        } else {
            // Cadastro admin
            RawUser rawUser = SignUpManager.createRawUser();
            

        }
    }

    public static boolean hasUsers() {
        // verifica no banco se tem usuarios
        return false;
    }
}
