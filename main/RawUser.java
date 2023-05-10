package DigitalVault_INF1416.main;

public class RawUser {

    public enum Group {
        ADM,
        USR
    }

    String certDig;
    String privateKey;
    String secret;
    Group group;
    String password;

    public RawUser(String certDig, String privateKey, String secret, Group group, String password) {
        this.certDig = certDig;
        this.privateKey = privateKey;
        this.secret = secret;
        this.group = group;
        this.password = password;
    }

    public void printUser() {
        System.out.println(String.format("Certificado Digital: %s, Chave Privada: %s, Frase Secreta: %s, Grupo: %s, Senha: %s, Confirmação de senha: %s", this.certDig, this.privateKey, this.secret, this.group, this.password));
    }
}
