package com.smartvault.storage;

import java.io.InputStream;
import java.util.Map;

public interface ObjectStorage {
  void put(String objectKey, InputStream content, long contentLength, Map<String, String> metadata);

  StoredObject get(String objectKey);

  record StoredObject(InputStream content, long contentLength, Map<String, String> metadata) {}
}
