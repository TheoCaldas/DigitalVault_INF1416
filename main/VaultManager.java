/* INF1416 2023.1 - Trabalho 4
 * Theo Caldas - 1911078    
 * Matheus Kulick - 1911090
 */
package DigitalVault_INF1416.main;

import java.io.Console;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import DigitalVault_INF1416.db.*;
import DigitalVault_INF1416.main.UIManager.UIAction;

public class VaultManager {

    public static Scanner scanner;
    public static String ADMIN_SECRET;
    
    public static UIAction listVault(User user) throws SQLException, CertificateException {
        UIManager.vaultFlow(user);
        scanner = new Scanner(System.in);
        Console console = System.console();
        System.out.println(" ____________________________________");
        System.out.print("|Caminho da pasta: ");
        String folderPath = scanner.nextLine();
        char[] secretChar = console.readPassword("Frase Secreta: ");
        String secret = new String(secretChar);

        User admin;
        try {
            admin = DBQueries.selectAdmin();
        } catch (Exception e) {
            return UIAction.BACK_TO_MENU;
        }

        byte[] file = readVault(admin, folderPath, "index", ADMIN_SECRET);
        if (file == null){
            System.out.println("Falha ao acessar indice!");
            return UIAction.BACK_TO_MENU;
        }
        String indexString = new String(file);
        String[] files = indexString.split("\n");
        if (files == null) return UIAction.BACK_TO_MENU;

        while (true){
            int i = 0;
            for (String fil: files){ //list
                Pattern pattern = Pattern.compile(user.email.replace(".", "\\."));
                Matcher matcher = pattern.matcher(fil);
                if (matcher.find()){
                    System.out.println(i + " - " + fil);
                    i++;
                }
            }

            System.out.println("Digite o numero do item desejado ou q para voltar: ");
            String input = scanner.nextLine();
            if (input.equals("q")) return UIAction.BACK_TO_MENU;
            int index = Integer.parseInt(input);
        
            String[] info = files[index].split(" ");
            String codeName = info[0];
            String secretName = info[1];
            String userEmail = info[2];
            String group = info[3];

            if (!userEmail.equals(user.email)){
                System.out.println("Sem acesso!");
                continue;
            }
            byte[] result = readVault(user, folderPath, codeName, secret);
            System.out.println("Arquivo secreto lido!");
            try {
                Files.write(Paths.get(secretName), result);
            } catch (Exception e) {
                continue;
            }
        }
    }

    private static byte[] readVault(User user, String folderPath, String filename, String userSecret) 
    throws SQLException, CertificateException {
        KeyChain kc = DBQueries.getKeyChain(user.kid);
        if (kc == null){
            return null;
        }
        
        byte[] file;
        try {
            PrivateKey pk = PrivateKeyManager.getPrivateKey(kc, userSecret);
            file = decryptFile(pk, kc.crt, 
                folderPath + "/" + filename + ".enc", 
                folderPath + "/" + filename + ".env",
                folderPath + "/" + filename + ".asd");

            if (file == null) return null;
            return file;
        } catch (Exception e) {
            System.out.println("Impossível acessar pasta");
            return null;
        }
    }

    public static byte[] decryptFile(PrivateKey privateKey, X509Certificate cert, String encodedPath, 
    String envelopePath, String signaturePath)
    throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, 
    InvalidKeyException, IllegalBlockSizeException, BadPaddingException, SignatureException {
		byte[] encodedFile = Files.readAllBytes(Paths.get(encodedPath));
		byte[] envelopeFile = Files.readAllBytes(Paths.get(envelopePath));
		byte[] signatureFile = Files.readAllBytes(Paths.get(signaturePath));
		
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] seedBytes = cipher.doFinal(envelopeFile);
		
		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
		secureRandom.setSeed(seedBytes);
		
		KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
		keyGenerator.init(56, secureRandom);
		
		cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, keyGenerator.generateKey());
		byte[] decodedFile = cipher.doFinal(encodedFile);
		
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initVerify(cert.getPublicKey());
		
		signature.update(decodedFile);
		if(!signature.verify(signatureFile)) {
			System.out.println("Falha na assinatura!");
            return null;
		}
		return decodedFile;
	}
}
