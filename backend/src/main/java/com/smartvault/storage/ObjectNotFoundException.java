package com.smartvault.storage;

public class ObjectNotFoundException extends RuntimeException {
  public ObjectNotFoundException(String key) {
    super("Object not found: " + key);
  }
}
