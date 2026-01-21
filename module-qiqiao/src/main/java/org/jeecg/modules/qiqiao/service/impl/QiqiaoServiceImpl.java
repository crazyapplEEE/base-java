package org.jeecg.modules.qiqiao.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.common.constant.HttpMethods;
import org.jeecg.modules.qiqiao.service.IQiqiaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j @Service public class QiqiaoServiceImpl implements IQiqiaoService {
    @Autowired private RedisUtil redisUtil;
    @Value("${biisaas.tokenRedisKey}") private String tokenRedisKey;
    @Value("${biisaas.baseUrl}") private String baseUrl;
    @Value("${biisaas.corpId}") private String corpId;
    @Value("${biisaas.secret}") private String secret;
    @Value("${biisaas.account}") private String account;
    @Value("${biisaas.accessKeyUrl}") private String accessKeyUrl;
    @Value("${biisaas.accessTokenUrl}") private String accessTokenUrl;
    @Value("${biisaas.usersAccountUrl}") private String usersAccountUrl;
    @Value("${biisaas.getUsersUrl}") private String getUsersUrl;
    @Value("${biisaas.getDeptsFromQywxIds}") private String getDeptsFromQywxIdsUrl;

    /**
     * 使用accessToken请求接口的方式(检测到token过期会重试)
     */
    @Override public String requestWith(final String urlString, final String method, final Object data, String token,
        Integer remainRetryCnt) {
        log.info("requestWith: " + urlString);
        final String prefix = "[requestWith] remainRetryCnt=" + remainRetryCnt + " ";
        StringBuilder sb = null;
        InputStream inputStream;
        HttpURLConnection httpUrlConnection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            httpUrlConnection = (HttpURLConnection)url.openConnection();
            // 设置连接网络的超时时间
            httpUrlConnection.setConnectTimeout(15000);
            httpUrlConnection.setReadTimeout(60000);
            httpUrlConnection.setDoInput(true);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setRequestMethod(method);
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            //设置请求头header
            if (StringUtils.isNotBlank(token)) {
                httpUrlConnection.setRequestProperty("X-Auth0-Token", token);
            }

            //设置请求体
            if (data != null) {
                OutputStream outputStream = httpUrlConnection.getOutputStream();
                outputStream.write(JSON.toJSONString(data).getBytes());
            }

            int responseCode = httpUrlConnection.getResponseCode();
            if (responseCode == 200) {
                // 从服务器获得一个输入流
                inputStream = httpUrlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String lines;
                sb = new StringBuilder();
                while ((lines = reader.readLine()) != null) {
                    sb.append(lines);
                }
            } else {
                log.error(prefix + "Url: " + urlString + " response code " + responseCode);
            }
        } catch (Exception e) {
            log.error(prefix + "EXCEPTION CAUGHT: " + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error(prefix + "EXCEPTION CAUGHT: " + e.getMessage());
            }

            if (httpUrlConnection != null) {
                httpUrlConnection.disconnect();
            }
        }
        // 检测错误码
        if (remainRetryCnt == 0) {
            return sb == null ? null : sb.toString();
        }
        // 递归终止条件: remainRetryCnt == 0 或 成功
        if (sb != null) {
            JSONObject resultJson = null;

            try {
                resultJson = JSON.parseObject(sb.toString());
            } catch (Exception e) {
                // log.warn("CANNOT PARSE JSON: " + sb);
                return sb.toString();
            }
            if ("0".equals(resultJson.getString("code"))) {
                return sb.toString();
            } else {
                if ("-401".equals(resultJson.getString("code"))) {
                    // 刷新token
                    log.info(prefix + "token异常，刷新token" + resultJson + ", url: " + urlString);
                    token = getAccessToken(true);
                }
                // 重试
                log.info(prefix + "code异常 重试！" + resultJson);
                return requestWith(urlString, method, data, token, remainRetryCnt - 1);
            }
        } else {
            // 重试
            log.info(prefix + "sb == null 重试！");
            return requestWith(urlString, method, data, token, remainRetryCnt - 1);
        }
    }

    @Override public String getAccessToken(boolean fresh) {
        String at = "";
        if (!fresh) {
            at = (String)redisUtil.get(tokenRedisKey);
        }
        if (fresh || StringUtils.isBlank(at)) {
            String timeStamp = String.valueOf(System.currentTimeMillis());
            try {
                String ak = requestWith(
                    baseUrl + accessKeyUrl + "?timestamp=" + timeStamp + "&random=2003" + "&corpId=" + corpId + "&secret=" + secret + "&account=" + account,
                    HttpMethods.GET, null, null, 0);
                if (StringUtils.isNotBlank(ak)) {
                    at = requestWith(
                        baseUrl + accessTokenUrl + "?timestamp=" + timeStamp + "&random=2003" + "&corpId=" + corpId + "&secret=" + secret + "&account=" + account + "&accessKey=" + ak,
                        HttpMethods.GET, null, null, 1);
                    if (StringUtils.isNotBlank(at)) {
                        // 有效时间设置为7小时（openAPI文档为8小时）
                        redisUtil.set(tokenRedisKey, at, 60 * 60 * 7);
                        return at;
                    }
                }
            } catch (Exception e) {
                log.error("getAccessToken 失败");
                e.printStackTrace();
                return null;
            }

        }
        return at;
    }

    @Override public JSONObject usersAccount(String account) {
        if (StringUtils.isBlank(account)) {
            return null;
        }
        String result =
            requestWith(baseUrl + usersAccountUrl + "?account=" + account, HttpMethods.GET, null, getAccessToken(false),
                1);
        log.info("usersAccount " + account + ": " + result);
        if (StringUtils.isNotBlank(result)) {
            final JSONObject jsonObject = JSONObject.parseObject(result);
            if ("0".equals(jsonObject.getString("code"))) {
                return jsonObject.getJSONObject("data");
            }
        }
        return null;
    }

    @Override public JSONObject usersInfo(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        String result = requestWith(baseUrl + getUsersUrl + userId, HttpMethods.GET, null, getAccessToken(false), 1);
        if (StringUtils.isNotBlank(result)) {
            final JSONObject jsonObject = JSONObject.parseObject(result);
            if ("0".equals(jsonObject.getString("code"))) {
                return jsonObject.getJSONObject("data");
            }
        }
        return null;
    }

    @Override public JSONObject getDepartmentsFromQywxIds(List<String> qwDeptIds) {
        if (CollectionUtils.isEmpty(qwDeptIds)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < qwDeptIds.size(); ++i) {
            if (i == qwDeptIds.size() - 1) {
                sb.append(qwDeptIds.get(i));
            } else {
                sb.append(qwDeptIds.get(i)).append(",");
            }
        }
        final String result =
            requestWith(baseUrl + getDeptsFromQywxIdsUrl + "?departmentIds=" + sb, HttpMethods.GET, null,
                getAccessToken(false), 0);
        if (StringUtils.isNotBlank(result)) {
            final JSONObject jsonObject = JSONObject.parseObject(result);
            if ("0".equals(jsonObject.getString("code"))) {
                return jsonObject.getJSONObject("data");
            }
        }
        return null;
    }

    @Override public JSONObject simpleGetJsonObject(final String url) {
        log.info("requestUrl: " + url);
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        final String result = requestWith(url, HttpMethods.GET, null, getAccessToken(false), 1);
        if (StringUtils.isNotBlank(result)) {
            final JSONObject jsonObject = JSONObject.parseObject(result);
            if ("0".equals(jsonObject.getString("code"))) {
                return jsonObject.getJSONObject("data");
            } else {
                log.warn("result: " + result);
            }
        }
        return null;
    }

    @Override public JSONArray simpleGetJsonArray(final String url) {
        log.info("requestUrl: " + url);
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        final String result = requestWith(url, HttpMethods.GET, null, getAccessToken(false), 1);
        if (StringUtils.isNotBlank(result)) {
            final JSONObject jsonObject = JSONObject.parseObject(result);
            if ("0".equals(jsonObject.getString("code"))) {
                return jsonObject.getJSONArray("data");
            } else {
                log.warn("result: " + result);
            }
        }
        return null;
    }
}
