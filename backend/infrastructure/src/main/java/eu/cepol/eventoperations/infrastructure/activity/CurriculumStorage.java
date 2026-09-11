package eu.cepol.eventoperations.infrastructure.activity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Local synthetic evidence store. Production must replace this port with private object storage and scanning. */
@Component
public class CurriculumStorage {
  private final Path root;

  public CurriculumStorage(@Value("${app.curricula.root:.local/curricula}") String root) {
    this.root = Path.of(root);
  }

  public StoredFile store(byte[] bytes) throws IOException {
    if (bytes == null || bytes.length == 0 || bytes.length > 20_000_000) throw new IllegalArgumentException("Curriculum file is empty or exceeds 20 MB");
    Files.createDirectories(root);
    String key = UUID.randomUUID() + ".bin";
    Files.write(root.resolve(key), bytes);
    try { var digest = MessageDigest.getInstance("SHA-256"); return new StoredFile(key, HexFormat.of().formatHex(digest.digest(bytes))); }
    catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 unavailable", e); }
  }
  public record StoredFile(String objectKey, String sha256) { }
}
