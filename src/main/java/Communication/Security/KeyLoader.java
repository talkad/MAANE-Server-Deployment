package Communication.Security;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;


@Slf4j
public class KeyLoader {

    private final String password;
    private final String filepath;
    private final String encryptedText;

    private static class CreateSafeThreadSingleton {
        private static final KeyLoader INSTANCE = new KeyLoader();
    }

    public KeyLoader(){
        password = "1234";

//        filepath = ".\\src\\main\\resources\\store.keystore"; // other's path
//        filepath = "C:\\MAANE\\maane\\maane\\src\\main\\resources\\store.keystore";
//        filepath = ".\\src\\main\\resources\\store.keystore"; // other's path
//        filepath = "maane\\src\\main\\resources\\store.keystore"; // other's path
//        filepath = "C:\\MAANE\\maane\\maane\\src\\main\\resources\\store.keystore";
//        filepath = "C:\\Users\\User\\Desktop\\UpdatedMaane\\MAANE\\maane\\maane\\src\\main\\resources\\store.keystore";
//        filepath = "maane\\src\\main\\resources\\store.keystore"; // other's path

        filepath = System.getProperty("user.dir") + "\\src\\main\\resources\\store.keystore";

        encryptedText = "354132168465432";
    }

    public static KeyLoader getInstance() {
        return KeyLoader.CreateSafeThreadSingleton.INSTANCE;
    }


    public void storeKey(String key, SecretKey secretKey) {
        try {
            File file = new File(filepath);
            KeyStore keystore = KeyStore.getInstance("JCEKS");

            if (!file.exists()) {
                keystore.load(null, null);
            }

            keystore.setKeyEntry(key, secretKey, password.toCharArray(), null);
            OutputStream writeStream = new FileOutputStream(filepath);
            keystore.store(writeStream, password.toCharArray());

            log.info("key store created");

        } catch(Exception e){
            log.error("storing key failed");
            log.error(e.getMessage());
        }
    }

    public SecretKey readKey(String key){
        try{
            KeyStore keystore = KeyStore.getInstance("JCEKS");
            InputStream readStream = new FileInputStream(filepath);
            keystore.load(readStream, password.toCharArray());

            return (SecretKey) keystore.getKey(key, password.toCharArray());

        }catch(Exception e){
            log.error("key from {} didn't load\n error: {}", filepath, e.getMessage());
            return null;
        }
    }

    private SecretKey generateKey(){
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            return generator.generateKey();

        }catch(Exception e){
            log.error("key generation failed\n error: {}", e.getMessage());
            return null;
        }

    }

    public byte[] encryptKey(String toEncrypt, SecretKey key, Cipher cipher){
        try{
            byte[] text = toEncrypt.getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return cipher.doFinal(text);

        }catch(Exception  e){
            log.error("key encryption failed");
            return null;
        }

    }

    public byte[] decryptKey(byte[] enc, SecretKey key, Cipher cipher) {
        try{
            cipher.init(Cipher.DECRYPT_MODE, key);

            return cipher.doFinal(enc);

        }catch(Exception  e){
            log.error("key decryption failed");
            return null;
        }

    }

    public byte[] getEncryptionKey(String keyStr) {
        SecretKey key = KeyLoader.getInstance().readKey(keyStr);

        try {
            Cipher cipher = Cipher.getInstance("AES");
            // byte[] dec = KeyLoader.getInstance().decryptKey(encryptedData, key, cipher);

            return KeyLoader.getInstance().encryptKey(encryptedText, key, cipher);

        }catch(Exception e){
            System.out.println("failed");
            return null;
        }

    }

    public String getAdminPassword() {
        SecretKey key = KeyLoader.getInstance().readKey("auth_key");
        byte[] encodedPassword = {-123, -26, 68, 117, -59, -44, 119, -15, -27, -10, 79, 57, 73, -78, -75, -120};

        try {
            return new String(KeyLoader.getInstance().decryptKey(encodedPassword, key, Cipher.getInstance("AES")), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return "";
    }

    public String getMailPassword()  {
        SecretKey key = KeyLoader.getInstance().readKey("auth_key");
        byte[] encodedPassword = {52, 25, -2, 44, 120, 30, -111, -40, -113, 122, -128, -22, 19, -47, -18, -110, 34, 80, 34, -79, 38, -62, 66, 14, 63, -17, -112, 60, -9, -126, -76, 56};

        try {
            return new String(KeyLoader.getInstance().decryptKey(encodedPassword, key, Cipher.getInstance("AES")), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return "";
    }




}
