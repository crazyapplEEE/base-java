package org.jeecg.modules.publicManagement.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.common.constant.ApplicationProfile;
import org.jeecg.modules.publicManagement.service.IPublicManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "支撑-公共管理平台") @RestController @RequestMapping("/publicManagement") @Slf4j
public class PublicManagementController {
    @Autowired private IPublicManagementService publicManagementService;
    @Value("${spring.profiles.active}") private String profile;

    @ApiOperation("获取token") @GetMapping("getToken") public Result<String> getToken() {
        if (!ApplicationProfile.DEV.equals(profile)) {
            return null;
        }

        final String prefix = "[getToken] ";
        log.info(prefix + "started");
        final String result = publicManagementService.getPublicMpToken(false);
        log.info(prefix + "ended with result=" + result);
        return Result.OK(result);
    }

    @ApiOperation("通过手机号获取访问者信息") @GetMapping("getIntervieweeByPhoneNumber")
    public JSONObject getIntervieweeByPhoneNumber(@RequestParam("phoneNumber") String phoneNumber) {
        if (!ApplicationProfile.DEV.equals(profile)) {
            return null;
        }

        final String prefix = "[getIntervieweeByPhoneNumber] ";
        log.info(prefix + "started with phoneNumber=" + phoneNumber);
        final JSONObject result = publicManagementService.getIntervieweeByPhoneNumber(phoneNumber);
        log.info(prefix + "ended with result=" + result);
        return result;
    }

    @ApiOperation("通过公共管理平台id获取用户信息") @GetMapping("getUserInfoById")
    public Result<?> getUserInfoById(@RequestParam("id") String id) {
        if (!ApplicationProfile.DEV.equals(profile)) {
            return null;
        }
        final String prefix = "[getUserInfoById] ";
        log.info(prefix + "started with id=" + id);
        final JSONObject result = publicManagementService.getUserInfoById(id);
        if (result == null) {
            log.error(prefix + "CANNOT FIND USER WITH ID " + id);
            return Result.error("系统访问量太大，请您稍候重试");
        }
        log.info(prefix + "ended with result=" + result);
        return Result.OK(result);
    }

    @ApiOperation("通过OA账户loginid获取用户信息") @GetMapping("getUserInfoByUserName")
    public Result<?> getUserInfoByUserName(@RequestParam("userName") String userName) {
        if (!ApplicationProfile.DEV.equals(profile)) {
            return null;
        }
        final String prefix = "[getUserInfoByUserName] ";
        log.info(prefix + "started with userName=" + userName);
        final JSONObject result = publicManagementService.getUserInfoByUserName(userName);
        if (result == null) {
            log.error(prefix + "CANNOT FIND USER WITH userName " + userName);
            return Result.error("系统访问量太大，请您稍候重试");
        }
        // 删除敏感信息
        final List<String> sensitiveKeyList = Arrays.asList("personalId", "phonenumber");
        sensitiveKeyList.forEach(result::remove);
        log.info(prefix + "ended with result=" + result);
        return Result.OK(result);
    }

    @ApiOperation("通过OA账户id获取用户信息") @GetMapping("getUserInfoByOaId")
    public Result<?> getUserInfoByOaId(@RequestParam("oaId") String oaId) {
        if (!ApplicationProfile.DEV.equals(profile)) {
            return null;
        }
        final String prefix = "[getUserInfoByOaId] ";
        log.info(prefix + "started with userName=" + oaId);
        final JSONObject result = publicManagementService.getUserInfoByOaId(oaId);
        if (result == null) {
            log.error(prefix + "CANNOT FIND USER WITH OA ID " + oaId);
            return Result.error("系统访问量太大，请您稍候重试");
        }
        log.info(prefix + "ended with result=" + result);
        return Result.OK(result);
    }

