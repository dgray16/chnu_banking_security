package server.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Created by a1 on 24.11.16.
 *
 * RSA - 1024 bits key.
 */
public class CryptographyService {

    public static KeyPair lastKey;

    public static void generateRSAKeys() {
        try {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            final KeyPair keyPair = keyPairGenerator.generateKeyPair();
            lastKey = keyPair;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
