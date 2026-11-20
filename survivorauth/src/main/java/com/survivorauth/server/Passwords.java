package com.survivorauth.server;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hash de senhas com PBKDF2-HMAC-SHA256 e sal aleatório por conta. */
public final class Passwords {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static AccountStore.Account create(String password, int iterations) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, iterations);
        return new AccountStore.Account(
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(hash),
                iterations,
                System.currentTimeMillis());
    }

    public static boolean verify(String password, AccountStore.Account account) {
        byte[] salt = Base64.getDecoder().decode(account.salt());
        byte[] expected = Base64.getDecoder().decode(account.hash());
        byte[] actual = pbkdf2(password, salt, account.iterations());
        return MessageDigest.isEqual(expected, actual); // comparação em tempo constante
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("PBKDF2 indisponível", e);
        }
    }

    private Passwords() {}
}
