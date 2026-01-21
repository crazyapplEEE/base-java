package org.jeecg.modules.qywx.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.qywx.service.RegulationQywxReplyService;
import org.jeecg.modules.qywx.utils.WXBizMsgCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.StringReader;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Api(tags = "支撑-企业微信问答") @RestController @RequestMapping("/qywx") @Slf4j public class WxReplyController {
    @Value("${wxConfig.corpId}") private String coprId;
    @Value("${wxConfig.msgToken}") private String msgToken;
    @Value("${wxConfig.msgEncodingAESKey}") private String msgEncodingAESKey;
    @Autowired @Lazy private RegulationQywxReplyService regulationQywxReplyService;

    @AutoLog("支撑-企业微信问答-接收普通消息") @ApiOperation("企业微信问答-接收普通消息")
    @RequestMapping(value = "/receive_msg", method = {RequestMethod.GET, RequestMethod.POST})
    public String push(@RequestParam(name = "msg_signature") String msgSignature, @RequestParam String timestamp,
                       @RequestParam String nonce, @RequestParam(required = false) String echostr,
                       HttpServletRequest request) {
        log.info("[qywx/push]");
        log.info("msgSignature: " + msgSignature);
        log.info("timestamp: " + timestamp);
        log.info("nonce: " + nonce);
        log.info("echostr: " + echostr);

        String result = "";
        try {
            final WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(msgToken, msgEncodingAESKey, coprId);
            if (StringUtils.isEmpty(echostr)) {
                final String postDataString = getPostDataString(request);
                log.info("postDataString: " + postDataString);
                final String msg = wxcpt.DecryptMsg(msgSignature, timestamp, nonce, postDataString);
                log.info("msg: " + msg);

                result = extractStringFromXml(msg, "Content");
                final String fromUserName = extractStringFromXml(msg, "FromUserName");
                regulationQywxReplyService.reply(fromUserName, result);
            } else {
                // 配置时会调用此处
                result = wxcpt.VerifyURL(msgSignature, timestamp, nonce, echostr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("result: " + result);
        return result;
    }

    private String getPostDataString(final HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        final StringBuilder postData = new StringBuilder();
        try {
            // Read the POST data
            final BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                postData.append(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Process the POST data
        return postData.toString();
    }

    private String extractStringFromXml(final String xmlStr, final String tagName) {
        String result = null;
        if (StringUtils.isEmpty(xmlStr) || StringUtils.isEmpty(tagName)) {
            return result;
        }

        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final StringReader sr = new StringReader(xmlStr);
            final InputSource is = new InputSource(sr);
            final Document document = db.parse(is);

            final Element root = document.getDocumentElement();
            final NodeList nodelist = root.getElementsByTagName(tagName);
            if (nodelist.getLength() > 0) {
                result = nodelist.item(0).getTextContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
