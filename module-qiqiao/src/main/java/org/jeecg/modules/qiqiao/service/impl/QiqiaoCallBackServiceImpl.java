package org.jeecg.modules.qiqiao.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.common.constant.HttpMethods;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.qiqiao.service.IQiqiaoCallBackService;
import org.jeecg.modules.qiqiao.service.IQiqiaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author zhouwei
 * @date 2024/9/26
 */
@Slf4j
@Service
public class QiqiaoCallBackServiceImpl implements IQiqiaoCallBackService {
    @Autowired
    private IQiqiaoService qiqiaoService;

    @Value("${biisaas.baseUrl}")
    private String baseUrl;
    @Value("${biisaas.callBackUrl}")
    private String callBackUrl;
    @Value("${biisaas.corpId}")
    private String corpId;
    @Value("${biisaas.secret}")
    private String secret;
    @Value("${biisaas.account}")
    private String account;

    @Override
    public JSONObject callBack(String applicationId, String taskId, Map data) {
        String prefix = "callBack: ";
        if (applicationId == null) {
            log.warn(prefix + "applicationId IS NULL!");
            return null;
        }
        if (taskId == null) {
            log.warn(prefix + "taskId IS NULL!");
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(baseUrl + callBackUrl + "?corpId=" + corpId + "&secret=" + secret + "&account="
                + account + "&applicationId=" + applicationId + "&taskId=" + taskId);
        for (Object key : data.keySet()) {
            stringBuilder.append("&").append(key).append("=").append(data.get(key));
        }

        String result = qiqiaoService.requestWith(stringBuilder.toString(), HttpMethods.GET, null, qiqiaoService.getAccessToken(false), 1);
        if (StringUtils.isNotEmpty(result)) {
            JSONObject resultJson = JSON.parseObject(result);
            if ("0".equals(resultJson.getString("code"))) {
                return (JSONObject) resultJson.get("data");
            } else {
                log.error(prefix + result);
                return null;
            }
        } else {
            log.error(prefix + result);
            return null;
        }
    }
}
