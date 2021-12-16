package org.beifengtz.jvmm.common.util;

import org.beifengtz.jvmm.common.tuple.Pair;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:25 2021/05/11
 *
 * @author beifengtz
 */
public class SignatureUtil {

    /**
     * AES加密
     *
     * @param src 源
     * @param key 秘钥
     * @return 结果
     * @throws Exception 加密异常
     */
    public static String AESEncrypt(String src, String key) throws Exception {
        if (Objects.isNull(src)) {
            src = "";
        }
        if (key.length() != 16) {
            throw new IllegalArgumentException("length of key must be 16: " + key.length());
        }
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec spec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//"算法/模式/补码方式"
        cipher.init(Cipher.ENCRYPT_MODE, spec);
        byte[] encrypted = cipher.doFinal(src.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

    /**
     * AES解密
     *
     * @param src 源
     * @param key 秘钥
     * @return 结果
     */
    public static String AESDecrypt(String src, String key) {
        try {
            if (key.length() != 16) {
                throw new IllegalArgumentException("length of key must be 16");
            }
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec spec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, spec);
            byte[] encrypted1 = Base64.getDecoder().decode(src);
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * 生成公钥私钥对
     *
     * @return left: public key, right: private key
     * @throws NoSuchAlgorithmException if no Provider supports a KeyPairGeneratorSpi implementation for the specified algorithm.
     */
    public static Pair<String, String> genRSAKeyPair() throws NoSuchAlgorithmException {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(1024, new SecureRandom());

        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        String publicKeyString = new String(Base64.getEncoder().encode(publicKey.getEncoded()));
        String privateKeyString = new String(Base64.getEncoder().encode((privateKey.getEncoded())));
        return Pair.of(publicKeyString, privateKeyString);
    }

    /**
     * RSA加密
     *
     * @param content   被加密的内容
     * @param publicKey 公钥
     * @return 加密后的内容
     * @throws Exception error
     */
    public static String RSAEncrypt(String content, String publicKey) throws Exception {
        //base64编码的公钥
        byte[] decoded = Base64.getDecoder().decode(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * RSA解密
     *
     * @param str        源
     * @param privateKey 私钥
     * @return 解密后的内容
     * @throws Exception error
     */
    public static String RSADecrypt(String str, String privateKey) throws Exception {
        //64位解码加密后的字符串
        byte[] inputByte = Base64.getDecoder().decode(str.getBytes(StandardCharsets.UTF_8));
        //base64编码的私钥
        byte[] decoded = Base64.getDecoder().decode(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        return new String(cipher.doFinal(inputByte));
    }

    /**
     * MD5加密
     *
     * @param str 源
     * @return 加密字符串
     * @throws Exception error
     */
    public static String MD5(String str) throws Exception {
        if (Objects.isNull(str)) {
            str = "";
        }
        MessageDigest md5 = MessageDigest.getInstance("md5");
        byte[] s = md5.digest(str.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : s) {
            result.append(Integer.toHexString((0x000000ff & b) | 0xffffff00).substring(6));
        }
        return result.toString();
    }
}
