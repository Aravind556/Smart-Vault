package com.smartvault.crypto;

public record DataKey(byte[] plaintextKey, byte[] encryptedKey, byte[] encryptedKeyIv) {
  public static DataKey kms(byte[] plaintextKey, byte[] encryptedKey) {
    return new DataKey(plaintextKey, encryptedKey, null);
  }

  public static DataKey local(byte[] plaintextKey, byte[] encryptedKey, byte[] encryptedKeyIv) {
    return new DataKey(plaintextKey, encryptedKey, encryptedKeyIv);
  }
}
