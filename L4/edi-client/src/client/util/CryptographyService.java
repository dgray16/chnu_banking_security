package client.util;

import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created by a1 on 12.11.16.
 */
public class CryptographyService {

    public static SecretKey lastRC4Key;

    public static byte[] RC4(String plainText, String password) {
        /* Create a binary key from the argument key ( seed ) */
        SecureRandom secureRandom = new SecureRandom(password.getBytes());
        KeyGenerator keyGenerator = null;

        try {
            keyGenerator = KeyGenerator.getInstance("RC4");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        keyGenerator.init(secureRandom);

        lastRC4Key = keyGenerator.generateKey();

        /* Create an instance of cipher */
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RC4");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        /* Initialize the cipher with the key */
        try {
            cipher.init(Cipher.ENCRYPT_MODE, lastRC4Key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        try {
            /*return Arrays.toString(cipher.doFinal(plainText.getBytes()));*/
            return cipher.doFinal(plainText.getBytes());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static String RSA(String plainText, String publicKeyString) {
        String stringToReturn = "";

        if ( publicKeyString !=  null ) {
            try {
                final Cipher cipher = Cipher.getInstance("RSA");

                byte[] publicBytes = Base64.getDecoder().decode(publicKeyString);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey publicKey = keyFactory.generatePublic(keySpec);

                cipher.init(Cipher.ENCRYPT_MODE, publicKey);

                stringToReturn = Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));

            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException | InvalidKeyException
                    | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }
        return stringToReturn;
    }

    public static String generateSecureRandomPassword() {
        String characters = "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM][';/.,+_)(*&^%$#@!";
        return RandomStringUtils.random(10, characters);
    }

}
