package com.smartvault.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.smartvault.crypto.AwsKmsKeyEncryptionService;
import com.smartvault.crypto.KeyEncryptionService;
import com.smartvault.crypto.LocalKeyEncryptionService;
import com.smartvault.storage.ObjectStorage;
import com.smartvault.storage.S3ObjectStorage;

import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class ServiceWiringConfig {

  @Bean
  public ObjectStorage objectStorage(S3Client s3, SmartVaultProperties props) {
    return new S3ObjectStorage(s3, props);
  }

  @Bean
  public KeyEncryptionService keyEncryptionService(SmartVaultProperties props, KmsClient kmsClient) {
    String provider = props.getKeyProvider();
    if (provider == null || provider.isBlank() || provider.equalsIgnoreCase("local")) {
      return new LocalKeyEncryptionService(props);
    }
    if (provider.equalsIgnoreCase("aws-kms") || provider.equalsIgnoreCase("kms")) {
      return new AwsKmsKeyEncryptionService(kmsClient, props);
    }

    throw new IllegalStateException("Unknown smartvault.key-provider: " + provider);
  }
}