    @ApiOperation("通过OA账户获取用户信息") @GetMapping("getUserInfo")
    public Result<?> getUserInfo(@RequestParam(value = "userName", required = false) String userName,
        @RequestParam(value = "oaId", required = false) String oaId) {
        if (!ApplicationProfile.DEV.equals(profile)) {
            return null;
        }
        final String prefix = "[getUserInfo] ";
        log.info(prefix + "started with userName=" + userName + "， oaId=" + oaId);
        JSONObject result = null;
        if (!ObjectUtils.isEmpty(userName)) {
            result = publicManagementService.getUserInfoByUserName(userName);
            if (result == null) {
                log.error(prefix + "CANNOT FIND USER WITH USERNAME " + userName);
            }
        } else if (!ObjectUtils.isEmpty(oaId)) {
            result = publicManagementService.getUserInfoByOaId(oaId);
            if (result == null) {
                log.error(prefix + "CANNOT FIND USER WITH OA ID " + oaId);
            }
        } else {
            log.error(prefix + "INPUT IS NULL");
        }

        if (result == null) {
            return Result.error("系统访问量太大，请您稍候重试");
        }

        log.info(prefix + "ended with result=" + result);
        return Result.OK(result);
    }

    @ApiOperation("通过OA账户获取用户信息(只返回必要信息)") @GetMapping("checkUser")
    public Result<?> checkUser(@RequestParam(value = "userName") String userName) {
        if (!ApplicationProfile.DEV.equals(profile)) {
            return null;
        }
        final String prefix = "[checkUser] ";
        log.info(prefix + "started with userName=" + userName);

        JSONObject result = null;
        if (!ObjectUtils.isEmpty(userName)) {
            result = publicManagementService.getUserInfoByUserName(userName);
            if (result == null) {
                // 尝试用手机号登录系统
                result = publicManagementService.getUserInfoByPhoneNumber(userName);
                if (result == null) {
                    log.error(prefix + "CANNOT FIND USER WITH USERNAME " + userName);
                    return Result.error("系统访问量太大，请您稍候重试");
                }
            }
        } else {
            log.error(prefix + "userName IS NULL");
            return Result.error("系统正在升级，请稍后再试");
        }
        Map<String, Object> minimumResult = new HashMap<>();
        minimumResult.put("userType", result.getString("userType"));
        minimumResult.put("userId", result.getString("userId"));
        minimumResult.put("account", result.getString("account"));
        log.info(prefix + "ended with result=" + minimumResult);
        return Result.OK(minimumResult);
    }

    @ApiOperation("通过用户ID获取角色键值") @GetMapping("getRoleKeysByUserId")
    public Result<?> getRoleKeysByUserId(@RequestParam("userId") String userId) {
        final String prefix = "[getRoleKeysByUserId] ";
        log.info(prefix + "started with userId=" + userId);
        final Result<?> result = publicManagementService.getRoleKeysByUserId(userId);
        log.info(prefix + "ended with result=" + result);
        return result;
    }

    @ApiOperation("获取AntDesignVue的TreeData格式的部门信息") @GetMapping("getDeptTreeData")
    public Result<?> getDeptTreeData(@RequestParam(value = "orgId", required = false) String orgId) {
        final String prefix = "[getDeptTreeData] ";
        log.info(prefix + "started with orgId=" + orgId);
        final Result<?> result = publicManagementService.getDeptTreeData(orgId);
        log.info(prefix + "ended with result=" + result);
        return result;
    }

    @ApiOperation("根据部门ID获取成员列表") @GetMapping("getUserListByDeptId")
    public Result<?> getUserListByDeptId(@RequestParam(value = "deptId", required = false) String deptId) {
        if (!ApplicationProfile.DEV.equals(profile)) {
            return null;
        }
        final String prefix = "[getUserListByDeptId] ";
        log.info(prefix + "started with deptId=" + deptId);
        final JSONObject userList = publicManagementService.getUserListByDeptId(deptId);
        if (userList == null) {
            log.error(prefix + "CANNOT FIND USER LIST WITH DEPT ID " + deptId);
            return Result.error("系统访问量太大，请您稍候重试");
        }
        log.info(prefix + "ended with result=" + userList);
        return Result.OK(userList);
    }

    @ApiOperation("根据姓名获取成员列表(不含oaid)") @GetMapping("getUserListByNickName")
    public Result<?> getUserListByNickName(@RequestParam(value = "nickName", required = false) String nickName) {
        if (!ApplicationProfile.DEV.equals(profile)) {
            return null;
        }
        final String prefix = "[getUserListByNickName] ";
        log.info(prefix + "started with nickName=" + nickName);
        final JSONObject userList = publicManagementService.getUserListByNickName(nickName);
        if (userList == null) {
            log.error(prefix + "CANNOT FIND USER LIST WITH NICKNAME " + nickName);
            return Result.error("系统访问量太大，请您稍候重试");
        }
        log.info(prefix + "ended with result=" + userList);
        return Result.OK(userList);
    }

