package org.jeecg.modules.qiqiao.service;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * @author zhouwei
 * @date 2024/9/26
 */

public interface IQiqiaoCallBackService {
    /**
     * @param applicationId
     * @param taskId
     * @param data
     * @return
     */
    JSONObject callBack(String applicationId, String taskId, Map data);
}
