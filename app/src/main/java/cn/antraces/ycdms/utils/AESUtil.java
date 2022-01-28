package cn.antraces.ycdms.utils;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    /*   算法/模式/填充 */
    private static final String CipherMode = "AES/ECB/PKCS5Padding";

    /*  创建密钥  */
    private static SecretKeySpec createKey(String password) {
        byte[] data;
        if (password == null) {
            password = "";
        }
        StringBuilder sb = new StringBuilder(32);
        sb.append(password);
        while (sb.length() < 32) {
            sb.append("0");
        }
        if (sb.length() > 32) {
            sb.setLength(32);
        }

        data = sb.toString().getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(data, "AES");
    }

    /* 加密字节数据  */
    public static byte[] encrypt(byte[] content, String password) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*加密(结果为16进制字符串)  */
    public static String encrypt(String content, String password) {
        byte[] data = null;
        try {
            data = content.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        data = encrypt(data, password);
        return byte2hex(data != null ? data : new byte[0]);
    }

    /*解密字节数组*/
    public static byte[] decrypt(byte[] content, String password) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*解密16进制的字符串为字符串  */
    public static String decrypt(String content, String password) {
        byte[] data = null;
        try {
            data = hex2byte(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        data = decrypt(data, password);
        if (data == null) return null;
        String result;
        result = new String(data, StandardCharsets.UTF_8);
        return result;
    }

    /*字节数组转成16进制字符串  */
    public static String byte2hex(byte[] b) { // 一个字节的数，
        StringBuilder sb = new StringBuilder(b.length * 2);
        String tmp;
        for (byte value : b) {
            // 整数转成十六进制表示
            tmp = (Integer.toHexString(value & 0XFF));
            if (tmp.length() == 1) {
                sb.append("0");
            }
            sb.append(tmp);
        }
        return sb.toString().toUpperCase(); // 转成大写
    }

    /*将hex字符串转换成字节数组 */
    private static byte[] hex2byte(String inputString) {
        if (inputString == null || inputString.length() < 2) {
            return new byte[0];
        }
        inputString = inputString.toLowerCase();
        int l = inputString.length() / 2;
        byte[] result = new byte[l];
        for (int i = 0; i < l; ++i) {
            String tmp = inputString.substring(2 * i, 2 * i + 2);
            result[i] = (byte) (Integer.parseInt(tmp, 16) & 0xFF);
        }
        return result;
    }
}
