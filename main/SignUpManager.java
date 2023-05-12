package DigitalVault_INF1416.main;

import DigitalVault_INF1416.db.*;

import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import javax.crypto.*;

import java.sql.SQLException;
import java.io.*;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.*;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

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

        return user;
    }

    private static boolean saveUser(RawUser user){ 
        //acessar certificado
        X509Certificate cert;
        try{
            cert = getCertificate(user.crtPath);
        }catch (Exception e){
            System.err.println(e.getMessage());
            System.err.println("Certificado Inválido");
            return false;
        }        

        //confirmar dados
        printCertificate(cert);
        String email = extractEmail(cert.getSubjectX500Principal().toString());
        if (email.equals(invalidEmail)){
            System.err.println("Email Inválido");
            return false;
        }
            
        System.out.print("\nConfirma Dados (1 - Sim, 2 - Não): ");
        String confirm = scanner.nextLine();

        if (!confirm.equals("1")) return false;
        
        //verificar se email ja foi tomado
        try {
            if (DBQueries.emailAlreadyTaken(email)){
                System.err.println("Email já cadastrado por outro usuário");
                return false;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }

        //acessar chave privada
        PrivateKey privateKey;
        try{
            privateKey = getPrivateKey(user.privateKeyPath, user.secret);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Chave Privada Inválida");
            return false;
        }

        try {
            if (!validateKeys(cert, privateKey)){
                System.err.println("Chave privada não compatível com certificado!");
                return false;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        } 
        
        String hash = hashPassword(user.password);

        //TO DO: create token
        String token = user.secret;

        //salvar tudo no BD
        try {
            saveData(email, hash, privateKey, token, user.group, cert);
        } catch (Exception er) {
            System.err.println(er.getMessage());
            System.err.println("Falha ao a BD");
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

    private static PrivateKey getPrivateKey(String path, String secret) throws 
    IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
     IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {

        Security.addProvider(new BouncyCastleProvider());
    
        //read encrypted private key in path
        FileInputStream fis = new FileInputStream(path);
        byte[] encryptedKeyBytes = new byte[fis.available()];
        fis.read(encryptedKeyBytes);
        fis.close();

        //generate secretKey with PRNG and secret string
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(secret.getBytes());
        KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
        keyGenerator.init(56, random);
		SecretKey secretKey = keyGenerator.generateKey();

        //decrypt private key with secretKey
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedKeyBytes = cipher.doFinal(encryptedKeyBytes);

        //remove headers
        String encodedPK = new String(decryptedKeyBytes);
        encodedPK = encodedPK.replace("-----BEGIN PRIVATE KEY-----", "");
        encodedPK = encodedPK.replace("-----END PRIVATE KEY-----", "");
        encodedPK = encodedPK.replace("\n", "");

        //decode base64
        byte[] decodedPK = Base64.getDecoder().decode(encodedPK);

        //use key spec and key factory to get final private key
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPK);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        return privateKey;
    }

    private static void saveData(String email, String hash, PrivateKey privateKey, String token, 
    RawUser.Group group, X509Certificate cert) throws SQLException{

        //TO DO: Ver qual forma correta de armazenar pk e crt
        String crt = cert.toString();
        String pk = privateKey.toString();

        Random rand = new Random();
        int kid = rand.nextInt();

        rand = new Random();
        int uid = rand.nextInt();

        int gid = (group == RawUser.Group.ADM) ? DBQueries.adminGID : DBQueries.userGID;

        DBQueries.insertKeys(kid, crt, pk);
        DBQueries.insertUser(uid, email, hash, token, kid, gid);
    }

    private static boolean validateKeys(X509Certificate cert, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
        PublicKey publicKey = cert.getPublicKey();

        //Generate random bytes
        byte[] randomBytes = new byte[4096];
		SecureRandom.getInstanceStrong().nextBytes(randomBytes);

        //Sign with private key
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initSign(privateKey);
		signature.update(randomBytes);
		byte[] signedBytes = signature.sign();

        //Verify with public key
        signature.initVerify(publicKey);
		signature.update(randomBytes);
        return signature.verify(signedBytes);
    }

    private static String hashPassword(String password){
        final byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String hash = OpenBSDBCrypt.generate(password.getBytes(), salt, 12);
        String encodedSalt = Base64.getEncoder().encodeToString(salt);
        String encodedHash = Base64.getEncoder().encodeToString(hash.getBytes());
        return "$2y$12$" + encodedSalt + encodedHash;
    }
}
