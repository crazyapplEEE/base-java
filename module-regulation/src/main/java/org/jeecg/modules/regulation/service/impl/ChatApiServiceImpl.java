package org.jeecg.modules.regulation.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.content.service.IContentManagementService;
import org.jeecg.modules.regulation.dto.ChatContentDTO;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiHistory;
import org.jeecg.modules.regulation.service.ChatApiService;
import org.jeecg.modules.regulation.service.IZyRegulationBiiHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zmy
 */
@Slf4j @Service public class ChatApiServiceImpl implements ChatApiService {
    @Value("${chat-service.api}") private String apiUrl;
    @Value("${chat-service.params.is_knowledge}") private Boolean is_knowledge;
    @Value("${chat-service.params.memory}") private Integer memory;
    @Value("${chat-service.params.top_p}") private Integer top_p;
    @Value("${chat-service.params.max_length}") private Integer max_length;
    @Value("${chat-service.params.temperature}") private Double temperature;
    @Value("${chat-service.params.model}") private String model;
    @Autowired private IZyRegulationBiiHistoryService zyRegulationBiiHistoryService;
    @Autowired @Qualifier("biiContentManagementService") private IContentManagementService contentManagementService;

    /**
     * 请求chat接口并返回结果
     *
     * @param session
     * @param msg
     */
    @Override
    public void requestMsg(final Session session, final String msg, final String source,
                           final Set<String> identifierSet, final Map<String, List<String>> identifier2KnowledgeList) {
        if (session == null || StringUtils.isEmpty(msg)) {
            return;
        }

        List<JSONObject> sourceInfoList = null;
        if (CollectionUtils.isNotEmpty(identifierSet)) {
            sourceInfoList = new ArrayList<>(identifierSet.size());
            for (final String identifier : identifierSet) {
                final List<ZyRegulationBiiHistory> zyRegulationBiiHistories = zyRegulationBiiHistoryService.queryByIdentifier(identifier);
                if (CollectionUtils.isEmpty(zyRegulationBiiHistories)) {
                    continue;
                }

                List<String> knowledgeList = new ArrayList<>();
                if (identifier2KnowledgeList != null && identifier2KnowledgeList.containsKey(identifier)) {
                    knowledgeList = identifier2KnowledgeList.get(identifier);
                }

                final ZyRegulationBiiHistory zyRegulationBiiHistory = zyRegulationBiiHistories.get(0);
                final String previewUrl = contentManagementService.getPreviewUrl(zyRegulationBiiHistory.getContentFileId());

                JSONObject sourceInfo = new JSONObject();
                sourceInfo.put("previewUrl", previewUrl);
                sourceInfo.put("name", zyRegulationBiiHistory.getName());
                sourceInfo.put("knowledgeList", knowledgeList);
                sourceInfoList.add(sourceInfo);
            }
        }
        log.info("sourceInfoList: " + sourceInfoList);

        log.info("请求chat接口并返回结果 :" + apiUrl);
        ChatContentDTO chatContentDTO = new ChatContentDTO();
        chatContentDTO.setPrompt(msg);
        chatContentDTO.set_knowledge(is_knowledge);
        chatContentDTO.setMemory(memory);
        chatContentDTO.setTop_p(top_p);
        chatContentDTO.setMax_length(max_length);
        chatContentDTO.setTemperature(temperature);
        chatContentDTO.setModel(model);
        chatContentDTO.setOptions(new JSONObject());
        final HttpPost httpPost = new HttpPost(apiUrl);
        String reqObj = JSONObject.toJSONString(chatContentDTO);
        log.info("请求参数 :" + reqObj);
        StringEntity entity = new StringEntity(reqObj, "utf-8");
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.addHeader("Content-Type", "application/json");

        final RemoteEndpoint.Basic basicRemote = session.getBasicRemote();
        try (CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
             CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost)) {
            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            InputStream inputStream = httpEntity.getContent();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
                String responseStr = outputStream.toString("UTF-8");
                String[] messageParts = responseStr.split("\n");
                for (String messagePart : messageParts) {
                    basicRemote.sendText(messagePart);
                }
            }

            if (StringUtils.isNotEmpty(source)) {
                log.info("source: " + source);
                log.info("sourceInfoList: " + sourceInfoList);
                basicRemote.sendText("data: {\"knowledge\": true, \"text\": \"" + source + "\", \"sourceInfoList\": " + sourceInfoList + "}");
            }

            // log.info("响应结果 :" + outputStream.toString("UTF-8"));
            basicRemote.sendText("data: {\"closed\": true}");
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + e.getMessage());
        }
    }
}
