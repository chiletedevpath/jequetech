package pe.jequetech.service;

import java.security.*;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

final class Claves {
  private Claves() {}

  static String hash(String clave) {
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    // Se guarda la sal junto al resultado para comprobar la contraseña después.
    return Base64.getEncoder().encodeToString(salt)
        + ":"
        + Base64.getEncoder().encodeToString(derivar(clave, salt));
  }

  static boolean coincide(String clave, String hash) {
    String[] parts = hash.split(":");
    return MessageDigest.isEqual(
        Base64.getDecoder().decode(parts[1]), derivar(clave, Base64.getDecoder().decode(parts[0])));
  }

  private static byte[] derivar(String clave, byte[] salt) {
    PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), salt, 120000, 256);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("No se pudo procesar la clave", e);
    } finally {
      spec.clearPassword();
    }
  }
}
