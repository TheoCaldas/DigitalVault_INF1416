package DigitalVault_INF1416.main;

public class TempUser {

    public enum Group {
        ADM,
        USR
    }

    String crtPath;
    String privateKeyPath;
    String secret;
    Group group;
    String password;

    public TempUser(String certDig, String privateKey, String secret, Group group, String password) {
        this.crtPath = certDig;
        this.privateKeyPath = privateKey;
        this.secret = secret;
        this.group = group;
        this.password = password;
    }

    public void printUser() {
        System.out.println(String.format("Certificado Digital: %s, Chave Privada: %s, Frase Secreta: %s, Grupo: %s, Senha: %s", this.crtPath, this.privateKeyPath, this.secret, this.group, this.password));
    }
}
