package com.moonassist.service;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EncryptionService {

  private static final String ENCRYPTION_ALGORITHM = "AES/ECB/PKCS5Padding";

  public String encrypt(final String text, final Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

    Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    byte[] encrypted = cipher.doFinal(text.getBytes());
    Base64.Encoder encoder = Base64.getEncoder();
    return encoder.encodeToString(encrypted);
  }

  public String decrypt(final String encryptedString, final Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

    Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
    Base64.Decoder decoder = Base64.getDecoder();
    cipher.init(Cipher.DECRYPT_MODE, key);
    return new String(cipher.doFinal(decoder.decode(encryptedString)));
  }

  public String encryptSilent(final String text, final Key key) {

    try {
      return encrypt(text, key);
    } catch (Exception e) {
      log.error("Error encrypting", e);
      throw new IllegalStateException("Storage failure");
    }

  }

  public String decryptSilent(final String text, final Key key) {

    try {
      return decrypt(text, key);
    } catch (Exception e) {
      log.error("Error decrypting", e);
      throw new IllegalStateException("Storage failure", e);
    }

  }

  @SneakyThrows
  public static final SecretKey generate(final String password) {

    byte[] salt = {
        (byte)0xe7, (byte)0x33, (byte)0x03, (byte)0xea,
        (byte)0x1e, (byte)0xf8, (byte)0x01, (byte)0xae
    };

    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
    SecretKey tmp = factory.generateSecret(spec);
    return new SecretKeySpec(tmp.getEncoded(), "AES");
  }

}