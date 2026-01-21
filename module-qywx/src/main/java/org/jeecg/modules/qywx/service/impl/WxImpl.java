package org.jeecg.modules.qywx.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.common.constant.RedisKey;
import org.jeecg.modules.qywx.dto.MsgTextDTO;
import org.jeecg.modules.qywx.dto.WechatJsJdkConfDTO;
import org.jeecg.modules.qywx.dto.WxMsgTextDTO;
import org.jeecg.modules.qywx.service.WxInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.jeecg.modules.common.utils.ProxyUtils.generateRequestConfig;


/**
 * @author zmy
 */
@Slf4j @Service public class WxImpl implements WxInterface {
    private static final String PRIVATE = "PRIVATE";
    private final int FALL_TIME_LIMIT = 5;
    @Value("${wxConfig.corpId}") private String corpId;
    @Value("${wxConfig.wxAppSecret}") private String wxAppSecret;
    @Value("${wxConfig.getWxToken}") private String getWxToken;
    @Value("${wxConfig.getWxUserInfo}") private String getWxUserInfo;
    @Value("${wxConfig.getWxTicket}") private String getWxTicket;
    @Value("${wxConfig.sendMessageUrl}") private String sendMessageUrl;
    @Value("${wxConfig.publicCorpId}") private String publicCorpId;
    @Value("${wxConfig.publicWxAppSecret}") private String publicWxAppSecret;
    @Value("${wxConfig.publicGetWxToken}") private String publicGetWxToken;
    @Value("${wxConfig.publicGetWxTicket}") private String publicGetWxTicket;
    @Value("${wxConfig.agentid}") private String agentid;
    @Autowired private RedisUtil redisUtil;

    private String requestWithProxy(CloseableHttpClient client, HttpGet httpGet) {
        try {
            httpGet.setConfig(generateRequestConfig());
            CloseableHttpResponse response = client.execute(httpGet);
            HttpEntity reEntity = response.getEntity();
            return EntityUtils.toString(reEntity);
        } catch (IOException ioe) {
            log.error("请求出错哦：" + ioe.getMessage());
            return null;
        }
    }

