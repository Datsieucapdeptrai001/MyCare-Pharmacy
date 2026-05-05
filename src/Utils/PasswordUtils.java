package Utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Tiện ích băm mật khẩu dùng PBKDF2WithHmacSHA256.
 * Không cần thư viện ngoài — dùng thẳng java.security và javax.crypto có sẵn trong JDK.
 *
 * Định dạng lưu DB: "<salt_base64>:<hash_base64>"
 */
public class PasswordUtils {

    private static final int ITERATIONS  = 65_536;   // số vòng lặp
    private static final int KEY_LENGTH  = 256;       // bit
    private static final int SALT_LENGTH = 16;        // byte
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Băm mật khẩu thuần văn bản → chuỗi "salt:hash" để lưu vào DB.
     */
    public static String hashPassword(String password) {
        byte[] salt = generateSalt();
        byte[] hash = pbkdf2(password.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Xác minh mật khẩu nhập vào có khớp với chuỗi đã lưu trong DB không.
     *
     * @param rawPassword   mật khẩu người dùng vừa nhập (thuần văn bản)
     * @param storedHash    chuỗi "salt:hash" lấy từ DB
     */
    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) return false;

        String[] parts = storedHash.split(":");
        
        // [FIX LỖI 1]: Chặn văng App (Crash) nếu chuỗi trong DB là pass thô (không có dấu :)
        if (parts.length != 2) return false; 

        try {
            byte[] salt         = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            byte[] actualHash   = pbkdf2(rawPassword.toCharArray(), salt);
            // So sánh constant-time để tránh timing attack
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            spec.clearPassword(); // xoá mật khẩu khỏi bộ nhớ ngay sau khi dùng
            return hash;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Lỗi khi băm mật khẩu", e);
        }
    }
}