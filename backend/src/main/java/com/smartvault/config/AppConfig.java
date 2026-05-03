package com.smartvault.config;

import java.net.URI;
import java.util.Optional;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.KmsClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
@EnableConfigurationProperties(SmartVaultProperties.class)
public class AppConfig {

  @Bean
  public AwsCredentialsProvider awsCredentialsProvider(SmartVaultProperties props) {
    if (StringUtils.hasText(props.getS3().getAccessKey()) && StringUtils.hasText(props.getS3().getSecretKey())) {
      return StaticCredentialsProvider.create(
          AwsBasicCredentials.create(props.getS3().getAccessKey(), props.getS3().getSecretKey()));
    }
    return DefaultCredentialsProvider.create();
  }

  @Bean
  public S3Client s3Client(SmartVaultProperties props, AwsCredentialsProvider creds) {
    S3ClientBuilder builder = S3Client.builder()
        .credentialsProvider(creds)
        .region(Region.of(props.getS3().getRegion()))
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(props.getS3().isForcePathStyle()).build());

    if (StringUtils.hasText(props.getS3().getEndpoint())) {
      builder = builder.endpointOverride(URI.create(props.getS3().getEndpoint()));
    }

    return builder.build();
  }

  @Bean
  public KmsClient kmsClient(SmartVaultProperties props, AwsCredentialsProvider creds) {
    KmsClientBuilder builder = KmsClient.builder()
        .credentialsProvider(creds)
        .region(Region.of(Optional.ofNullable(props.getKms().getRegion()).orElse(props.getS3().getRegion())));

    if (StringUtils.hasText(props.getKms().getEndpoint())) {
      builder = builder.endpointOverride(URI.create(props.getKms().getEndpoint()));
    }

    return builder.build();
  }
}
