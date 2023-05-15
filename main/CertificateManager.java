package DigitalVault_INF1416.main;

import java.security.cert.*;
import java.io.*;
import java.util.Base64;
import java.util.regex.*;

public class CertificateManager {
    public static final String INVALID_EMAIL = "INVALID EMAIL";
    public static final String INVALID_CN = "INVALID COMMON NAME";

    public static X509Certificate getCertificate(String path) throws CertificateException, IOException{
        FileInputStream fis = new FileInputStream(path);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)cf.generateCertificate(fis);
        fis.close();
        return cert;
    }

    public static void printCertificate(X509Certificate cert){
        System.out.println("\nCERTIFICATE INFO");
        System.out.println("Version: " + cert.getVersion());
        System.out.println("Serial number: " + cert.getSerialNumber());
        System.out.println("Validity period: " + cert.getNotBefore() + " - " + cert.getNotAfter());
        System.out.println("Algorithm: " + cert.getSigAlgName());

        String issuer = cert.getIssuerX500Principal().toString();
        String subject = cert.getSubjectX500Principal().toString();

        System.out.println("Issuer (Common Name): " + extractCommonName(issuer));
        System.out.println("Subject (Common Name): " + extractCommonName(subject));
        System.out.println("Login Name (Subject Email): " + extractEmail(subject));
    }

    public static String extractEmail(String subject){
        Pattern pattern = Pattern.compile("EMAILADDRESS=([^,]+)");
        Matcher matcher = pattern.matcher(subject);
        if (matcher.find())
            return matcher.group(1);
        return INVALID_EMAIL;
    }

    public static String extractCommonName(String subject){
        Pattern pattern = Pattern.compile("CN=([^,]+)");
        Matcher matcher = pattern.matcher(subject);
        if (matcher.find())
            return matcher.group(1);
        return INVALID_CN;
    }

    public static String certificateToString(X509Certificate cert) throws CertificateEncodingException {
        byte[] certBytes = cert.getEncoded();
        String certString = Base64.getEncoder().encodeToString(certBytes);
        return certString;
    }

    public static X509Certificate stringToCertificate(String certString) throws CertificateException {
        byte[] certBytes = Base64.getDecoder().decode(certString);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
        return cert;
    }
}
