/* INF1416 2023.1 - Trabalho 4
 * Theo Caldas - 1911078    
 * Matheus Kulick - 1911090
 */
package DigitalVault_INF1416.db;

import java.security.cert.*;
import java.sql.ResultSet;
import java.sql.SQLException;

import DigitalVault_INF1416.main.CertificateManager;

public class KeyChain {
    public int kid;
    public X509Certificate crt;
    public String privateKey;

    public KeyChain(ResultSet rs) throws SQLException, CertificateException {
        int kid = rs.getInt("kid");
        String crt = rs.getString("crt");
        String privateKey = rs.getString("privatekey");

        this.kid = kid;
        this.crt = CertificateManager.stringToCertificate(crt);
        this.privateKey = privateKey;
    }
}


