package org.jeecg.modules.publicManagement.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.jeecg.modules.common.utils.StringUtils.encodeURIComponent;

@Component public class InterfaceUrlConstants {
    private static final String GET_INTERVIEWEE_BY_PHONE_NUMBER = "mysystem/miniapp/getIntervieweeByPhoneNumber";
    private static final String GET_ROLE_KEYS_BY_USER_ID = "mysystem/system/user/getRoleKeys";
    private static final String GET_USER = "mysystem/system/user/getUser";
    private static final String GET_ORG_TREE = "mybiisyn/biiSyn/biiSynOrg/getOrgTree";
    private static final String GET_ORG = "mybiisyn/biiSyn/biiSynOrg/getOrg";
    private static final String GET_USER_LIST = "mysystem/system/user/list";
    private static final String GET_USER_OA_LIST = "mysystem/system/user/listWithOa";
    private static final String GET_USERINFO_LIST = "mysystem/system/user/getUsers";
    private static final String SEND_UNIFIED_AGENDA_MESSAGE = "mypublicdata/wx/message/sendToAgenda";
    private static final String OA_OFS_AGENDA_SEND = "/mypublicdata/oa/ofsAgenda/add";
    private static final String OA_OFS_AGENDA_DONE = "/mypublicdata/oa/ofsAgenda/done";
    private static final String GET_SSO = "/mysystem/system/user/getSSO";
    private static final String GET_ORG_TREE_EMPLOYEE_NUMBER = "/mybiisyn/biiSyn/biiSynOrg/getOrgTreeEmployeeNumber";
    private static String prefix;

    public static String getSSO(String secret, String tokenAlgorithm, String tokenJoinStr, String tokenUpper,
        String username) {
        return prefix + GET_SSO + "?secret=" + secret + "&tokenAlgorithm=" + tokenAlgorithm + "&tokenJoinStr=" + tokenJoinStr + "&tokenUpper=" + tokenUpper + "&username=" + username;
    }

    public static String sendTodo() {
        return prefix + OA_OFS_AGENDA_SEND;
    }

    public static String doneTodo() {
        return prefix + OA_OFS_AGENDA_DONE;
    }

    public static String sendUnifiedAgendaMessage(String account, String title, String description, String url) {
        String paramStr = "?userId=" + encodeURIComponent(account) + "&title=" + encodeURIComponent(
            title) + "&description=" + encodeURIComponent(description) + "&url=" + encodeURIComponent(url);
        String result = prefix + SEND_UNIFIED_AGENDA_MESSAGE + paramStr;
        return result;
    }

    public static String sendApplicationMessage(String appId, String secret, String account, String title,
        String description, String url) {
        final String paramStr = "?userId=" + encodeURIComponent(account) + "&title=" + encodeURIComponent(
            title) + "&description=" + encodeURIComponent(description) + "&url=" + encodeURIComponent(
            url) + "&appId=" + encodeURIComponent(appId) + "&secret=" + encodeURIComponent(secret);
        final String result = prefix + SEND_UNIFIED_AGENDA_MESSAGE + paramStr;
        return result;
    }

    public static String getUserListByDeptId(String deptId) {
        String result = prefix + GET_USER_LIST;
        if (deptId != null) {
            result += "?deptId=" + deptId;
        }
        return result;
    }

    public static String getActiveUserListByDeptId(String deptId) {
        String result = prefix + GET_USER_LIST + "?userType=0";
        if (deptId != null) {
            result += "&deptId=" + deptId;
        }
        return result;
    }

    public static String getActiveUserList() {
        String result = prefix + GET_USER_LIST + "?userType=0";
        return result;
    }

    public static String getActiveOaUserList() {
        String result = prefix + GET_USER_OA_LIST + "?userType=0";
        return result;
    }

    public static String getAllOaUserList() {
        String result = prefix + GET_USER_OA_LIST;
        return result;
    }

    public static String getActiveUserListByNickName(String nickName) {
        String result = prefix + GET_USER_LIST + "?userType=0";
        if (nickName != null) {
            result += "&nickName=" + encodeURIComponent(nickName);
        }
        return result;
    }

    public static String getIntervieweeByPhoneNumber(String phoneNumber) {
        return prefix + GET_INTERVIEWEE_BY_PHONE_NUMBER + "?phoneNumber=" + phoneNumber;
    }

    public static String getRoleKeysByUserId(String userId) {
        return prefix + GET_ROLE_KEYS_BY_USER_ID + "?userId=" + userId;
    }

    public static String findByUsername(String username) {
        return prefix + GET_USER + "?username=" + username;
    }

    public static String findByUser(String param, String value) {
        return prefix + GET_USER + "?" + param + "=" + encodeURIComponent(value);
    }

    public static String findByNickname(String nickName) {
        String result = prefix + GET_USERINFO_LIST;
        if (nickName != null) {
            result += "?nickName=" + encodeURIComponent(nickName);
        }
        return result;
    }

    public static String findByOaId(String oaid) {
        return prefix + GET_USER + "?oaid=" + oaid;
    }

    public static String findById(String id) {
        return prefix + GET_USER + "?id=" + id;
    }

    public static String getOrgTree(String orgId) {
        String result = prefix + GET_ORG_TREE;
        if (orgId != null) {
            result += "?orgId=" + orgId;
        }
        return result;
    }

    public static String getOrgByOrgOaId(String orgOaId) {
        String result = prefix + GET_ORG;
        if (orgOaId != null) {
            result += "?orgOaId=" + orgOaId;
        }
        return result;
    }

    /**
     * 查询部门的上级公司
     *
     * @param orgId
     * @return
     */
    public static String getCompanyByOrgId(String orgId) {
        String result = prefix + GET_ORG;
        if (orgId != null) {
            result += "?orgId=" + orgId;
        }
        return result;
    }

    public static String getOrgTreeEmployeeNumber(String orgId) {
        String result = prefix + GET_ORG_TREE_EMPLOYEE_NUMBER;
        if (orgId != null) {
            result += "?orgId=" + orgId;
        }
        return result;
    }

    public static String findByWxid(String wxid) {
        return prefix + GET_USER + "?wxid=" + wxid;
    }

    @Value("${public_mp.backendUrl}") public void setPrefix(String prefix) {
        InterfaceUrlConstants.prefix = prefix;
    }
}
