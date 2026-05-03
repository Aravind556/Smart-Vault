package com.smartvault.crypto;

public interface KeyEncryptionService {
  DataKey generateDataKey();

  byte[] decryptDataKey(byte[] encryptedKey, byte[] encryptedKeyIv);

  String providerName();
}
