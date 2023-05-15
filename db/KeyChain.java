/* INF1416 2023.1 - Trabalho 4
 * Theo Caldas - 1911078    
 * Matheus Kulick - 1911090
 */
package DigitalVault_INF1416.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bouncycastle.util.encoders.Base64;

import DigitalVault_INF1416.main.CertificateManager;

public class KeyChain {
    public int kid;
    public X509Certificate crt;
    public PrivateKey privateKey;

    public KeyChain(ResultSet rs) throws SQLException, CertificateException {
        int kid = rs.getInt("kid");
        String crt = rs.getString("crt");
        String privateKey = rs.getString("privatekey");

        this.kid = kid;
        this.crt = CertificateManager.stringToCertificate(crt);
        this.privateKey = recoverPK(privateKey);
    }

    private PrivateKey recoverPK(String pk){
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(hexStringToByteArray(pk));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privKey = kf.generatePrivate(keySpec);
            return privKey;
        } catch (Exception e) {
            return null;
        }
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
}


