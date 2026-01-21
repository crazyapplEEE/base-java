package org.jeecg.modules.qiqiao.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhangmingyu
 * @since 2022-05-17
 */
public interface IQiqiaoService {
    /**
     * 使用accessToken请求接口的方式(检测到token过期会重试)
     *
     * @param urlString
     * @param method
     * @param data
     * @param token
     * @param remainRetryCnt
     * @return
     */
    String requestWith(String urlString, String method, Object data, String token, Integer remainRetryCnt);

    /**
     * 获取token
     *
     * @param fresh
     * @return
     */
    String getAccessToken(boolean fresh);

    /**
     * 通过企业微信id查询人员信息
     *
     * @param account 企业微信人员id
     * @return null if not found
     */
    JSONObject usersAccount(String account);

    /**
     * 通过七巧人员id查询人员信息
     *
     * @param userId 七巧人员id
     * @return null if not found
     */
    JSONObject usersInfo(String userId);

    /**
     * 根据企业微信部门ID批量获取部门列表
     *
     * @param qwDeptIds 部门id
     * @return
     */
    JSONObject getDepartmentsFromQywxIds(List<String> qwDeptIds);

    JSONObject simpleGetJsonObject(String url);

    JSONArray simpleGetJsonArray(String url);
}
