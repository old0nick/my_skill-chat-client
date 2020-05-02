package com.example.mychat;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private static final String pass = "skillbox";

    private static SecretKeySpec keySpec;

    static {
        MessageDigest shaDigest = null;
        try {
            shaDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = pass.getBytes();
            shaDigest.update(bytes, 0, bytes.length);
            byte[] key = shaDigest.digest();
            keySpec = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String rawText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(rawText.getBytes());
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    public static String decrypt(String cipheredText) throws Exception{
        byte[] cyphered = Base64.decode(cipheredText, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] rawText = cipher.doFinal(cyphered);
        return new String(rawText, "UTF-8");
    }
}
