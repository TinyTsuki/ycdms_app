package cn.antraces.ycdms.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//MD5加密
public class MD5Util {

    //加密字符串
    public static String getMD5Code(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes(StandardCharsets.UTF_8));
            byte[] encryption = md5.digest();
            StringBuilder stringBuffer = new StringBuilder();
            for (byte b : encryption) {
                if (Integer.toHexString(0xff & b).length() == 1) {
                    stringBuffer.append("0").append(Integer.toHexString(0xff & b));
                } else {
                    stringBuffer.append(Integer.toHexString(0xff & b));
                }
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            // e.printStackTrace();
            return "";
        }
    }

    public static String getMD5Code_16(String string) {
        return getMD5Code(string).substring(8, 24);
    }

    //加密文件
    public static String md5ForFile(File file) {
        int bufferSize = 1024;
        FileInputStream fis;
        DigestInputStream dis;

        try {
            //创建MD5转换器和文件流
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            dis = new DigestInputStream(fis, messageDigest);

            byte[] buffer = new byte[bufferSize];
            //DigestInputStream实际上在流处理文件时就在内部就进行了一定的处理
            while (dis.read(buffer) > 0) ;

            //通过DigestInputStream对象得到一个最终的MessageDigest对象。
            messageDigest = dis.getMessageDigest();

            // 通过messageDigest拿到结果，也是字节数组，包含16个元素
            byte[] array = messageDigest.digest();
            // 同样，把字节数组转换成字符串
            StringBuilder hex = new StringBuilder(array.length * 2);
            for (byte b : array) {
                if ((b & 0xFF) < 0x10) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

}