    @ApiOperation("根据姓名获取成员列表(含oaid)") @GetMapping("findByNickname")
    public Result<?> findByNickname(@RequestParam(value = "nickName", required = false) String nickName) {
        if (!ApplicationProfile.DEV.equals(profile)) {
            return null;
        }
        final String prefix = "[findByNickname] ";
        log.info(prefix + "started with nickName=" + nickName);
        final JSONObject userList = publicManagementService.findByNickname(nickName);
        if (userList == null) {
            log.error(prefix + "CANNOT FIND USER LIST WITH NICKNAME " + nickName);
            return Result.error("系统访问量太大，请您稍候重试");
        }
        log.info(prefix + "ended with result=" + userList);
        return Result.OK(userList);
    }

    @ApiOperation("获取所有在职人员") @GetMapping("getActiveUserList") public Result<?> getActiveUserList() {
        if (!ApplicationProfile.DEV.equals(profile)) {
            return null;
        }
        final String prefix = "[getActiveUserList] ";
        log.info(prefix + "started");
        final JSONObject userList = publicManagementService.getActiveUserList();
        if (userList == null) {
            log.error(prefix + "CANNOT FIND USER LIST");
            return Result.error("系统访问量太大，请您稍候重试");
        }
        log.info(prefix + "ended");
        return Result.OK(userList);
    }

    @ApiOperation("获取部门多级多选信息") @GetMapping("getDeptTreeSelect") public Result<?> getDeptTreeSelect() {
        final String prefix = "[getDeptTreeSelect] ";
        log.info(prefix + "started");
        final String orgId = "7f304de353c358egcfg267f023db8f25";
        final Result<?> result = publicManagementService.getDeptTreeSelect(orgId);
        log.info(prefix + "ended with result=" + result);
        return result;
    }

    @ApiOperation("获取公司部门OrgId列表") @GetMapping("getDeptListByOrgId")
    public Result<?> getDeptListByOrgId(@RequestParam(value = "orgId", required = false) String orgId,
        @RequestParam(value = "izSingle", required = false) String izSingle) {
        final String prefix = "[getDeptListByOrgId] ";
        log.info(prefix + "started with orgId=" + orgId + " and izSingle=" + izSingle);

        // izSingle=1:不包含下级单位的部门；0：包含下级单位的部门；
        final Result<?> result = publicManagementService.getDeptListByOrgId(orgId, izSingle);
        log.info(prefix + "ended with result=" + result);
        return result;
    }

    @ApiOperation("获取vant的Cascader格式的部门信息") @GetMapping("getDeptVantCascader")
    public Result<?> getDeptVantCascader(@RequestParam(value = "orgId", required = false) String orgId) {
        final String prefix = "[getDeptVantCascader] ";
        log.info(prefix + "started with orgId=" + orgId);
        final Result<?> result = publicManagementService.getDeptVantCascader(orgId);
        log.info(prefix + "ended with result=" + result);
        return result;
    }

    @ApiOperation("根据机构ID获取机构信息") @GetMapping("getOrgByOrgId")
    public Result<?> getOrgByOrgId(@RequestParam("orgId") String orgId) {
        final String prefix = "[getOrgByOrgId] ";
        log.info(prefix + "started with orgId=" + orgId);
        final JSONObject result = publicManagementService.getOrgByOrgId(orgId);
        if (result == null) {
            log.error(prefix + "CANNOT FIND ORG WITH ORG ID " + orgId);
            return Result.error("系统访问量太大，请您稍候重试");
        }
        log.info(prefix + "ended with result=" + result);
        return Result.OK(result);
    }

    @ApiOperation("通过orgId获取所有在职人数") @GetMapping("getOrgTreeEmployeeNumber")
    public Result<?> getOrgTreeEmployeeNumber(@RequestParam("orgId") String orgId) {
        final String prefix = "[getOrgTreeEmployeeNumber] ";
        log.info(prefix + "started with orgId=" + orgId);
        final Result<?> result = publicManagementService.getOrgTreeEmployeeNumber(orgId);
        log.info(prefix + "ended with result=" + result);
        return Result.OK(result);
    }
}