    private String getWxTokenInt(String corpId, String wxAppSecret, String source) {
        if (PRIVATE.equals(source)) {
            try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(String.format(getWxToken, corpId, wxAppSecret));
                CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
                HttpEntity reEntity = closeableHttpResponse.getEntity();
                return EntityUtils.toString(reEntity);
            } catch (Exception e) {
                log.error("获取Token错误：" + e.getMessage());
                return "";
            } finally {
                log.info("获取Token完毕");
            }
        } else {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(String.format(publicGetWxToken, corpId, wxAppSecret));
            return requestWithProxy(client, httpGet);
        }

    }

    @Override public String getPrivateWxUserInfo(String accessToken, String code) {
        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            final String url = String.format(getWxUserInfo, accessToken, code);
            log.info("getPrivateWxUserInfo url: " + url);
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity reEntity = closeableHttpResponse.getEntity();
            final String result = EntityUtils.toString(reEntity);
            log.info("getPrivateWxUserInfo: " + result);
            return result;
        } catch (Exception e) {
            log.error("获取Token错误：" + e.getMessage());
            return "";
        } finally {
            log.info("获取Token完毕");
        }
    }

    @Override @Async
    public void sendTextMessage(final String touser, final String toparty, final String totag, final String content) {
        if (StringUtils.isEmpty(content)) {
            log.warn("CONTENT IS EMPTY");
            return;
        }

        if (StringUtils.isEmpty(touser) && StringUtils.isEmpty(toparty) && StringUtils.isEmpty(totag)) {
            log.warn("NO RECEIVER FOUND!");
            return;
        }

        // 发送消息
        final String url = String.format(sendMessageUrl, getWxToken(PRIVATE, false));
        final HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        WxMsgTextDTO wxMsgTextDTO = new WxMsgTextDTO();
        wxMsgTextDTO.setAgentid(agentid);
        wxMsgTextDTO.setTouser(touser);
        wxMsgTextDTO.setToparty(toparty);
        wxMsgTextDTO.setTotag(totag);
        wxMsgTextDTO.setText(new MsgTextDTO().setContent(content));

        try {
            httpPost.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(wxMsgTextDTO),
                    ContentType.APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }
        try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response =
                client.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, Consts.UTF_8);
                EntityUtils.consume(entity);
                log.info("result: " + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getWxTicketInt(String token, String source) {
        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            if (PRIVATE.equals(source)) {
                HttpGet httpGet = new HttpGet(String.format(getWxTicket, token));
                CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
                HttpEntity reEntity = closeableHttpResponse.getEntity();
                return EntityUtils.toString(reEntity);
            } else {
                CloseableHttpClient client = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(String.format(publicGetWxTicket, token));
                return requestWithProxy(client, httpGet);
            }
        } catch (Exception e) {
            log.error("获取Ticket错误：" + e.getMessage());
        } finally {
            log.info("获取Ticket完毕");
        }
        return "";
    }

    @Override public String getWxToken(final String source, final boolean refresh) {
        if (PRIVATE.equals(source)) {
            return getWxTokenString(source, corpId, wxAppSecret, refresh);
        } else {
            return getWxTokenString(source, publicCorpId, publicWxAppSecret, refresh);
        }

    }

    private String getWxTokenString(final String source, final String corpId, final String wxAppSecret,
                                    final boolean refresh) {
        final String redisKeyWxToken = RedisKey.QYWX_TOKEN + corpId + "_" + wxAppSecret;
        Object token = redisUtil.get(redisKeyWxToken);
        if (refresh) {
            redisUtil.del(redisKeyWxToken);
        } else if (token != null) {
            log.info("getWxTokenString: " + token);
            return String.valueOf(token);
        }
        String accessToken = "";
        Integer expireTime = 0;
        int failTimes = 0;
        while (StringUtils.isEmpty(accessToken) && failTimes < FALL_TIME_LIMIT) {
            String str = getWxTokenInt(corpId, wxAppSecret, source);
            log.info("getWxTokenString: " + str);
            if (StringUtils.isEmpty(str)) {
                failTimes++;
                continue;
            }
            JSONObject json = JSON.parseObject(str);
            if (json.getInteger("errcode") != 0) {
                failTimes++;
                continue;
            }
            accessToken = json.getString("access_token");
            expireTime = json.getInteger("expires_in");
        }

        if (StringUtils.isEmpty(accessToken)) {
            return "";
        } else {
            redisUtil.set(redisKeyWxToken, accessToken, expireTime / 2);
            return accessToken;
        }
    }

    private String getWxTicket(String source) {
        if (PRIVATE.equals(source)) {
            return getWxTicketString(source, corpId, wxAppSecret);
        } else {
            return getWxTicketString(source, publicCorpId, publicWxAppSecret);
        }
    }

    private String getWxTicketString(String source, String publicCorpId, String publicWxAppSecret) {
        final String redisKeyWxTicket = RedisKey.QYWX_TICKET + publicCorpId + "_" + publicWxAppSecret;
        Object ticket = redisUtil.get(redisKeyWxTicket);
        if (ticket != null) {
            return String.valueOf(ticket);
        }
        String token = getWxToken(source, false);
        if (StringUtils.isEmpty(token)) {
            return "";
        }
        String accessTicket = "";
        Integer expireTime = 7200;
        int failTimes = 0;
        while (StringUtils.isEmpty(accessTicket) && failTimes < FALL_TIME_LIMIT) {
            String str = getWxTicketInt(token, source);
            if (StringUtils.isEmpty(str)) {
                failTimes++;
                continue;
            }
            JSONObject json = JSON.parseObject(str);
            if (json.getInteger("errcode") != 0) {
                failTimes++;
                continue;
            }
            accessTicket = json.getString("ticket");
            expireTime = json.getInteger("expires_in");
        }

        if (StringUtils.isEmpty(accessTicket)) {
            return "";
        } else {
            redisUtil.set(redisKeyWxTicket, accessTicket, expireTime / 2);
            return accessTicket;
        }
    }

    @Override public WechatJsJdkConfDTO getJdkConf(String url, String source) {
        String ticket = getWxTicket(source);
        if (StringUtils.isEmpty(ticket)) {
            return null;
        }
        WechatJsJdkConfDTO dto = new WechatJsJdkConfDTO();
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = RandomStringUtils.randomAlphanumeric(16);
        String string;
        string = "jsapi_ticket=" + ticket + "&noncestr=" + nonceStr + "&timestamp=" + timestamp + "&url=" + url;
        if (PRIVATE.equals(source)) {
            dto.setAppId(corpId);
        } else {
            dto.setAppId(publicCorpId);
        }
        dto.setNonceStr(nonceStr);
        dto.setSignature(DigestUtils.sha1Hex(string));
        dto.setTimestamp(timestamp);
        return dto;
    }
}
