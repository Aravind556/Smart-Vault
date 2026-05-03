package com.smartvault.crypto;

import java.io.InputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesGcmStreamingCipher {
  public static final int GCM_IV_BYTES = 12;
  public static final int GCM_TAG_BYTES = 16;
  private static final int GCM_TAG_BITS = 128;

  private final SecureRandom random = new SecureRandom();

  public EncryptedStream encrypt(InputStream plaintext, byte[] dataKey) {
    byte[] iv = new byte[GCM_IV_BYTES];
    random.nextBytes(iv);

    Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, dataKey, iv);
    return new EncryptedStream(new CipherInputStream(plaintext, cipher), iv);
  }

  public InputStream decrypt(InputStream ciphertext, byte[] dataKey, byte[] iv) {
    Cipher cipher = initCipher(Cipher.DECRYPT_MODE, dataKey, iv);
    return new CipherInputStream(ciphertext, cipher);
  }

  private Cipher initCipher(int mode, byte[] dataKey, byte[] iv) {
    try {
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(mode, new SecretKeySpec(dataKey, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
      return cipher;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize AES-GCM cipher", e);
    }
  }

  public record EncryptedStream(InputStream stream, byte[] iv) {}
}
