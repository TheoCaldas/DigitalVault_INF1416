package DigitalVault_INF1416.main;

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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import DigitalVault_INF1416.db.*;
import DigitalVault_INF1416.main.UIManager.UIAction;

public class VaultManager {

    public static UIAction readVault(User user) throws SQLException, CertificateException {
        // String[] inputs = UIManager.readVaultFlow(user);
        // String folderPath = inputs[0];
        // String secret = inputs[1];

        String folderPath = "/Users/theo/Desktop/PUC/seg info/trab4/DigitalVault_INF1416/cofredigital";
        String secret = "";

        KeyChain kc = DBQueries.getKeyChain(user.kid);
        if (kc == null){
            return UIAction.STOP_PROGRAM;
        }
        
        String filename = "/index";
        byte[] file;
        try {
            file = decryptFile(kc.privateKey, kc.crt, 
                folderPath + filename + ".enc", 
                folderPath + filename + ".env",
                folderPath + filename + ".asd");

            String indexString = new String(file);
            String[] files = indexString.split("\n");
            for(String f : files) {
                System.out.println(f);
                // String[] fileInfo = f.split(" ");
                // secretFiles.add(new SecretFile(fileInfo[0], fileInfo[1], fileInfo[2], fileInfo[3]));
            }
            // Files.write(Paths.get(folderPath + File.separator + currentSecretFileName), file);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.out.println("Impossível acessar pasta");
        }
        
        return UIAction.BACK_TO_MENU;
    }

    // public void salefjjksef(){
    //     X509Certificate cert;	
    //     byte[] file;
    //     try {
    //         cert = CryptoFactory.getCertificateFromBase64(User.getUser().getCertificado());
    //         file = CryptoFactory.decryptFile(User.getUser().getPrivateKey(),
    //                 cert, 
    //                 path + File.separator + secretFile.getCodeName() + ".enc", 
    //                 path + File.separator + secretFile.getCodeName() + ".env", 
    //                 path + File.separator + secretFile.getCodeName()  + ".asd", false);
    //         Files.write(Paths.get(path + File.separator + currentSecretFileName), file);
    //     }
    // }

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
		}
		return decodedFile;
	}
}
