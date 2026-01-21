package org.jeecg.modules.common.utils;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.jeecg.modules.common.constant.ApplicationProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Tong Ling
 */
@Component public class ProxyUtils {
    private static String proxyIp;
    private static Integer proxyPort;
    private static Integer proxyConnectTimeout;
    private static Integer proxySocketTimeout;
    private static Integer proxyRequestTimeout;

    private static String profile;

    /**
     * 设置代理IP，连接超时时间，请求读取数据的超时时间，从connectManager获取Connection超时时间
     */
    public static RequestConfig generateRequestConfig() {
        if (ApplicationProfile.DEV.equals(profile)) {
            return RequestConfig.custom().setConnectTimeout(proxyConnectTimeout).setSocketTimeout(proxySocketTimeout)
                .setConnectionRequestTimeout(proxyRequestTimeout).build();
        }

        HttpHost proxy = new HttpHost(proxyIp, proxyPort);
        return RequestConfig.custom().setProxy(proxy).setConnectTimeout(proxyConnectTimeout)
            .setSocketTimeout(proxySocketTimeout).setConnectionRequestTimeout(proxyRequestTimeout).build();
    }

    @Value("${spring.profiles.active}") public void setProfile(String profile) {
        ProxyUtils.profile = profile;
    }

    @Value("${proxy.ip}") public void setProxyIp(String proxyIp) {
        ProxyUtils.proxyIp = proxyIp;
    }

    @Value("${proxy.port}") public void setProxyPort(Integer proxyPort) {
        ProxyUtils.proxyPort = proxyPort;
    }

    @Value("${proxy.connect-timeout}") public void setProxyConnectTimeout(Integer proxyConnectTimeout) {
        ProxyUtils.proxyConnectTimeout = proxyConnectTimeout;
    }

    @Value("${proxy.socket-timeout}") public void setProxySocketTimeout(Integer proxySocketTimeout) {
        ProxyUtils.proxySocketTimeout = proxySocketTimeout;
    }

    @Value("${proxy.request-timeout}") public void setProxyRequestTimeout(Integer proxyRequestTimeout) {
        ProxyUtils.proxyRequestTimeout = proxyRequestTimeout;
    }
}
