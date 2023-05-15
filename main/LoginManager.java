/* INF1416 2023.1 - Trabalho 4
 * Theo Caldas - 1911078    
 * Matheus Kulick - 1911090
 */
package DigitalVault_INF1416.main;

import DigitalVault_INF1416.db.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
// import org.bouncycastle.util.encoders.Base64;

public class LoginManager {
    protected final static long BLOCKED_WAIT = 120000; //blocked waiting time in miliseconds
    protected final static String TEMP_FILENAME = "token.txt";

    protected Scanner scanner;
    protected long elapsedMiliseconds;
    protected User currentUser;
    protected String currentPassword;

    private static LoginManager instance = null;

    private final static boolean DEBUG_MODE = true;

    protected LoginManager() {}

    public static LoginManager getInstance() {
        if (instance == null) {
            instance = new LoginManager();
        }
        return instance;
    }

    public User login(){
        scanner = new Scanner(System.in);

        if (DEBUG_MODE){
            firstStep();
            return currentUser;
        }

        int error2Count = 0; //count of failed logins on step 2
        int error3Count = 0; //count of failed logins on step 3
        boolean step1Check = false; //has passed on step 1
        boolean step2Check = false; //has passed on step 2

        while (true){
            printHeader();
            String option = scanner.nextLine();

            if (option.equals("2")) return null;

            if (step1Check || (step1Check = firstStep())){
                if (step2Check || (step2Check = secondStep())){
                    if (thirdStep()){
                        System.out.println("Login realizado!");
                        break;
                    }
                    else
                        error3Count++;
                }else
                    error2Count++;
            }

            if (error2Count >= 3){ 
                if (!blockUser(currentUser)) //bloqueia usuario
                    System.err.println("Falha ao bloquear usuário!");
                else
                    System.err.println("3 falhas consecutivas. Usuário bloqueado!");
                error2Count = 0;
                step1Check = false;
            }

            if (error3Count >= 3){ 
                if (!blockUser(currentUser)) //bloqueia usuario
                    System.err.println("Falha ao bloquear usuário!");
                else
                    System.err.println("3 falhas consecutivas. Usuário bloqueado!");
                error3Count = 0;
                error2Count = 0;
                step1Check = false;
                step2Check = false;
            }

            System.out.println("\n\nLogin não realizado. Tente novamente.\n");
        }
        User loggedUser = currentUser;
        currentUser = null;
        return loggedUser;
    }

    protected void printHeader(){
        System.out.println("======TELA DE LOGIN======");
        System.out.print("Digite 1 para iniciar login - Digite 2 para voltar: ");
    }

    public boolean firstStep(){
        System.out.print("Email cadastrado: ");
        // String email = "admin@inf1416.puc-rio.br";
        String email = scanner.nextLine();

        User user = null;
        try {
            user = DBQueries.selectUser(email);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Erro ao buscar por email!");
        }
        if (user == null){
            System.err.println("Email não cadastrado!");
            return false;
        }

        if (isUserBlocked(user)){
            long waitSeconds = (BLOCKED_WAIT - elapsedMiliseconds) / 1000;
            System.err.println("Usuário está bloqueado! Tente novamente em " + waitSeconds + " segundos.");
            return false;
        }

        currentUser = user;
        return true;
    }

    protected boolean secondStep() {
        String userHash = currentUser.hash;
        if (!validatePassword(userHash)){
            System.err.println("Senha Inválida!");
            return false;
        }

        //escrever arquivo token
        try {
            writeTempFile(currentUser.hash, currentUser.token);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Falha ao escrever token.txt");
            return false;
        }
        
        return true;
    }

    protected boolean validatePassword(String userHash){
        // String[] passwords = {"12341234"};
        String[] passwords = PasswordManager.passwordInput();

        for (int i = 0; i < passwords.length; i++) {
            char[] password = passwords[i].toCharArray();
            
            if (OpenBSDBCrypt.checkPassword(userHash, password)) {
                currentPassword = passwords[i];
                return true;
            } 
        }
        return false;
    }

    public boolean thirdStep(){
        System.out.print("Entre com o token gerado pelo aplicativo iToken: ");
        String token = scanner.next();

        String decryptedUserToken;
        try {
            decryptedUserToken = decryptToken(currentUser.token, currentPassword);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Falha ao decriptar semente!");
            return false;
        } 

        String[] possibleTokens = new String[3];
        try {
            possibleTokens[0] = generateFinalToken(decryptedUserToken, 0);
            possibleTokens[1] = generateFinalToken(decryptedUserToken, 1);
            possibleTokens[2] = generateFinalToken(decryptedUserToken, -1);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Falha ao gerar tokens!");
            return false;
        }

        for (String string : possibleTokens) {
            if (string.equals(token))
                return true;
        }

        System.out.println("Token inválido!");
        return false;
        // return true;
    }

    private boolean isUserBlocked(User user){
        if (user.blocked == null)
            return false;
        Date nowDate = new Date();
        Date blockedDate = user.blocked;
        elapsedMiliseconds = (nowDate.getTime() - blockedDate.getTime());
        if (elapsedMiliseconds > BLOCKED_WAIT)
            return false;
        return true;
    }

    private boolean blockUser(User user) {
        Date nowDate = new Date();
        try {
            return DBQueries.blockUser(user, nowDate);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    private void writeTempFile(String hash, String token) throws IOException{
        FileWriter writer = new FileWriter(TEMP_FILENAME);
        writer.write(hash + "\n");
        writer.write(token);
        writer.close();
    }

    protected String decryptToken(String token, String password) 
    throws NoSuchAlgorithmException, NoSuchPaddingException, 
    InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        byte[] decodedToken = Base64.getDecoder().decode(token);

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(password.getBytes());
        KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
        keyGenerator.init(56, random);
		SecretKey secretKey = keyGenerator.generateKey();

        //Decrypt token with key
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedToken = cipher.doFinal(decodedToken);

        return getHex(decryptedToken);
    }

    static public String getHex(byte[] data){
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(0x0100 + (data[i] & 0x00FF)).substring(1);
            buf.append((hex.length() < 2 ? "0" : "") + hex);
        }
        return buf.toString();
    }

    protected String generateFinalToken(String token, int deltaMinutes) throws NoSuchAlgorithmException{
        final long miliToMinutes = 60000;
        long delay = deltaMinutes * miliToMinutes;
        long currentTimeMinutes = (System.currentTimeMillis() / miliToMinutes) * miliToMinutes;
        long time = currentTimeMinutes + delay;
        String seed = token + time;

        byte[] finalToken = new byte[4];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(seed.getBytes());
        random.nextBytes(finalToken);

        int finalInt = Math.abs(ByteBuffer.wrap(finalToken).getInt());
        return String.format("%06d", finalInt);
    }
}
