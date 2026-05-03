package com.smartvault.api;

import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smartvault.service.VaultService;

@RestController
@RequestMapping("/api/files")
public class FileController {
  private final VaultService vault;

  public FileController(VaultService vault) {
    this.vault = vault;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
    var res = vault.upload(file);
    return Map.of(
        "id", res.id(),
        "filename", res.filename(),
        "size", res.size(),
        "keyProvider", res.keyProvider());
  }

  @GetMapping("/{id}")
  public ResponseEntity<InputStreamResource> download(@PathVariable("id") String id) {
    var res = vault.download(id);

    ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(res.filename()))
        .contentType(MediaType.parseMediaType(res.contentType()));

    if (res.size() >= 0) {
      builder = builder.contentLength(res.size());
    }

    return builder.body(new InputStreamResource(res.content()));
  }

  private static String contentDisposition(String filename) {
    String safe = filename == null || filename.isBlank() ? "file" : filename.replace("\"", "");
    return "attachment; filename=\"" + safe + "\"";
  }
}
