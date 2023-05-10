package DigitalVault_INF1416.main;

import java.util.Scanner;

public class SignUpManager {

    static RawUser createRawUser() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Nenhum usuário cadastrado, vamos criar o usuário admin :)");

        System.out.print("Caminho do arquivo do certificado digital: ");
        String certDig = scanner.nextLine();

        System.out.print("Caminho do arquivo da chave privada: ");
        String privateKey = scanner.nextLine();

        System.out.print("Frase secreta: ");
        String secret = scanner.nextLine();

        System.out.print("Grupo(1 - Administrador, 2 - Usuário): ");
        String group_string = scanner.nextLine();
        RawUser.Group group = group_string.equals("1") ? RawUser.Group.ADM : RawUser.Group.USR;
        
        System.out.print("Senha pessoal: ");
        String password = scanner.nextLine();

        while(!isPasswordValid(password)) {
            System.out.println("Digite a senha novamente, a senha deve conter 8, 9 ou 10 caracteres, a senha deve conter apenas digitos de 0 a 9, a senha não deve conter números repetidos em sequência");
            System.out.print("Senha pessoal: ");
            password = scanner.nextLine();
        }

        System.out.print("Confirmação senha pessoal: ");
        String password_conf = scanner.nextLine();

        while(!password.equals(password_conf)) {
            System.out.println("As senhas não coincidem :(, insira novamente");

            System.out.print("Senha: ");
            password = scanner.nextLine();

            while(!isPasswordValid(password)) {
                System.out.println("Digite a senha novamente, a senha deve conter 8, 9 ou 10 caracteres, a senha deve conter apenas digitos de 0 a 9, a senha não deve conter números repetidos em sequência");
                System.out.print("Senha pessoal: ");
                password = scanner.nextLine();
            }
    
            System.out.print("Confirmação de senha: ");
            password_conf = scanner.nextLine();
        }

        RawUser user = new RawUser(
            certDig, 
            privateKey, 
            secret, 
            group, 
            password
        );

        scanner.close();

        user.printUser();

        return user;
    }

    private static boolean isPasswordValid(String password) {
        Integer passLength = password.length();
        boolean hasValidSize = (passLength == 8 || passLength == 9 || passLength == 10);
        boolean hasOnlyDigits = (password.matches("\\d+"));
        boolean hasNoEqualDigitsInSequence = true;
        for(Integer i = 1; i < password.length(); i++) {
            if(password.charAt(i) == password.charAt(i - 1)) {
                hasNoEqualDigitsInSequence = false;
                break;
            }
        }
        return (hasValidSize && hasOnlyDigits && hasNoEqualDigitsInSequence);
                
    }
}
