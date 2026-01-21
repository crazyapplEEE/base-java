package org.jeecg.modules.qywx.utils;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.qywx.enums.ResponseEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

public class ErrorUtils {
    private static final JSONObject JSON_OBJECT = new JSONObject();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final Logger logger = LoggerFactory.getLogger(ErrorUtils.class);

    /**
     * 成功下封装
     *
     * @param obj
     * @param responseEnum
     * @return
     */
    public static JSONObject success(Object obj, ResponseEnum responseEnum) {
        JSON_OBJECT.put("respCode", responseEnum.getRespCode());
        JSON_OBJECT.put("respDesc", responseEnum.getRespDesc());
        JSON_OBJECT.put("respStamp", SIMPLE_DATE_FORMAT.format(System.currentTimeMillis()));
        JSON_OBJECT.put("response", obj);
        return JSON_OBJECT;
    }

    /**
     * 非catch下异常封装
     *
     * @param obj
     * @param responseEnum
     * @return
     */
    public static JSONObject formalError(Object obj, ResponseEnum responseEnum) {
        logger.info(responseEnum.toString());
        JSON_OBJECT.put("respCode", responseEnum.getRespCode());
        JSON_OBJECT.put("respDesc", responseEnum.getRespDesc());
        JSON_OBJECT.put("respStamp", SIMPLE_DATE_FORMAT.format(System.currentTimeMillis()));
        JSON_OBJECT.put("response", obj);
        return JSON_OBJECT;
    }
}
