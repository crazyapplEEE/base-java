package org.jeecg.modules.regulation.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.modules.regulation.dto.RegulationOld;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiHistory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JeecgSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegulationOldServiceTest {
    @Autowired private IZyRegulationBiiService zyRegulationBiiService;
    @Autowired private IZyRegulationBiiHistoryService zyRegulationBiiHistoryService;

    @Test public void sync() {
        zyRegulationBiiService.syncOldRegulationList();
    }

    @Test public void updateTime() {
        final List<ZyRegulationBiiHistory> zyRegulationBiiHistoryList = zyRegulationBiiHistoryService.list();
        Assert.assertNotNull(zyRegulationBiiHistoryList);

        zyRegulationBiiHistoryList.forEach(zyRegulationBiiHistory -> {
            final String code = zyRegulationBiiHistory.getCode();

            final String searchUrl = "https://jtregulation.bii.com.cn/bii/regulation/search";

            JSONObject data = new JSONObject();
            JSONObject query = new JSONObject();
            query.put("isActive", "3");
            query.put("id", code);
            data.put("query", query);

            JSONObject user = new JSONObject();
            user.put("loginId", "lingtong");
            user.put("companyType", "0");
            user.put("level", "1");

            final JSONArray result = search(searchUrl, data.toJSONString(), user);
            Assert.assertNotNull(result);

            final JSONObject jsonObject = result.getJSONObject(0);
            final Date publishTime = jsonObject.getDate("publishTime");
            final Date abolishTime = jsonObject.getDate("abolishTime");

            zyRegulationBiiHistory.setPublishTime(publishTime);
            zyRegulationBiiHistory.setAbolishTime(abolishTime);

            if (!zyRegulationBiiHistoryService.updateById(zyRegulationBiiHistory)) {
                System.out.println("FAILED TO UPDATE " + zyRegulationBiiHistory);
            }
        });
    }

    @Test public void getAll() {
        final String searchUrl = "https://jtregulation.bii.com.cn/bii/regulation/search";

        JSONObject data = new JSONObject();
        JSONObject query = new JSONObject();
        query.put("isActive", "1");
        query.put("type", "2");
        query.put("isLite", "1");
        query.put("flag", "1");
        data.put("query", query);

        JSONObject user = new JSONObject();
        user.put("loginId", "lingtong");
        user.put("companyType", "0");
        user.put("level", "1");

        search(searchUrl, data.toJSONString(), user);
    }

    @Test public void getById() {
        final String searchUrl = "https://jtregulation.bii.com.cn/bii/regulation/search";

        JSONObject data = new JSONObject();
        JSONObject query = new JSONObject();
        query.put("isActive", "3");
        query.put("id", "60d2f9e7d08eab2993b94b4d");
        data.put("query", query);

        JSONObject user = new JSONObject();
        user.put("loginId", "lingtong");
        user.put("companyType", "0");
        user.put("level", "1");

        search(searchUrl, data.toJSONString(), user);
    }

    @Test public void getLiteByIdList() {
        final String liteUrl = "https://jtregulation.bii.com.cn/bii/regulation/lite";

        JSONArray data = new JSONArray();
        data.add("60d2f9e7d08eab2993b94b4d");
        data.add("6422b0bdd08ebe33a82554c7");

        JSONObject user = new JSONObject();
        user.put("loginId", "lingtong");
        user.put("companyType", "0");
        user.put("level", "1");

        search(liteUrl, data.toJSONString(), user);
    }

    @Test public void downloadFile() {
        final String downloadUrl = "https://jtregulation.bii.com.cn/bii/file/download/{id}";
        String fileId = "60d2f9d1322b8f28041d4e64";
        String fileName = "附件1 合规管理规定（试行）.pdf";
        downloadFile(downloadUrl, fileId, fileName);
    }

    @Test public void convertResult() {
        final String searchUrl = "https://jtregulation.bii.com.cn/bii/regulation/search";

        JSONObject data = new JSONObject();
        JSONObject query = new JSONObject();
        query.put("isActive", "1");
        query.put("type", "2");
        query.put("isLite", "1");
        query.put("flag", "1");
        data.put("query", query);

        JSONObject user = new JSONObject();
        user.put("loginId", "lingtong");
        user.put("companyType", "0");
        user.put("level", "1");

        final JSONArray result = search(searchUrl, data.toJSONString(), user);
        try {
            final List<RegulationOld> regulationOldList =
                new ObjectMapper().readValue(result.toJSONString(), new TypeReference<List<RegulationOld>>() {
                });
            System.out.println(regulationOldList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONArray search(String url, String data, JSONObject user) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.addHeader("User", user.toJSONString());
        httpPost.setEntity(new StringEntity(data, "UTF-8"));
        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, Consts.UTF_8);
                EntityUtils.consume(entity);
                JSONObject resObj = JSONObject.parseObject(result);
                System.out.println(resObj);

                return resObj.getJSONArray("result");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JSONArray();
    }

    private void downloadFile(String url, String fileId, String fileName) {
        HttpGet httpGet = new HttpGet(url.replace("{id}", fileId));
        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = client.execute(httpGet)) {
            File file = new File(fileName);
            try (OutputStream out = Files.newOutputStream(file.toPath())) {
                httpResponse.getEntity().writeTo(out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
