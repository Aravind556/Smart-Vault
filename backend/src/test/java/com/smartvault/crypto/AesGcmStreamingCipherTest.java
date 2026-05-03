package com.smartvault.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;

public class AesGcmStreamingCipherTest {

  @Test
  void encryptsAndDecryptsRoundTrip() throws Exception {
    byte[] key = new byte[32];
    byte[] data = "hello smart vault".getBytes();

    AesGcmStreamingCipher cipher = new AesGcmStreamingCipher();
    var enc = cipher.encrypt(new ByteArrayInputStream(data), key);

    byte[] ciphertext = enc.stream().readAllBytes();
    byte[] plaintext = cipher.decrypt(new ByteArrayInputStream(ciphertext), key, enc.iv()).readAllBytes();

    assertThat(plaintext).isEqualTo(data);
  }
}
