package com.smartvault.service;

import java.io.InputStream;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smartvault.crypto.AesGcmStreamingCipher;
import com.smartvault.crypto.KeyEncryptionService;
import com.smartvault.storage.ObjectStorage;
import com.smartvault.storage.ObjectStorage.StoredObject;

@Service
public class VaultService {
  private static final String META_FILE_IV = "file-iv";
  private static final String META_ENCRYPTED_KEY = "encrypted-key";
  private static final String META_ENCRYPTED_KEY_IV = "encrypted-key-iv";
  private static final String META_ORIGINAL_FILENAME = "original-filename";
  private static final String META_ORIGINAL_CONTENT_TYPE = "original-content-type";
  private static final String META_ORIGINAL_SIZE = "original-size";
  private static final String META_KEY_PROVIDER = "key-provider";

  private final ObjectStorage storage;
  private final KeyEncryptionService keyEncryption;
  private final AesGcmStreamingCipher fileCipher = new AesGcmStreamingCipher();

  public VaultService(ObjectStorage storage, KeyEncryptionService keyEncryption) {
    this.storage = storage;
    this.keyEncryption = keyEncryption;
  }

  public UploadResult upload(MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("file is required");
    }

    String objectKey = UUID.randomUUID().toString();

    var dataKey = keyEncryption.generateDataKey();
    AesGcmStreamingCipher.EncryptedStream encrypted = fileCipher.encrypt(inputStream(file), dataKey.plaintextKey());

    long plaintextSize = file.getSize();
    long encryptedSize = plaintextSize + AesGcmStreamingCipher.GCM_TAG_BYTES;

    Map<String, String> metadata = new LinkedHashMap<>();
    metadata.put(META_FILE_IV, Base64.getEncoder().encodeToString(encrypted.iv()));
    metadata.put(META_ENCRYPTED_KEY, Base64.getEncoder().encodeToString(dataKey.encryptedKey()));
    if (dataKey.encryptedKeyIv() != null) {
      metadata.put(META_ENCRYPTED_KEY_IV, Base64.getEncoder().encodeToString(dataKey.encryptedKeyIv()));
    }
    metadata.put(META_ORIGINAL_FILENAME, safe(file.getOriginalFilename()));
    metadata.put(META_ORIGINAL_CONTENT_TYPE, safe(file.getContentType()));
    metadata.put(META_ORIGINAL_SIZE, Long.toString(plaintextSize));
    metadata.put(META_KEY_PROVIDER, keyEncryption.providerName());

    storage.put(objectKey, encrypted.stream(), encryptedSize, metadata);

    return new UploadResult(objectKey, file.getOriginalFilename(), plaintextSize, keyEncryption.providerName());
  }

  public DownloadResult download(String objectKey) {
    StoredObject obj = storage.get(objectKey);

    String fileIvB64 = required(obj.metadata(), META_FILE_IV);
    String encryptedKeyB64 = required(obj.metadata(), META_ENCRYPTED_KEY);
    String encryptedKeyIvB64 = obj.metadata().get(META_ENCRYPTED_KEY_IV);

    byte[] fileIv = Base64.getDecoder().decode(fileIvB64);
    byte[] encryptedKey = Base64.getDecoder().decode(encryptedKeyB64);
    byte[] encryptedKeyIv = encryptedKeyIvB64 == null ? null : Base64.getDecoder().decode(encryptedKeyIvB64);

    byte[] dataKey = keyEncryption.decryptDataKey(encryptedKey, encryptedKeyIv);
    InputStream plaintext = fileCipher.decrypt(obj.content(), dataKey, fileIv);

    String filename = obj.metadata().getOrDefault(META_ORIGINAL_FILENAME, objectKey);
    String contentType = obj.metadata().getOrDefault(META_ORIGINAL_CONTENT_TYPE, "application/octet-stream");
    long size = parseLong(obj.metadata().get(META_ORIGINAL_SIZE), -1);

    return new DownloadResult(filename, contentType, size, plaintext);
  }

  private static InputStream inputStream(MultipartFile file) {
    try {
      return file.getInputStream();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to read uploaded file", e);
    }
  }

  private static String required(Map<String, String> meta, String key) {
    String val = meta.get(key);
    if (val == null || val.isBlank()) {
      throw new IllegalStateException("Missing required object metadata: " + key);
    }
    return val;
  }

  private static String safe(String v) {
    return v == null ? "" : v;
  }

  private static long parseLong(String v, long fallback) {
    try {
      if (v == null) {
        return fallback;
      }
      return Long.parseLong(v);
    } catch (Exception e) {
      return fallback;
    }
  }

  public record UploadResult(String id, String filename, long size, String keyProvider) {}

  public record DownloadResult(String filename, String contentType, long size, InputStream content) {}
}
