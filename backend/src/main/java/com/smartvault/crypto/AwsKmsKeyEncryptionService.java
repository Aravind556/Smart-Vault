package com.smartvault.crypto;

import java.nio.ByteBuffer;

import org.springframework.util.StringUtils;

import com.smartvault.config.SmartVaultProperties;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DataKeySpec;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyRequest;

public class AwsKmsKeyEncryptionService implements KeyEncryptionService {
  private final KmsClient kms;
  private final String keyId;

  public AwsKmsKeyEncryptionService(KmsClient kms, SmartVaultProperties props) {
    this.kms = kms;
    this.keyId = props.getKms().getKeyId();

    if (!StringUtils.hasText(this.keyId)) {
      throw new IllegalStateException("smartvault.kms.key-id is required when key-provider=aws-kms");
    }
  }

  @Override
  public DataKey generateDataKey() {
    var resp = kms.generateDataKey(GenerateDataKeyRequest.builder()
        .keyId(keyId)
        .keySpec(DataKeySpec.AES_256)
        .build());

    byte[] plaintext = toBytes(resp.plaintext().asByteBuffer());
    byte[] encrypted = toBytes(resp.ciphertextBlob().asByteBuffer());
    return DataKey.kms(plaintext, encrypted);
  }

  @Override
  public byte[] decryptDataKey(byte[] encryptedKey, byte[] encryptedKeyIv) {
    // encryptedKeyIv is unused for KMS
    var resp = kms.decrypt(DecryptRequest.builder()
        .ciphertextBlob(SdkBytes.fromByteArray(encryptedKey))
        .build());
    return toBytes(resp.plaintext().asByteBuffer());
  }

  @Override
  public String providerName() {
    return "aws-kms";
  }

  private static byte[] toBytes(ByteBuffer buf) {
    ByteBuffer dup = buf.duplicate();
    byte[] out = new byte[dup.remaining()];
    dup.get(out);
    return out;
  }
}
