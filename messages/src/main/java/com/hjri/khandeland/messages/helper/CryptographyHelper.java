package com.hjri.khandeland.messages.helper;

import android.support.annotation.Nullable;

import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Amin Hajari on 11/4/2016.
 */

public final class CryptographyHelper {
    private final static String IV = "IV";
    private final static String CIPHER_TEXT = "CIPHERTEXT";
    private final static String SECRET_KEY = "ReM3mBErMe4M1n3dGE";
    private static byte[] salt = new byte[]{ 57, 85, 25, 56, 96, 100, 15, 71, 84, 67, 96, 10, 24, 111, 112, 69, 3 };

    private static SecretKeySpec secret;
    public static SecretKeySpec getSecretKey() throws Exception{
        SecretKeySpec s = null;
        try {
            s = generateKey(SECRET_KEY.toCharArray(), salt);
        }catch (Exception e){}
        return s;
    }

    public CryptographyHelper() throws Exception{
        secret = generateKey(SECRET_KEY.toCharArray(), salt);
    }

    public static SecretKeySpec generateKey(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password, salt, 1024, 128);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        return secret;
    }

    public static Map encrypt(String cleartext, @Nullable byte[] iv, SecretKeySpec secret) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // If the IvParameterSpec argument is omitted (null), a new IV will be created
        cipher.init(Cipher.ENCRYPT_MODE, secret,
                iv == null ? null : new IvParameterSpec(iv));
        AlgorithmParameters params = cipher.getParameters();
        byte[] usedIV = params.getParameterSpec(IvParameterSpec.class).getIV();
        byte[] cipherText = cipher.doFinal(cleartext.getBytes("UTF-8"));
        Map result = new HashMap();
        result.put(IV, usedIV);
        result.put(CIPHER_TEXT, cipherText);
        return result;
    }

    public static String decrypt(byte[] ciphertext, byte[] iv, SecretKeySpec secret) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        String plaintext = new String(cipher.doFinal(ciphertext), "UTF-8");
        return plaintext;
    }

    /** Encryption Example
     *  Map result = encrypt(message, null, secret);
     *  To Get IV --> (byte[])result.get(IV)
     *  To Get Cipher --> (byte[])result.get(CIPHER_TEXT)
     *
     *  Decryption Example
     *  String decryptedText = decrypt(Base64.decode(cipher), Base64.decode(iv), secret); */
}
