package com.smartvault.storage;

import java.io.InputStream;
import java.util.Map;

import com.smartvault.config.SmartVaultProperties;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3ObjectStorage implements ObjectStorage {
  private final S3Client s3;
  private final String bucket;

  public S3ObjectStorage(S3Client s3, SmartVaultProperties props) {
    this.s3 = s3;
    this.bucket = props.getS3().getBucket();
  }

  @Override
  public void put(String objectKey, InputStream content, long contentLength, Map<String, String> metadata) {
    s3.putObject(PutObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .metadata(metadata)
            .contentType("application/octet-stream")
            .build(),
        RequestBody.fromInputStream(content, contentLength));
  }

  @Override
  public StoredObject get(String objectKey) {
    try {
      var in = s3.getObject(GetObjectRequest.builder().bucket(bucket).key(objectKey).build());
      var resp = in.response();
      return new StoredObject(in, resp.contentLength(), resp.metadata());
    } catch (NoSuchKeyException e) {
      throw new ObjectNotFoundException(objectKey);
    } catch (S3Exception e) {
      if (e.statusCode() == 404) {
        throw new ObjectNotFoundException(objectKey);
      }
      throw e;
    }
  }
}
