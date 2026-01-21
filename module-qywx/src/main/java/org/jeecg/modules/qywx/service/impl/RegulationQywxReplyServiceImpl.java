package org.jeecg.modules.qywx.service.impl;

import com.alibaba.fastjson.JSONArray;
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
import org.jeecg.modules.qywx.service.RegulationQywxReplyService;
import org.jeecg.modules.qywx.service.WxInterface;
import org.jeecg.modules.regulation.dto.ChatContentDTO;
import org.jeecg.modules.regulation.service.RegulationAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @author zmy
 */
@Slf4j @Service public class RegulationQywxReplyServiceImpl implements RegulationQywxReplyService {
    @Value("${chat-service.api}") private String apiUrl;
    @Value("${chat-service.params.is_knowledge}") private Boolean is_knowledge;
    @Value("${chat-service.params.memory}") private Integer memory;
    @Value("${chat-service.params.top_p}") private Integer top_p;
    @Value("${chat-service.params.max_length}") private Integer max_length;
    @Value("${chat-service.params.temperature}") private Double temperature;
    @Value("${chat-service.params.model}") private String model;
    @Value("${chat-service.knowledge_base.prompt_template}") private String promptTemplate;
    @Autowired private RegulationAiService regulationAiService;
    @Autowired private WxInterface wxInterface;

    @Override @Async public void reply(final String fromUserName, final String content) {
        log.info("Reply to " + fromUserName + " (question: " + content + ")");

        // @todo 知识库
        // 查找知识库
        final JSONArray knowledgeDocs = regulationAiService.queryKnowledge(content);
        StringBuilder knowledgeSb = new StringBuilder();
        if (CollectionUtils.isNotEmpty(knowledgeDocs)) {
            for (int i = 0; i < knowledgeDocs.size(); i++) {
                final JSONObject knowledgeDoc = knowledgeDocs.getJSONObject(i);
                if (knowledgeDoc == null) {
                    continue;
                }

                final JSONObject metadata = knowledgeDoc.getJSONObject("metadata");
                log.info("metadata: " + metadata);
                if (metadata == null) {
                    continue;
                }

                String knowledge = metadata.getString("full_knowledge");
                if (StringUtils.isEmpty(knowledge)) {
                    knowledge = knowledgeDoc.getString("page_content");
                }
                knowledgeSb.append(knowledge).append("\n");
            }
        }

        String prompt;
        if (knowledgeSb.length() == 0) {
            prompt = content;
        } else {
            prompt = promptTemplate.replace("{knowledge}", knowledgeSb.toString()).replace("{question}", content);
        }

        ChatContentDTO chatContentDTO = new ChatContentDTO();
        chatContentDTO.setPrompt(prompt);
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

        String result = "对不起，我不清楚你在讲什么，你可以尝试问问别的问题 :)";
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
                    // log.info("messagePart: " + messagePart);
                    final String[] splitMessages = messagePart.split("data: ");
                    String splitMessage = splitMessages[splitMessages.length - 1];

                    final String pattern = "\"text\": \"";
                    final int lastIndexOfText = splitMessage.lastIndexOf(pattern);
                    String substring = null;
                    if (lastIndexOfText >= 0) {
                        substring = splitMessage.substring(lastIndexOfText + pattern.length());
                        final int firstIndexOfQuote = substring.indexOf("\"");
                        if (firstIndexOfQuote >= 0) {
                            substring = substring.substring(0, firstIndexOfQuote);
                            // log.info("substring: " + substring);
                        }
                    }
                    result = StringUtils.unicodeToChinese(substring);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("result: " + result);
        wxInterface.sendTextMessage(fromUserName, null, null, result);
    }
}
