package DigitalVault_INF1416.main;

import java.security.cert.CertificateException;
import java.sql.SQLException;

import DigitalVault_INF1416.db.User;
import DigitalVault_INF1416.main.UIManager.UIAction;

public class VaultManager {
    public static UIAction readVault(User user) throws SQLException, CertificateException {
        String[] inputs = UIManager.readVaultFlow(user);
        String folderPath = inputs[0];
        String secret = inputs[1];


        
        return UIAction.BACK_TO_MENU;
    }
}
