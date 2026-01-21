package org.jeecg.modules.common.utils;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.codec.binary.Hex;
import org.jeecg.modules.common.constant.ApplicationProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Calendar;

/**
 * @author Tong Ling
 * @date 2021/11/15
 */
@Component public class EncryptionUtils {
    public final static String BII_SSO_SECRET = "GeHeImniS1311";
    public final static String EMOBILE_SECRET = "glnrSI";
    // 6 hours
    private final static Long MAX_TIME_DIFFERENCE_IN_MILLI = Long.valueOf("21600000");
    private static String profile;

    /**
     * 使用sha256算法计算文件摘要
     *
     * @param filePath 文件路径
     * @return 文件摘要
     */
    public static byte[] calculateSHA256(String filePath) {
        MessageDigest digest = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            fis = new FileInputStream(filePath);
            bis = new BufferedInputStream(fis);

            byte[] buffer = new byte[8192]; // 8k
            int sizeRead;
            while ((sizeRead = bis.read(buffer)) != -1) {
                digest.update(buffer, 0, sizeRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return digest.digest();
    }

    public static String hexSHA1(String value) {
        if (value == null) {
            return null;
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (Exception ex) {
            return null;
        }
        md.update(value.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        return byteToHexString(digest);
    }

    public static String byteToHexString(byte[] bytes) {
        return String.valueOf(Hex.encodeHex(bytes));
    }

    public static boolean loginCheck(String loginId, String timeMilli, String token) {
        return loginCheck(loginId, timeMilli, token, BII_SSO_SECRET);
    }

    public static boolean mobileLoginCheck(String loginId, String timeMilli, String token) {
        return loginCheck(loginId, timeMilli, token, EMOBILE_SECRET);
    }

    private static boolean loginCheck(final String loginId, final String timeMilli, final String token,
        final String secret) {
        if (StrUtil.isBlank(loginId) || StrUtil.isBlank(timeMilli) || StrUtil.isBlank(token)) {
            return false;
        }

        if (!ApplicationProfile.DEV.equals(profile)) {
            Long timeMilliVal = null;
            try {
                timeMilliVal = Long.valueOf(timeMilli);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            final long curTimeInMillis = Calendar.getInstance().getTimeInMillis();
            if (Math.abs(curTimeInMillis - timeMilliVal) > MAX_TIME_DIFFERENCE_IN_MILLI) {
                // Note: 如果报这个错误，可能是因为系统时间有问题，可考虑修改系统同步时钟地址
                return false;
            }
        }

        String calcToken = hexSHA1(secret + loginId + timeMilli);
        if (calcToken == null) {
            return false;
        }

        return calcToken.equals(token);
    }

    @Value("${spring.profiles.active}") public void setProfile(String profile) {
        EncryptionUtils.profile = profile;
    }
}
