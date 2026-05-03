package com.smartvault.crypto;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.util.StringUtils;

import com.smartvault.config.SmartVaultProperties;

public class LocalKeyEncryptionService implements KeyEncryptionService {
  private static final int DATA_KEY_BYTES = 32;
  private static final int GCM_IV_BYTES = 12;
  private static final int GCM_TAG_BITS = 128;

  private final SecureRandom random = new SecureRandom();
  private final byte[] masterKey;

  public LocalKeyEncryptionService(SmartVaultProperties props) {
    String b64 = props.getLocal().getMasterKeyBase64();
    if (!StringUtils.hasText(b64)) {
      throw new IllegalStateException(
          "smartvault.local.master-key-base64 is required when key-provider=local (32 bytes Base64)"
      );
    }
    this.masterKey = Base64.getDecoder().decode(b64);
    if (this.masterKey.length != 32) {
      throw new IllegalStateException("Local master key must be exactly 32 bytes (AES-256)");
    }
  }

  @Override
  public DataKey generateDataKey() {
    byte[] dataKey = new byte[DATA_KEY_BYTES];
    random.nextBytes(dataKey);

    byte[] iv = new byte[GCM_IV_BYTES];
    random.nextBytes(iv);

    byte[] encrypted = encryptWithMasterKey(dataKey, iv);
    return DataKey.local(dataKey, encrypted, iv);
  }

  @Override
  public byte[] decryptDataKey(byte[] encryptedKey, byte[] encryptedKeyIv) {
    if (encryptedKeyIv == null || encryptedKeyIv.length != GCM_IV_BYTES) {
      throw new IllegalArgumentException("encryptedKeyIv must be 12 bytes for local provider");
    }
    return decryptWithMasterKey(encryptedKey, encryptedKeyIv);
  }

  @Override
  public String providerName() {
    return "local";
  }

  private byte[] encryptWithMasterKey(byte[] plaintext, byte[] iv) {
    try {
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(masterKey, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
      return cipher.doFinal(plaintext);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to wrap data key", e);
    }
  }

  private byte[] decryptWithMasterKey(byte[] ciphertext, byte[] iv) {
    try {
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(masterKey, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
      return cipher.doFinal(ciphertext);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to unwrap data key", e);
    }
  }
}
