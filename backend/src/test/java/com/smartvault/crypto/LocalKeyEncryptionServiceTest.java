package com.smartvault.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.smartvault.config.SmartVaultProperties;

public class LocalKeyEncryptionServiceTest {

  @Test
  void wrapsAndUnwrapsDataKey() {
    SmartVaultProperties props = new SmartVaultProperties();
    props.getLocal().setMasterKeyBase64(Base64.getEncoder().encodeToString(new byte[32]));

    LocalKeyEncryptionService svc = new LocalKeyEncryptionService(props);
    DataKey dk = svc.generateDataKey();

    byte[] decrypted = svc.decryptDataKey(dk.encryptedKey(), dk.encryptedKeyIv());
    assertThat(decrypted).isEqualTo(dk.plaintextKey());
  }
}
