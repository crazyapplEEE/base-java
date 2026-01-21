package org.jeecg.modules.publicManagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.common.vo.AdvTreeNode;
import org.jeecg.modules.common.vo.TreeSelectVO;
import org.jeecg.modules.common.vo.VantCascaderVO;
import org.jeecg.modules.publicManagement.constants.InterfaceUrlConstants;
import org.jeecg.modules.publicManagement.service.IPublicManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j @Service public class PublicManagementServiceImpl implements IPublicManagementService {
    private final String DEPT_VANT_CASCADER_REDIS_KEY = "DEPT_VANT_CASCADER_REDIS_KEY_";
    private final String EMPLOYEE_NUMBER_REDIS_KEY = "REDIS_EMPLOYEE_NUMBER_";

    @Autowired private RedisUtil redisUtil;

    @Value("${public_mp.deptTreeSelectRedisKey}") private String deptTreeSelectRedisKey;
    @Value("${public_mp.tokenRedisKey}") private String tokenRedisKey;
    @Value("${public_mp.oauthTokenUrl}") private String GET_OAUTH_TOKEN_URL;
    @Value("${public_mp.clientId}") private String clientId;
    @Value("${public_mp.clientSecret}") private String clientSecret;

    @Override public String getPublicMpToken(boolean forceRefresh) {
        if (forceRefresh) {
            return getPublicMpTokenFresh();
        }
        Object object = redisUtil.get(tokenRedisKey);
        if (object == null) {
            return getPublicMpTokenFresh();
        }
        return object.toString();
    }

    @Override public JSONObject getUserInfoByUserName(String username) {
        final String jsonStr = getJson(InterfaceUrlConstants.findByUsername(username));
        if (jsonStr == null || "".equals(jsonStr)) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public JSONObject getUserInfoByWxid(String wxid) {
        final String jsonStr = getJson(InterfaceUrlConstants.findByWxid(wxid));
        if (jsonStr == null || "".equals(jsonStr)) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public JSONObject getUserInfoByPhoneNumber(final String phonenumber) {
        final String jsonStr = getJson(InterfaceUrlConstants.findByUser("phonenumber", phonenumber));
        if (jsonStr == null || "".equals(jsonStr)) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public JSONObject findByNickname(final String nickName) {
        final String jsonStr = getJson(InterfaceUrlConstants.findByNickname(nickName));
        if (jsonStr == null || "".equals(jsonStr)) {
            return null;
        }
        final JSONObject result = new JSONObject();
        result.put("rows", JSONObject.parseArray(jsonStr));
        return result;
    }

    @Override public JSONObject getUserInfoByOaId(final String oaId) {
        final String jsonStr = getJson(InterfaceUrlConstants.findByOaId(oaId));
        if (jsonStr == null || "".equals(jsonStr)) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public JSONObject getUserInfoById(final String id) {
        final String jsonStr = getJson(InterfaceUrlConstants.findById(id));
        if (jsonStr == null || "".equals(jsonStr)) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public Result<?> getDeptTreeData(final String orgId) {
        final String prefix = "[getDeptTreeData] ";
        // 先在redis里面看看有没有
        final String redisKey = orgId == null ? deptTreeSelectRedisKey : deptTreeSelectRedisKey + "_ORG_ID_" + orgId;
        final Object deptTreeSelectObj = redisUtil.get(redisKey);
        if (!ObjectUtils.isEmpty(deptTreeSelectObj)) {
            return Result.OK(deptTreeSelectObj);
        }

        List<AdvTreeNode> result = null;
        try {
            final String json = getJson(InterfaceUrlConstants.getOrgTree(orgId));
            final JSONArray jsonArray = JSONArray.parseArray(json);
            result = convertJsonArray2AdvTreeNodeList(jsonArray);

            if (!CollectionUtils.isEmpty(result)) {
                redisUtil.del(redisKey);
                redisUtil.set(redisKey, result, 60 * 60 * 24); // store for 24 hours
            }
        } catch (Exception e) {
            log.error(prefix + "EXCEPTION CAUGHT" + e.getMessage());
            return Result.error("getDeptTreeData FAILED");
        }
        return Result.OK(result);
    }

    @Override public List<String> getDeptOaIdByOrgId(final String orgId) {
        if (orgId == null) {
            return null;
        }

        // 查找包括自己在内的所有部门OA ID
        final String json = getJson(InterfaceUrlConstants.getOrgTree(orgId));
        final JSONArray jsonArray = JSONArray.parseArray(json);
        return getDeptOaIdListFromJsonArray(jsonArray);
    }

    @Override public JSONArray getOrgTree(final String orgId) {
        if (orgId == null) {
            return null;
        }

        // 查找包括自己在内的所有部门OA ID
        final String json = getJson(InterfaceUrlConstants.getOrgTree(orgId));
        final JSONArray jsonArray = JSONArray.parseArray(json);
        return jsonArray;
    }

    @Override public List<String> getDeptOaIdListByOrgIdList(final List<String> orgIds) {
        if (orgIds == null) {
            return null;
        }

        List<String> result = new ArrayList<>(orgIds.size());
        for (int i = 0; i < orgIds.size(); ++i) {
            final List<String> deptOaIdList = getDeptOaIdByOrgId(orgIds.get(i));
            if (deptOaIdList != null) {
                result.addAll(deptOaIdList);
            }
        }

        return result;
    }

    @Override public Result<?> getRoleKeysByUserId(final String userId) {
        final String prefix = "[getRoleKeysByUserId] ";
        final String jsonStr = getJson(InterfaceUrlConstants.getRoleKeysByUserId(userId));
        Set<String> result = null;
        try {
            result = new ObjectMapper().readValue(jsonStr, new TypeReference<Set<String>>() {
            });
        } catch (Exception e) {
            log.error(prefix + "EXCEPTION CAUGHT: " + e.getMessage());
            return Result.error("SYSTEM ERROR");
        }
        return Result.OK(result);
    }

    @Override public JSONObject getIntervieweeByPhoneNumber(final String phoneNumber) {
        final String jsonStr = getJson(InterfaceUrlConstants.getIntervieweeByPhoneNumber(phoneNumber));
        if (jsonStr == null) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public JSONObject getOrgByOrgOaId(final String orgOaId) {
        final String jsonStr = getJson(InterfaceUrlConstants.getOrgByOrgOaId(orgOaId));
        if (jsonStr == null) {
            return null;
        }
        final JSONArray jsonArray = JSONObject.parseArray(jsonStr);
        if (jsonArray == null || jsonArray.isEmpty()) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonArray.get(0).toString());
        return result;
    }

    @Override public JSONObject getCompanyByOrgId(final String orgId) {
        final String jsonStr = getJson(InterfaceUrlConstants.getCompanyByOrgId(orgId));
        if (jsonStr == null) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public JSONObject getUserListByDeptId(final String deptId) {
        final String jsonStr = getJson(InterfaceUrlConstants.getUserListByDeptId(deptId));
        if (jsonStr == null) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public JSONObject getActiveUserListByDeptId(final String deptId) {
        final String jsonStr = getJson(InterfaceUrlConstants.getActiveUserListByDeptId(deptId));
        if (jsonStr == null) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public JSONObject getUserListByNickName(final String nickName) {
        final String jsonStr = getJson(InterfaceUrlConstants.getActiveUserListByNickName(nickName));
        if (jsonStr == null) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public JSONObject getActiveUserList() {
        final String jsonStr = getJson(InterfaceUrlConstants.getActiveUserList());
        if (jsonStr == null) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public JSONObject sendUnifiedAgendaMessage(String account, String title, String description, String url) {
        final String prefix = "[sendUnifiedAgendaMessage] ";
        log.info(
            prefix + "started! account=" + account + ", title=" + title + ", description=" + description + ", url=" + url);
        final String jsonStr =
            getJson(InterfaceUrlConstants.sendUnifiedAgendaMessage(account, title, description, url));
        if (jsonStr == null) {
            log.error(prefix + "jsonStr IS NULL");
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        log.info(prefix + "finished! result=" + result);
        return result;
    }

    @Override public JSONObject sendApplicationMessage(String appId, String secret, String account, String title,
        String description, String url) {
        final String prefix = "[sendApplicationMessage] ";
        log.info(
            prefix + "started! account=" + account + ", title=" + title + ", description=" + description + ", url=" + url);
        final String jsonStr =
            getJson(InterfaceUrlConstants.sendApplicationMessage(appId, secret, account, title, description, url));
        if (jsonStr == null) {
            log.error(prefix + "jsonStr IS NULL");
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        log.info(prefix + "finished! result=" + result);
        return result;
    }

    @Override public Result<?> getDeptTreeSelect(final String orgId) {
        final String prefix = "[getDeptTreeSelect] ";
        // 先在redis里面看看有没有
        final String redisKey =
            orgId == null ? deptTreeSelectRedisKey : deptTreeSelectRedisKey + "_ORG_ID_Tree_" + orgId;
        final Object deptVantCascaderObj = redisUtil.get(redisKey);
        if (!ObjectUtils.isEmpty(deptVantCascaderObj)) {
            return Result.OK(deptVantCascaderObj);
        }
        List<TreeSelectVO> result = null;
        try {
            final String json = getJson(InterfaceUrlConstants.getOrgTree(orgId));
            final JSONArray jsonArray = JSONArray.parseArray(json);
            result = convertJsonArray2VantCascaderVOList(jsonArray);

            if (!CollectionUtils.isEmpty(result)) {
                redisUtil.del(redisKey);
                redisUtil.set(redisKey, result, 60 * 60 * 24); // store for 24 hours
            }
        } catch (Exception e) {
            log.error(prefix + "EXCEPTION CAUGHT: " + e.getMessage());
            return Result.error("SYSTEM ERROR");
        }
        return Result.OK(result);
    }

    @Override public JSONObject getActiveOaUserList() {
        final String jsonStr = getJson(InterfaceUrlConstants.getActiveOaUserList());
        if (jsonStr == null) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public JSONObject getAllOaUserList() {
        final String jsonStr = getJson(InterfaceUrlConstants.getAllOaUserList());
        if (jsonStr == null) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override
    public JSONObject sendTodo(Integer category, String jumpUrl, String objectId, String parentId, String requestName,
        String workflowName, String nodeName, String systemId, String senderName, String receiverName) {
        Map<String, Object> entity =
            getTodoEntity(category, jumpUrl, objectId, parentId, requestName, workflowName, nodeName, systemId,
                senderName, receiverName);
        log.info("--------------------------------");
        log.info("entity: ");
        log.info(entity.toString());
        final String jsonStr = httpPostEntity("发送待办", entity, InterfaceUrlConstants.sendTodo());
        if (jsonStr == null) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override
    public JSONObject doneTodo(Integer category, String jumpUrl, String objectId, String parentId, String requestName,
        String workflowName, String nodeName, String systemId, String senderName, String receiverName) {
        Map<String, Object> entity =
            getTodoEntity(category, jumpUrl, objectId, parentId, requestName, workflowName, nodeName, systemId,
                senderName, receiverName);
        log.info("--------------------------------");
        log.info("entity: ", entity);
        final String jsonStr = httpPostEntity("已办待办", entity, InterfaceUrlConstants.doneTodo());
        if (jsonStr == null) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public JSONObject getSSO(String secret, String tokenAlgorithm, String tokenJoinStr, String tokenUpper,
        String username) {
        final String jsonStr =
            getJson(InterfaceUrlConstants.getSSO(secret, tokenAlgorithm, tokenJoinStr, tokenUpper, username));
        if (jsonStr == null) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonStr);
        return result;
    }

    @Override public Result<?> getDeptVantCascader(final String orgId) {
        final String prefix = "[getDeptVantCascader] ";

        // 先在redis里面看看有没有
        final String redisKey =
            orgId == null ? DEPT_VANT_CASCADER_REDIS_KEY : DEPT_VANT_CASCADER_REDIS_KEY + "_ORG_ID_" + orgId;
        final Object deptVantCascaderObj = redisUtil.get(redisKey);
        if (!ObjectUtils.isEmpty(deptVantCascaderObj)) {
            return Result.OK(deptVantCascaderObj);
        }

        List<VantCascaderVO> result = null;
        try {
            final String json = getJson(InterfaceUrlConstants.getOrgTree(orgId));
            final JSONArray jsonArray = JSONArray.parseArray(json);
            result = convertJsonArray2VantCascaderVOListSingle(jsonArray);

            if (!CollectionUtils.isEmpty(result)) {
                redisUtil.del(redisKey);
                redisUtil.set(redisKey, result, 60 * 60 * 24); // store for 24 hours
            }
        } catch (Exception e) {
            log.error(prefix + "EXCEPTION CAUGHT" + e.getMessage());
            return Result.error("SYSTEM ERROR");
        }
        return Result.OK(result);
    }

    @Override public Result<?> getDeptListByOrgId(final String orgId, final String izSingle) {
        final String prefix = "[getDeptListByOrgId] ";
        List<TreeSelectVO> result = null;
        try {
            final String json = getJson(InterfaceUrlConstants.getOrgTree(orgId));
            final JSONArray jsonArray = JSONArray.parseArray(json);
            result = convertJsonArray2VantCascaderVOList(jsonArray);
        } catch (Exception e) {
            log.error(prefix + "EXCEPTION CAUGHT" + e.getMessage());
            return Result.error("SYSTEM ERROR");
        }

        List<String> deptList = new ArrayList<>();
        TreeSelectVO treeSelectVO = result.get(0);
        if ("0".equals(izSingle)) {
            deptList = getChildDeptList(deptList, treeSelectVO);
        } else {
            List<TreeSelectVO> child = treeSelectVO.getChildren();
            for (TreeSelectVO t : child) {
                if (t.getState() != null && "1".equals(t.getState())) {
                    if (t.getId() != null && !"".equals(t.getId())) {
                        deptList.add(t.getId());
                    }
                }
            }
        }

        return Result.OK(deptList);
    }

    @Override public JSONObject getOrgByOrgId(final String orgId) {
        final String jsonStr = getJson(InterfaceUrlConstants.getCompanyByOrgId(orgId));
        if (jsonStr == null) {
            return null;
        }
        final JSONArray jsonArray = JSONObject.parseArray(jsonStr);
        if (jsonArray == null || jsonArray.isEmpty()) {
            return null;
        }
        final JSONObject result = JSONObject.parseObject(jsonArray.get(0).toString());
        return result;
    }

    // @TODO: 不同部门相同人员会重复计算，需要优化
    @Override public Result<?> getOrgTreeEmployeeNumber(final String orgId) {
        final String redisKey =
            orgId == null ? EMPLOYEE_NUMBER_REDIS_KEY : EMPLOYEE_NUMBER_REDIS_KEY + "_ORG_ID_" + orgId;
        final Object employeeNumberObj = redisUtil.get(redisKey);
        if (!ObjectUtils.isEmpty(employeeNumberObj)) {
            return Result.OK(employeeNumberObj);
        }

        // 访问下属部门
        final Result<?> treeResult = getDeptTreeData(orgId);
        if (!treeResult.isSuccess()) {
            return treeResult;
        }

        List<?> advTreeNodeList = new ObjectMapper().convertValue(treeResult.getResult(), List.class);
        Integer employeeNumber = 0;
        while (advTreeNodeList != null && !advTreeNodeList.isEmpty()) {
            List<AdvTreeNode> advTreeNodeListNew = new ArrayList<>(advTreeNodeList.size() * 10);
            for (int i = 0; i < advTreeNodeList.size(); ++i) {
                final AdvTreeNode advTreeNode =
                    new ObjectMapper().convertValue(advTreeNodeList.get(i), AdvTreeNode.class);

                if (advTreeNode == null) {
                    continue;
                }

                final String jsonStr = getJson(InterfaceUrlConstants.getOrgTreeEmployeeNumber(advTreeNode.getKey()));
                if (jsonStr == null) {
                    return null;
                }
                final JSONArray jsonArray = JSONObject.parseArray(jsonStr);
                if (jsonArray == null || jsonArray.isEmpty()) {
                    return null;
                }
                final JSONObject result = JSONObject.parseObject(jsonArray.get(0).toString());

                final Integer curEmployeeNumber = result.getInteger("employeeNumber");
                if (curEmployeeNumber != null) {
                    employeeNumber += curEmployeeNumber;
                }

                final List<AdvTreeNode> children = advTreeNode.getChildren();
                if (!CollectionUtils.isEmpty(children)) {
                    advTreeNodeListNew.addAll(children);
                }
            }

            advTreeNodeList = advTreeNodeListNew;
        }

        redisUtil.del(redisKey);
        redisUtil.set(redisKey, employeeNumber, 60 * 60 * 24); // store for 24 hours
        return Result.OK(employeeNumber);
    }

    /**
     * 使用Token请求接口的方式
     */
    private String getJson(final String urlString) {
        final String prefix = "[getJson] ";
        log.info(prefix + "started with urlString=" + urlString);

        StringBuffer sb = null;
        InputStream inputStream;
        HttpURLConnection httpURLConnection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            // 设置连接网络的超时时间
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setReadTimeout(60000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("GET");
            // token: 采用OAuth2.0的授权方式，注意prefix
            httpURLConnection.setRequestProperty("Authorization", getPublicMpToken(false));

            // Already connected error: https://www.jianshu.com/p/ef2918e98135
            // httpURLConnection.connect();

            int responseCode = httpURLConnection.getResponseCode();
            //            if (responseCode != 200) {
            //                httpURLConnection.disconnect();
            //                httpURLConnection.setConnectTimeout(15000);
            //                httpURLConnection.setReadTimeout(60000);
            //                httpURLConnection.setDoInput(true);
            //                httpURLConnection.setRequestMethod("GET");
            //                httpURLConnection.setRequestProperty("Authorization", getPublicMpToken(true));
            //                // httpURLConnection.connect();
            //            }
            //
            //            responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                // 从服务器获得一个输入流
                inputStream = httpURLConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String lines;
                sb = new StringBuffer();
                while ((lines = reader.readLine()) != null) {
                    sb.append(lines);
                }
            } else {
                log.error("Url: " + urlString + " response code " + responseCode);
            }
        } catch (Exception e) {
            log.error(prefix + "EXCEPTION CAUGHT" + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error(prefix + "EXCEPTION CAUGHT" + e.getMessage());
            }

            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        return sb == null ? null : sb.toString();
    }

    private String getPublicMpTokenFresh() {
        final String prefix = "[getPublicMpTokenFresh] ";
        redisUtil.del(tokenRedisKey);
        // 获取TOKEN
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(GET_OAUTH_TOKEN_URL);
        RequestConfig requestConfig =
            RequestConfig.custom().setConnectTimeout(60000).setConnectionRequestTimeout(60000).setSocketTimeout(60000)
                .build();
        httpPost.setConfig(requestConfig);

        MultipartEntityBuilder multipartEntityBuilder =
            MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .setCharset(StandardCharsets.UTF_8).addTextBody("client_id", clientId)
                .addTextBody("client_secret", clientSecret);

        String result = "";
        try {
            httpPost.setEntity(multipartEntityBuilder.build());
            CloseableHttpResponse response = client.execute(httpPost);
            // 接收返回响应二进制流信息(InputStream)
            HttpEntity entity = response.getEntity();
            // 将流信息转换成字符串  设置编码为UTF-8防止乱码
            String entityStr = EntityUtils.toString(entity, Consts.UTF_8);
            JSONObject entityJson = JSON.parseObject(entityStr);
            if (entityJson == null) {
                return result;
            }

            final Object code = entityJson.get("code");
            if (code != null && code.equals(0)) {
                // 成功
                String resultData = entityJson.get("data").toString();
                String token = JSON.parseObject(resultData).get("access_token").toString();
                result = "Bearer " + token;
                redisUtil.set(tokenRedisKey, result, 60);
            }
        } catch (Exception e) {
            log.error(prefix + "EXCEPTION CAUGHT: " + e.getMessage());
        }

        return result;
    }

    private TreeSelectVO convertJsonObject2VantCascaderVO(final JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }

        final String orgId = jsonObject.getString("orgId");
        if (orgId == null) {
            return null;
        }

        final String companyTpye = jsonObject.getString("companyTpye");
        if ("2".equals(companyTpye)) {
            // 壳公司
            return null;
        }

        final String orgCode = jsonObject.getString("orgCode");
        final String fullName = jsonObject.getString("fullName");
        final String orgType = jsonObject.getString("orgType");
        final JSONArray childTree = jsonObject.getJSONArray("childTree");
        //        String[] names = fullName.split("-");
        //        final String lastName = names[names.length - 1];
        TreeSelectVO result = new TreeSelectVO().setId(orgId).setLabel(fullName).setIcon(orgCode)
            .setChildren(convertJsonArray2VantCascaderVOList(childTree)).setState(orgType);

        return result;
    }

    private List<TreeSelectVO> convertJsonArray2VantCascaderVOList(final JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }

        List<TreeSelectVO> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); ++i) {
            final JSONObject jsonObject = JSONObject.parseObject(jsonArray.get(i).toString());

            final TreeSelectVO treeSelectVO = convertJsonObject2VantCascaderVO(jsonObject);
            if (treeSelectVO != null) {
                result.add(treeSelectVO);
            }
        }

        result.sort((o1, o2) -> {
            if (o1 == null) {
                return -1;
            }

            if (o2 == null) {
                return -1;
            }

            if (o1.getId() == null) {
                return -1;
            }

            if (o2.getId() == null) {
                return -1;
            }

            return o1.getId().compareTo(o2.getId());
        });

        return result.isEmpty() ? null : result;
    }

    private Map<String, Object> getTodoEntity(Integer category, String jumpUrl, String objectId, String parentId,
        String requestName, String workflowName, String nodeName, String systemId, String senderName,
        String receiverName) {
        Map<String, Object> entity = new HashMap<>();
        entity.put("category", category);
        entity.put("jumpUrl", jumpUrl);
        entity.put("objectId", objectId);
        entity.put("parentId", parentId);
        entity.put("requestName", requestName);
        entity.put("workflowName", workflowName);
        entity.put("nodeName", nodeName);
        entity.put("systemId", systemId);
        entity.put("senderName", senderName);
        entity.put("receiverName", receiverName);
        return entity;
    }

    private List<String> getChildDeptList(List<String> deptList, TreeSelectVO treeSelectVO) {
        List<TreeSelectVO> child = treeSelectVO.getChildren();
        if (child == null) {
            return deptList;
        }
        for (TreeSelectVO t : child) {
            if (t.getState() != null && "1".equals(t.getState())) {
                if (t.getId() != null && !"".equals(t.getId())) {
                    deptList.add(t.getId());
                }
            }
            deptList = getChildDeptList(deptList, t);
        }
        return deptList;
    }

    private VantCascaderVO convertJsonObject2VantCascaderVOSingle(final JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }

        final String orgId = jsonObject.getString("orgId");
        if (orgId == null) {
            return null;
        }

        final String companyTpye = jsonObject.getString("companyTpye");
        if ("2".equals(companyTpye)) {
            // 壳公司
            return null;
        }

        final String orgCode = jsonObject.getString("orgCode");
        final String fullName = jsonObject.getString("fullName");
        final JSONArray childTree = jsonObject.getJSONArray("childTree");
        VantCascaderVO result = new VantCascaderVO().setOrgCode(orgCode).setText(fullName).setValue(orgId)
            .setChildren(convertJsonArray2VantCascaderVOListSingle(childTree));

        return result;
    }

    private List<VantCascaderVO> convertJsonArray2VantCascaderVOListSingle(final JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }

        List<VantCascaderVO> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); ++i) {
            final JSONObject jsonObject = JSONObject.parseObject(jsonArray.get(i).toString());

            final VantCascaderVO cascaderVO = convertJsonObject2VantCascaderVOSingle(jsonObject);
            if (cascaderVO != null) {
                result.add(cascaderVO);
            }
        }

        result.sort((o1, o2) -> {
            if (o1 == null) {
                return -1;
            }

            if (o2 == null) {
                return -1;
            }

            if (o1.getOrgCode() == null) {
                return -1;
            }

            if (o2.getOrgCode() == null) {
                return -1;
            }

            return o1.getOrgCode().compareTo(o2.getOrgCode());
        });

        return result.isEmpty() ? null : result;
    }

    private String getResult(String functionDesc, HttpGet httpGet, HttpPost httpPost) throws IOException {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        CloseableHttpResponse closeableHttpResponse = null;
        if (httpGet != null) {
            closeableHttpResponse = closeableHttpClient.execute(httpGet);
        } else {
            closeableHttpResponse = closeableHttpClient.execute(httpPost);
        }
        HttpEntity reEntity = closeableHttpResponse.getEntity();
        String resultStr = EntityUtils.toString(reEntity);
        JSONObject result = JSON.parseObject(resultStr);
        if (result != null) {
            if ((result.containsKey("code") && result.get("code").equals(401)) || (result.containsKey(
                "status") && result.get("status").equals(401)) || (result.containsKey("error") && "invalid_token"
                .equals(result.get("status"))) || (result.containsKey("error") && "invalid_token"
                .equals(result.get("error")))) {
                // 权限不足或者token过期
                log.error("httpGet: " + functionDesc + " | " + "401");
                return null;
            } else if ((result.containsKey("code") && result.get("code").equals(500)) || (result.containsKey(
                "status") && result.get("status").equals(500))) {
                log.error("httpGet: " + functionDesc + " | " + "500");
                return null;
            } else {
                return result.toString();
            }
        } else {
            log.error("httpGet: " + functionDesc + " | " + "null");
            return null;
        }
    }

    private String httpPostEntity(String functionDesc, Map<String, Object> entityMap, String requestUrl) {
        String entityJson = JSONObject.toJSONString(entityMap);
        HttpPost httpPost = new HttpPost(requestUrl); // 设置响应头信息
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.addHeader("Authorization", getPublicMpToken(false));
        RequestConfig requestConfig =
            RequestConfig.custom().setConnectTimeout(60000).setConnectionRequestTimeout(60000).setSocketTimeout(60000)
                .build();
        httpPost.setConfig(requestConfig);
        httpPost.setEntity(new StringEntity(entityJson, "UTF-8"));
        try {
            return getResult(functionDesc, null, httpPost);
        } catch (Exception e) {
            log.error("httpPost: " + functionDesc + " | " + e.getMessage());
            return null;
        }
    }

    private List<String> getDeptOaIdListFromJsonObject(final JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }

        List<String> result = new ArrayList<>();
        final String orgType = jsonObject.getString("orgType");
        if (orgType != null && "1".equals(orgType)) {
            // 只有该机构是【部门】时，才加入自身OA ID
            final Integer orgOaId = jsonObject.getInteger("orgOaId");
            if (orgOaId != null) {
                result.add(orgOaId.toString());
            }
        }

        // 加入下属部门的ID
        final JSONArray childTree = jsonObject.getJSONArray("childTree");
        final List<String> childDeptOaIdList = getDeptOaIdListFromJsonArray(childTree);
        if (childDeptOaIdList != null) {
            result.addAll(childDeptOaIdList);
        }

        return result;
    }

    private List<String> getDeptOaIdListFromJsonArray(final JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }

        List<String> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); ++i) {
            final JSONObject jsonObject = jsonArray.getJSONObject(i);
            List<String> deptOaIdList = getDeptOaIdListFromJsonObject(jsonObject);
            if (deptOaIdList != null) {
                result.addAll(deptOaIdList);
            }
        }

        return result;
    }

    private AdvTreeNode convertJsonObject2AdvTreeNode(final JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }

        final String orgId = jsonObject.getString("orgId");
        if (orgId == null) {
            return null;
        }

        final String orgCode = jsonObject.getString("orgCode");
        final String fullName = jsonObject.getString("fullName");
        final JSONArray childTree = jsonObject.getJSONArray("childTree");
        AdvTreeNode result = new AdvTreeNode().setOrgCode(orgCode).setKey(orgId).setValue(orgId).setTitle(fullName)
            .setChildren(convertJsonArray2AdvTreeNodeList(childTree));

        return result;
    }

    private List<AdvTreeNode> convertJsonArray2AdvTreeNodeList(final JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }

        List<AdvTreeNode> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); ++i) {
            final JSONObject jsonObject = JSONObject.parseObject(jsonArray.get(i).toString());

            final AdvTreeNode advTreeNode = convertJsonObject2AdvTreeNode(jsonObject);
            if (advTreeNode != null) {
                result.add(advTreeNode);
            }
        }

        Collections.sort(result, (o1, o2) -> {
            if (o1 == null) {
                return -1;
            }

            if (o2 == null) {
                return -1;
            }

            if (o1.getOrgCode() == null) {
                return -1;
            }

            if (o2.getOrgCode() == null) {
                return -1;
            }

            return o1.getOrgCode().compareTo(o2.getOrgCode());
        });

        return result.isEmpty() ? null : result;
    }
}
