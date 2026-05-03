package com.smartvault.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smartvault")
public class SmartVaultProperties {
  private String keyProvider = "local";
  private final S3 s3 = new S3();
  private final Kms kms = new Kms();
  private final Local local = new Local();

  public String getKeyProvider() {
    return keyProvider;
  }

  public void setKeyProvider(String keyProvider) {
    this.keyProvider = keyProvider;
  }

  public S3 getS3() {
    return s3;
  }

  public Kms getKms() {
    return kms;
  }

  public Local getLocal() {
    return local;
  }

  public static class S3 {
    private String bucket;
    private String region;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private boolean forcePathStyle = true;

    public String getBucket() {
      return bucket;
    }

    public void setBucket(String bucket) {
      this.bucket = bucket;
    }

    public String getRegion() {
      return region;
    }

    public void setRegion(String region) {
      this.region = region;
    }

    public String getEndpoint() {
      return endpoint;
    }

    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }

    public String getAccessKey() {
      return accessKey;
    }

    public void setAccessKey(String accessKey) {
      this.accessKey = accessKey;
    }

    public String getSecretKey() {
      return secretKey;
    }

    public void setSecretKey(String secretKey) {
      this.secretKey = secretKey;
    }

    public boolean isForcePathStyle() {
      return forcePathStyle;
    }

    public void setForcePathStyle(boolean forcePathStyle) {
      this.forcePathStyle = forcePathStyle;
    }
  }

  public static class Kms {
    private String region;
    private String keyId;
    private String endpoint;

    public String getRegion() {
      return region;
    }

    public void setRegion(String region) {
      this.region = region;
    }

    public String getKeyId() {
      return keyId;
    }

    public void setKeyId(String keyId) {
      this.keyId = keyId;
    }

    public String getEndpoint() {
      return endpoint;
    }

    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }
  }

  public static class Local {
    private String masterKeyBase64;

    public String getMasterKeyBase64() {
      return masterKeyBase64;
    }

    public void setMasterKeyBase64(String masterKeyBase64) {
      this.masterKeyBase64 = masterKeyBase64;
    }
  }
}
