package org.jeecg.modules.publicManagement.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;

import java.util.List;

public interface IPublicManagementService {
    String getPublicMpToken(boolean forceRefresh);

    JSONObject getUserInfoByUserName(String username);

    JSONObject getUserInfoByWxid(String wxid);

    JSONObject getUserInfoByPhoneNumber(String phonenumber);

    JSONObject findByNickname(String nickName);

    JSONObject getUserInfoByOaId(String oaId);

    JSONObject getUserInfoById(String id);

    Result<?> getDeptTreeData(final String orgId);

    List<String> getDeptOaIdByOrgId(final String orgId);

    JSONArray getOrgTree(final String orgId);

    List<String> getDeptOaIdListByOrgIdList(final List<String> orgIds);

    Result<?> getRoleKeysByUserId(String userId);

    JSONObject getIntervieweeByPhoneNumber(String phoneNumber);

    JSONObject getOrgByOrgOaId(String orgOaId);

    JSONObject getCompanyByOrgId(String orgId);

    JSONObject getUserListByDeptId(String deptId);

    JSONObject getActiveUserListByDeptId(String deptId);

    JSONObject getUserListByNickName(String nickName);

    JSONObject getActiveUserList();

    JSONObject sendUnifiedAgendaMessage(String account, String title, String description, String url);

    JSONObject sendApplicationMessage(String appId, String secret, String account, String title, String description,
        String url);

    Result<?> getDeptTreeSelect(String orgId);

    JSONObject getActiveOaUserList();

    JSONObject getAllOaUserList();

    JSONObject sendTodo(Integer category, String jumpUrl, String objectId, String parentId, String requestName,
        String workflowName, String nodeName, String systemId, String senderName, String receiverName);

    JSONObject doneTodo(Integer category, String jumpUrl, String objectId, String parentId, String requestName,
        String workflowName, String nodeName, String systemId, String senderName, String receiverName);

    JSONObject getSSO(String secret, String tokenAlgorithm, String tokenJoinStr, String tokenUpper, String username);

    Result<?> getDeptListByOrgId(String orgId, String izSingle);

    Result<?> getDeptVantCascader(String orgId);

    JSONObject getOrgByOrgId(String orgId);

    Result<?> getOrgTreeEmployeeNumber(String orgId);
}
