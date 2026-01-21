package org.jeecg.modules.qywx.service;

import org.jeecg.modules.qywx.dto.WechatJsJdkConfDTO;

public interface WxInterface {
    WechatJsJdkConfDTO getJdkConf(String url, String source);

    String getWxToken(String source, boolean refresh);

    String getPrivateWxUserInfo(String accessToken, String code);

    /**
     * 主动发送文本消息
     *
     * @param touser 成员ID列表（消息接收者，多个接收者用‘|’分隔，最多支持1000个）。特殊情况：指定为@all，则向该企业应用的全部成员发送
     * @param toparty 部门ID列表，多个接收者用‘|’分隔，最多支持100个。当touser为@all时忽略本参数
     * @param totag 标签ID列表，多个接收者用‘|’分隔，最多支持100个。当touser为@all时忽略本参数
     * @param content 消息内容，最长不超过2048个字节
     */
    void sendTextMessage(String touser, String toparty, String totag, String content);
}
