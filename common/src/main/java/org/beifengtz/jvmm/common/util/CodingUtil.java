package org.beifengtz.jvmm.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 14:37 2021/5/22
 *
 * @author beifengtz
 */
public class CodingUtil {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final ByteBuffer LONG_BUFFER = ByteBuffer.allocate(8);

    public static String encodeUrl(String url, String enc) {
        try {
            return URLEncoder.encode(url, enc);
        } catch (UnsupportedEncodingException ignored) {
            return url;
        }
    }

    public static String encodeUrl(String url) {
        return encodeUrl(url, "utf-8");
    }

    public static String decodeUrl(String url, String enc) {
        try {
            return URLDecoder.decode(url, enc);
        } catch (UnsupportedEncodingException ignored) {
            return url;
        }
    }

    public static String decodeUrl(String url) {
        return decodeUrl(url, "UTF-8");
    }

    /**
     * 将hex字符串转为byte数组
     *
     * @param hexStr hex string
     * @return byte array
     */
    public static byte[] hexStr2Bytes(String hexStr) {
        int l = hexStr.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = Integer.valueOf(hexStr.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

    /**
     * 将byte数组转为hex字符串
     *
     * @param bytes byte array
     * @return hex string
     * @deprecated 改用 {@link CodingUtil#bytes2HexString(byte[])}
     */
    @Deprecated
    public static String bytes2HexStr(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte value : bytes) {
            sb.append(String.format("%02X", value));
        }
        return sb.toString();
    }

    /**
     * 将byte数组转为hex字符串
     *
     * @param bytes byte array
     * @return hex  string
     */
    public static String bytes2HexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static short byteArrayToShort(byte[] array) {
        short value = 0;
        for (int i = 0; i < array.length; i++) {
            // & 0xff，除去符号位干扰
            value |= (short) ((array[i] & 0xff) << (i * 8));
        }
        return value;
    }

    public static int byteArrayToInt(byte[] array) {
        int value = 0;
        for (int i = 0; i < array.length; i++) {
            value |= ((array[i] & 0xff) << (i * 8));

        }
        return value;
    }

    public static long byteArrayToLong(byte[] array) {
        long value = 0;
        for (int i = 0; i < array.length; i++) {
            value |= ((long) (array[i] & 0xff) << (i * 8));
        }
        return value;
    }

    public static byte[] intToAtomicByteArray(int v) {
        if (v <= Short.MAX_VALUE) {
            return shortToAtomicByteArray((short) v);
        }
        byte[] array = new byte[4];

        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) (v >> (i * 8));
        }
        return array;
    }

    public static byte[] intToByteArray(int v) {
        byte[] array = new byte[4];

        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) (v >> (i * 8));
        }
        return array;
    }

    public static byte[] shortToAtomicByteArray(short v) {
        if (v <= Byte.MAX_VALUE) {
            return new byte[]{(byte) v};
        }
        byte[] array = new byte[2];
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) (v >> (i * 8));
        }
        return array;
    }

    public static byte[] shortToByteArray(short v) {
        byte[] array = new byte[2];
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) (v >> (i * 8));
        }
        return array;
    }

    public static byte[] longToAtomicByteArray(long v) {
        if (v <= Integer.MAX_VALUE) {
            return intToAtomicByteArray((int) v);
        }
        byte[] array = new byte[8];

        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) (v >> (i * 8));
        }
        return array;
    }

    public static byte[] longToByteArray(long v) {
        byte[] array = new byte[8];

        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) (v >> (i * 8));
        }
        return array;
    }
}
