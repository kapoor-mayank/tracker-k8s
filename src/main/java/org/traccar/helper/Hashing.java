package org.traccar.helper;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public final class Hashing {
    public static final int ITERATIONS = 1000;
    public static final int SALT_SIZE = 24;
    public static final int HASH_SIZE = 24;
    private static SecretKeyFactory factory;

    static {
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static class HashingResult {
        private final String hash;
        private final String salt;

        public HashingResult(String hash, String salt) {
            this.hash = hash;
            this.salt = salt;
        }

        public String getHash() {
            return this.hash;
        }

        public String getSalt() {
            return this.salt;
        }
    }


    private static byte[] function(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, 1000, 192);
            return factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    public static HashingResult createHash(String password) {
        byte[] salt = new byte[24];
        RANDOM.nextBytes(salt);
        byte[] hash = function(password.toCharArray(), salt);
        return new HashingResult(
                DataConverter.printHex(hash),
                DataConverter.printHex(salt));
    }

    public static boolean validatePassword(String password, String hashHex, String saltHex) {
        byte[] hash = DataConverter.parseHex(hashHex);
        byte[] salt = DataConverter.parseHex(saltHex);
        return slowEquals(hash, function(password.toCharArray(), salt));
    }


    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return (diff == 0);
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\Hashing.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */