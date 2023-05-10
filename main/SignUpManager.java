package DigitalVault_INF1416.main;

import DigitalVault_INF1416.db.*;
import java.security.cert.*;
import java.sql.SQLException;
import java.io.*;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.*;

public class SignUpManager {
    
    private static String invalidEmail = "INVALID EMAIL";
    private static String invalidCommonName = "INVALID COMMON NAME";
    private static Scanner scanner;

    public static boolean singUp(){
        scanner = new Scanner(System.in);
        RawUser user = createRawUser();
        return saveUser(user);
    }

    private static RawUser createRawUser() {
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

        // user.printUser();
        // scanner.close();
        return user;
    }

    private static boolean saveUser(RawUser user){ 
        X509Certificate cert;
        try{
            cert = getCertificate(user.crtPath);
        }catch (Exception e){
            System.err.println(e.getMessage());
            System.err.println("Certificado Inválido");
            return false;
        }        

        printCertificate(cert);
        String email = extractEmail(cert.getSubjectX500Principal().toString());
        if (email.equals(invalidEmail)){
            System.err.println("Email Inválido");
            return false;
        }
            
        System.out.print("\nConfirma Dados (1 - Sim, 2 - Não): ");
        String confirm = scanner.nextLine();

        if (!confirm.equals("1")) return false;
        
        try {
            if (DBQueries.emailAlreadyTaken(email)){
                System.err.println("Email já cadastrado por outro usuário");
                return false;
            }
            saveData(user, cert, email);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Falha ao salvar no BD");
            return false;
        }
        
        return true;
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

    private static X509Certificate getCertificate(String path) throws CertificateException, IOException{
        FileInputStream fis = new FileInputStream(path);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)cf.generateCertificate(fis);
        fis.close();
        return cert;
    }

    private static void printCertificate(X509Certificate cert){
        System.out.println("\nCERTIFICATE INFO");
        System.out.println("Version: " + cert.getVersion());
        System.out.println("Serial number: " + cert.getSerialNumber());
        System.out.println("Validity period: " + cert.getNotBefore() + " - " + cert.getNotAfter());
        System.out.println("Algorithm: " + cert.getSigAlgName());

        String issuer = cert.getIssuerX500Principal().toString();
        String subject = cert.getSubjectX500Principal().toString();

        System.out.println("Issuer (Common Name): " + extractCommonName(issuer));
        System.out.println("Subject (Common Name): " + extractCommonName(subject));
        System.out.println("Login Name (Subject Email): " + extractEmail(subject));
    }

    private static String extractEmail(String subject){
        Pattern pattern = Pattern.compile("EMAILADDRESS=([^,]+)");
        Matcher matcher = pattern.matcher(subject);
        if (matcher.find())
            return matcher.group(1);
        return invalidEmail;
    }

    private static String extractCommonName(String subject){
        Pattern pattern = Pattern.compile("CN=([^,]+)");
        Matcher matcher = pattern.matcher(subject);
        if (matcher.find())
            return matcher.group(1);
        return invalidCommonName;
    }

    private static void saveData(RawUser user, X509Certificate cert, String email) throws SQLException{
        String crt = cert.toString();

        //TO DO: get private key
        String privateKey = user.privateKeyPath;

        //TO DO: hash password
        String hash = user.password;

        //TO DO: create token with secret
        String token = user.secret;

        Random rand = new Random();
        int kid = rand.nextInt();

        rand = new Random();
        int uid = rand.nextInt();

        int gid = (user.group == RawUser.Group.ADM) ? DBQueries.adminGID : DBQueries.userGID;

        DBQueries.insertKeys(kid, crt, privateKey);
        DBQueries.insertUser(uid, email, hash, token, kid, gid);
    }
}
