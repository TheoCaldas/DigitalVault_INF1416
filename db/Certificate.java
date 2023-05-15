package DigitalVault_INF1416.db;

import java.security.cert.*;
import java.sql.ResultSet;
import java.sql.SQLException;

import DigitalVault_INF1416.main.CertificateManager;

public class Certificate {
    public int kid;
    public X509Certificate crt;
    public String privateKey;

    public Certificate(ResultSet rs) throws SQLException, CertificateException {
        int kid = rs.getInt("kid");
        String crt = rs.getString("crt");
        String privateKey = rs.getString("privatekey");

        this.kid = kid;
        this.crt = CertificateManager.stringToCertificate(crt);
        this.privateKey = privateKey;
    }
}
