package site.bannabe.server.global.utils;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;

public class JasyptEncryptorApp {

  public static void main(String[] args) {
    String value = "fill in encryption necessary value";

    System.out.println("encryptValue :: " + jasyptEncoding(value));
  }

  private static String jasyptEncoding(String value) {
    String key = System.getenv("JASYPT_PASSWORD");
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("JASYPT_PASSWORD is empty");
    }
    StandardPBEStringEncryptor pbeEnc = new StandardPBEStringEncryptor();
    pbeEnc.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
    pbeEnc.setPassword(key);
    pbeEnc.setIvGenerator(new RandomIvGenerator());
    return "ENC(" + pbeEnc.encrypt(value) + ")";
  }

}