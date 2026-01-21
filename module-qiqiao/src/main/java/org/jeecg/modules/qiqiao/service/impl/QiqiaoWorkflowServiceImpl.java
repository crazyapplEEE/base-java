package org.jeecg.modules.qiqiao.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.modules.common.constant.HttpMethods;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoService;
import org.jeecg.modules.qiqiao.service.IQiqiaoWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j @Service public class QiqiaoWorkflowServiceImpl implements IQiqiaoWorkflowService {
    @Autowired private IQiqiaoService qiqiaoService;

    @Value("${biisaas.baseUrl}") private String baseUrl;
    @Value("${biisaas.workflow.form}") private String formUrl;
    @Value("${biisaas.workflow.start}") private String startUrl;
    @Value("${biisaas.workflow.delete}") private String deleteUrl;
    @Value("${biisaas.workflow.nextApprovers}") private String nextApproversUrl;

    @Override public JSONObject getWorkflowForm(final String applicationId, final String processId) {
        final String requestUrl =
            baseUrl + formUrl.replace("{applicationId}", applicationId).replace("{modelId}", processId);
        log.info("requestUrl: " + requestUrl);
        final String result =
            qiqiaoService.requestWith(requestUrl, HttpMethods.GET, null, qiqiaoService.getAccessToken(false), 1);
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultJson = JSON.parseObject(result);
            if ("0".equals(resultJson.getString("code"))) {
                return resultJson.getJSONObject("data");
            }
        } else {
            log.error("NULL DETECTED! requestUrl=" + requestUrl);
        }
        return null;
    }

    @Override public JSONObject startWorkflow(final RecordVO recordVO, final boolean needApprovers) {
        String prefix = "[startWorkflow] ";
        Map<String, Object> requestObj = new HashMap<>(4);
        if (StringUtils.isEmpty(recordVO.getLoginUserId())) {
            log.error(prefix + "LOGIN USER ID IS EMPTY!");
            return null;
        }
        prefix += recordVO.getLoginUserId() + " ";

        requestObj.put("loginUserId", recordVO.getLoginUserId());
        final JSONObject nextApprovers = getNextApprovers(recordVO);
        if (needApprovers && nextApprovers == null) {
            log.error(prefix + "nextApprovers IS EMPTY!");
            return null;
        }

        requestObj.put("nextNodesAndHandlers",
            needApprovers ? addApprovers(nextApprovers.getJSONArray("nextNodesAndHandlers")) : new JSONArray());

        // not required
        requestObj.put("designatedNodesAndHandlers",
            needApprovers ? addApprovers(nextApprovers.getJSONArray("designatedNodesAndHandlers")) : new JSONArray());
        requestObj.put("variables", recordVO.getData());
        log.info(prefix + "requestObj=" + requestObj);

        final String requestUrl = baseUrl + startUrl.replace("{applicationId}", recordVO.getApplicationId())
            .replace("{modelId}", recordVO.getProcessId());
        log.info(prefix + "requestUrl=" + requestUrl);
        final String result =
            qiqiaoService.requestWith(requestUrl, HttpMethods.POST, requestObj, qiqiaoService.getAccessToken(false), 1);
        log.info(prefix + "result=" + result);
        if (StringUtils.isNotEmpty(result)) {
            try {
                final JSONObject jsonObject = JSON.parseObject(result);
                final String code = jsonObject.getString("code");
                if ("0".equals(code)) {
                    return jsonObject.getJSONObject("data");
                }
            } catch (Exception e) {
                log.error(prefix + "EXCEPTION CAUGHT! " + e.getMessage());
            }
        }
        return null;
    }

    @Override public JSONObject deleteWorkflow(RecordVO recordVO) {
        final String prefix = "[deleteWorkflow] ";

        final String requestUrl = baseUrl + deleteUrl.replace("{applicationId}", recordVO.getApplicationId())
            .replace("{processInstanceId}", recordVO.getProcessInstanceId());
        log.info(prefix + "requestUrl=" + requestUrl);
        final String result =
            qiqiaoService.requestWith(requestUrl, HttpMethods.DELETE, null, qiqiaoService.getAccessToken(false), 1);
        log.info(prefix + "result=" + result);
        if (StringUtils.isNotEmpty(result)) {
            try {
                final JSONObject jsonObject = JSON.parseObject(result);
                final String code = jsonObject.getString("code");
                if ("0".equals(code)) {
                    return jsonObject.getJSONObject("data");
                }
            } catch (Exception e) {
                log.error(prefix + "EXCEPTION CAUGHT! " + e.getMessage());
            }
        }
        return null;
    }

    @Override public JSONObject getNextApprovers(final RecordVO recordVO) {
        final String prefix = "[getNextApprovers] ";
        Map<String, Object> requestObj = new HashMap<>();
        if (StringUtils.isEmpty(recordVO.getLoginUserId())) {
            log.error(prefix + "LOGIN USER ID IS EMPTY!");
        }
        // required
        requestObj.put("loginUserId", recordVO.getLoginUserId());

        // ======== not required =============
        requestObj.put("processInstanceId", "");
        requestObj.put("taskId", "");
        requestObj.put("variables", new HashMap<>());
        // ====================================

        final String accessToken = qiqiaoService.getAccessToken(false);
        final String requestUrl = baseUrl + nextApproversUrl.replace("{applicationId}", recordVO.getApplicationId())
            .replace("{processModelId}", recordVO.getProcessId()) + "?X-Auth0-Token=" + accessToken;
        log.info(prefix + "requestUrl=" + requestUrl);

        final String result = qiqiaoService.requestWith(requestUrl, HttpMethods.POST, requestObj, accessToken, 1);
        log.info(prefix + "result=" + result);

        try {
            final JSONObject jsonObject = JSON.parseObject(result);
            if ("0".equals(jsonObject.getString("code"))) {
                return jsonObject.getJSONObject("data");
            }
        } catch (Exception e) {
            log.error(prefix + "EXCEPTION CAUGHT! " + e.getMessage());
        }

        return null;
    }

    private JSONArray addApprovers(JSONArray nodesAndHandlers) {
        if (nodesAndHandlers == null) {
            return null;
        }

        for (int i = 0; i < nodesAndHandlers.size(); ++i) {
            final JSONObject jsonObject = nodesAndHandlers.getJSONObject(i);
            if (jsonObject.get("approvers") != null) {
                continue;
            }

            final JSONArray handlers = jsonObject.getJSONArray("handlers");
            if (handlers == null) {
                continue;
            }
            List<String> approverIdList = new ArrayList<>(handlers.size());
            for (int j = 0; j < handlers.size(); ++j) {
                approverIdList.add(handlers.getJSONObject(j).getString("id"));
            }
            jsonObject.put("approvers", approverIdList);
        }
        return nodesAndHandlers;
    }

}
