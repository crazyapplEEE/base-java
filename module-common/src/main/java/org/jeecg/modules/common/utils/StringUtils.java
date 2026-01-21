package org.jeecg.modules.common.utils;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static String encodeURIComponent(String s) {
        String result = null;

        try {
            result =
                URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20").replaceAll("\\%21", "!").replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(").replaceAll("\\%29", ")").replaceAll("\\%7E", "~");
        }

        // This exception should never occur.
        catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    public static boolean isEmpty(String s) {
        return org.apache.commons.lang3.StringUtils.isEmpty(s);
    }

    public static boolean isNotEmpty(String s) {
        return org.apache.commons.lang3.StringUtils.isNotEmpty(s);
    }

    // 提取中文数字部分
    public static int extractChineseNumber(String str) {
        String chineseNumber = "";
        Pattern pattern = Pattern.compile("[一二三四五六七八九十百千万]+");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            chineseNumber = matcher.group();
        }

        return convertChineseNumberToInt(chineseNumber);
    }

    // 将中文数字转换为整数
    private static int convertChineseNumberToInt(String chineseNumber) {
        int result = 0;

        for (int i = 0; i < chineseNumber.length(); i++) {
            char c = chineseNumber.charAt(i);
            int digit = getChineseNumberDigit(c);

            // 处理十、百、千等单位
            if (digit >= 10) {
                if (result == 0) {
                    result = 1;
                }
                result *= digit;
            } else {
                result += digit;
            }
        }

        return result;
    }

    // 获取中文数字对应的十进制值
    private static int getChineseNumberDigit(char c) {
        switch (c) {
            case '一':
                return 1;
            case '二':
                return 2;
            case '三':
                return 3;
            case '四':
                return 4;
            case '五':
                return 5;
            case '六':
                return 6;
            case '七':
                return 7;
            case '八':
                return 8;
            case '九':
                return 9;
            case '十':
                return 10;
            case '百':
                return 100;
            case '千':
                return 1000;
            case '万':
                return 10000;
            default:
                return 0;
        }
    }

    public static String unicodeToChinese(final String unicode) {
        if (isEmpty(unicode)) {
            return "";
        }
        String escaped = StringEscapeUtils.unescapeJava(unicode);
        String normalized = Normalizer.normalize(escaped, Normalizer.Form.NFKC);
        byte[] utf8Bytes = normalized.getBytes(StandardCharsets.UTF_8);
        return new String(utf8Bytes, StandardCharsets.UTF_8);
    }
}
