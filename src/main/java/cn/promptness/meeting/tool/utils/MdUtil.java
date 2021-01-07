package cn.promptness.meeting.tool.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MdUtil {

    /**
     * 加密字符串
     *
     * @return
     */
    public static String encipher(String password) {

        // 1).判断字符串
        if (password == null || password.length() == 0) {
            return null;
        }

        // 2).获取MessageDigest对象
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // 3).MessageDigest对象处理字符串,进行加密处理,得到16位的byte数组
        byte[] bs = digest.digest(password.getBytes());

        // 4).准备工作,准备需要将byte数字转化为字符的char数组
        StringBuilder builder = new StringBuilder();
        char[] c = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        for (byte b : bs) {

            // 5).获取高四位的值
            int high = (b >> 4) & 15;
            // 6).获取低四位的值
            int low = b & 15;

            char highChar = c[high];
            char lowChar = c[low];
            // 7).拼串
            builder.append(highChar).append(lowChar);
        }
        // 8).返回
        return builder.toString().toLowerCase();
    }


}
