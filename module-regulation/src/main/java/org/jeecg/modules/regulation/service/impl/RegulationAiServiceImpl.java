package org.jeecg.modules.regulation.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.regulation.service.RegulationAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j @Service public class RegulationAiServiceImpl implements RegulationAiService {
    @Value("${chat-service.knowledge_base.update_knowledge}") private String updateKnowledgeUrl;
    @Value("${chat-service.knowledge_base.query_knowledge}") private String queryKnowledgeUrl;

    @Override public JSONArray queryKnowledge(String question) {
        HttpPost httpPost = new HttpPost(queryKnowledgeUrl);
        log.info("Start to query ai");
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
        JSONObject data = new JSONObject();
        data.put("question", question);
        httpPost.setEntity(new StringEntity(data.toJSONString(), "UTF-8"));
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, Consts.UTF_8);
                EntityUtils.consume(entity);
                JSONObject resObj = JSONObject.parseObject(result);

                log.info("Finished querying ai");

                if (Integer.valueOf(200).equals(resObj.getInteger("code"))) {
                    return resObj.getJSONArray("data");
                }
            }
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }

        return null;
    }

    @Override
    public void updateDb(final String directoryPath, final String identifiersToAdd,
                         final String identifiersToUpdate, final String identifiersToDelete) {
        log.info("[updateDb] directoryPath=" + directoryPath);
        if (StringUtils.isEmpty(directoryPath)) {
            log.warn("DIRECTORY PATH IS EMPTY");
        }

        // 初始化知识库
        HttpPost httpPost = new HttpPost(updateKnowledgeUrl);
        log.info("Start to query ai");
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
        JSONObject data = new JSONObject();
        data.put("file_directory", directoryPath);
        if (StringUtils.isNotEmpty(identifiersToAdd)) {
            data.put("identifiers_to_add", identifiersToAdd);
        }
        if (StringUtils.isNotEmpty(identifiersToUpdate)) {
            data.put("identifiers_to_update", identifiersToUpdate);
        }
        if (StringUtils.isNotEmpty(identifiersToDelete)) {
            data.put("identifiers_to_delete", identifiersToDelete);
        }
        httpPost.setEntity(new StringEntity(data.toJSONString(), "UTF-8"));
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, Consts.UTF_8);
                EntityUtils.consume(entity);
                JSONObject resObj = JSONObject.parseObject(result);
                log.info("Finished initialization " + resObj);
            }
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }
    }
}
