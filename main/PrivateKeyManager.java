package DigitalVault_INF1416.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import DigitalVault_INF1416.db.KeyChain;

public class PrivateKeyManager {
    
    public static PrivateKey getPrivateKey(String path, String secret) throws 
    IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
     IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        //read encrypted private key in path
        FileInputStream fis = new FileInputStream(path);
        byte[] encryptedKeyBytes = new byte[fis.available()];
        fis.read(encryptedKeyBytes);
        fis.close();

        PrivateKey privateKey = decryptPK(secret, encryptedKeyBytes);
        return privateKey;
    }

    public static PrivateKey getPrivateKey(KeyChain kc, String secret) throws 
    IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
     IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        // byte[] encryptedKeyBytes = kc.privateKey.getBytes();
        byte[] encryptedKeyBytes = Base64.getDecoder().decode(kc.privateKey);

        PrivateKey privateKey = decryptPK(secret, encryptedKeyBytes);
        return privateKey;
    }

    public static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] byteArray = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return byteArray;
    }

    public static String getEncryptedPK(String path) throws IOException {
        //read encrypted private key in path
        FileInputStream fis = new FileInputStream(path);
        byte[] encryptedKeyBytes = new byte[fis.available()];
        fis.read(encryptedKeyBytes);
        fis.close();

        String enconded = Base64.getEncoder().encodeToString(encryptedKeyBytes);

        return enconded;//new String(encryptedKeyBytes);
    }

    private static PrivateKey decryptPK(String secret, byte[] encryptedKeyBytes)  throws 
    IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
     IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {

        Security.addProvider(new BouncyCastleProvider());

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
}
