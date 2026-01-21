package org.jeecg.modules.system.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysTokenUser;
import org.jeecg.common.util.MD5Util;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.encryption.EncryptedString;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.common.constant.ApplicationProfile;
import org.jeecg.modules.publicManagement.service.IPublicManagementService;
import org.jeecg.modules.qiqiao.service.IQiqiaoService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SsoLoginModel;
import org.jeecg.modules.system.model.SysLoginModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecg.modules.system.service.ISysLogService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.common.utils.EncryptionUtils;
import org.jeecg.modules.system.util.RandImageUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @Author scott
 * @since 2018-12-17
 */
@RestController @RequestMapping("/sys") @Api(tags = "JEECG-用户登录") @Slf4j public class LoginController {
    private static final String BASE_CHECK_CODES = "qwertyuiplkjhgfdsazxcvbnmQWERTYUPLKJHGFDSAZXCVBNM1234567890";
    @Autowired private ISysUserService sysUserService;
    @Autowired private ISysBaseAPI sysBaseAPI;
    @Autowired private ISysLogService logService;
    @Autowired private RedisUtil redisUtil;
    @Autowired private ISysDepartService sysDepartService;
    @Autowired private ISysDictService sysDictService;
    @Resource private BaseCommonService baseCommonService;
    @Autowired private IPublicManagementService publicManagementService;
    @Autowired private IQiqiaoService qiqiaoService;
    @Value("${spring.profiles.active}") private String profile;
    @Value("${spring.application.cnName}") private String applicationCnName;
    @Value("${sys.loginUrl}") private String loginUrl;
    @Value("${sys.biiLoginUrl}") private String biiLoginUrl;
    @Value("${sys.bjmoaLoginUrl}") private String bjmoaLoginUrl;

    @GetMapping(value = "/redirectSso") @ApiOperation("统一认证重定向")
    public void redirectSso(HttpServletRequest request, HttpServletResponse response) {
        final String prefix = "[redirectSso] ";
        log.info(prefix + "started");

        try {
            final Cookie[] cookies = request.getCookies();
            log.info(prefix + "cookies " + Arrays.toString(cookies));

            if (ApplicationProfile.DEV.equals(profile)) {
                String loginid = null;
                String oaId = null;
                if (cookies != null) {
                    for (final Cookie cookie : cookies) {
                        if ("loginidweaver".equals(cookie.getName())) {
                            // 正式统一认证
                            oaId = cookie.getValue();
                            break;
                        } else if ("username".equals(cookie.getName())) {
                            // 测试统一认证
                            loginid = cookie.getValue();
                            break;
                        }
                    }

                }
                log.info("oaId: " + oaId + ", loginid: " + loginid);
                if (StringUtils.isEmpty(oaId) && StringUtils.isEmpty(loginid)) {
                    return;
                }
                if (loginid == null) {
                    final JSONObject userInfo = publicManagementService.getUserInfoByOaId(oaId);
                    if (userInfo == null) {
                        log.warn(prefix + "userInfo IS NULL");
                        return;
                    }
                    loginid = userInfo.getString("userName");
                    if (loginid == null) {
                        log.warn(prefix + "loginid IS NULL");
                        return;
                    }
                    final String stamp = String.valueOf(new Date().getTime());
                    final String token = EncryptionUtils.hexSHA1(EncryptionUtils.BII_SSO_SECRET + loginid + stamp);
                    response.sendRedirect(String.format(loginUrl, loginid, stamp, token));
                }

            } else {
                String qiqiaoUserId = null;
                String zhiduCompany = null;
                if (cookies != null) {
                    for (final Cookie cookie : cookies) {
                        final String cookieName = cookie.getName();
                        if ("QIQIAO_USER_ID".equals(cookieName)) {
                            qiqiaoUserId = cookie.getValue();
                        } else if ("ZHIDU_COMPANY".equalsIgnoreCase(cookieName)) {
                            zhiduCompany = cookie.getValue();
                        }
                    }
                }
                log.info("zhiduCompany: " + zhiduCompany + ", qiqiaoUserId: " + qiqiaoUserId);

                if (StringUtils.isEmpty(qiqiaoUserId) || StringUtils.isEmpty(zhiduCompany)) {
                    return;
                }

                if ("BII".equalsIgnoreCase(zhiduCompany)) {
                    response.sendRedirect(String.format(biiLoginUrl, qiqiaoUserId));
                } else if ("BJMOA".equalsIgnoreCase(zhiduCompany)) {
                    response.sendRedirect(String.format(bjmoaLoginUrl, qiqiaoUserId));
                }
            }
        } catch (Exception e) {
            log.error(prefix + "EXCEPTION CAUGHT: " + e.getMessage());
        }
    }

    @PostMapping(value = "/debugLogin") @ApiOperation("调试登录接口")
    public Result<?> debugLogin(@RequestBody SsoLoginModel ssoLoginModel) {
        if (!ApplicationProfile.DEV.equals(profile)) {
            return Result.error("登录失败！");
        }

        Result<?> result = new Result<>();
        final String username = ssoLoginModel.getLoginid();
        final String name = "sysadmin".equals(username) ? "admin" : "bii";
        SysUser sysUser = sysUserService.getUserByName(name);
        result = sysUserService.checkUserIsEffective(sysUser);
        if (!result.isSuccess()) {
            return result;
        }

        if ("bii".equals(name)) {
            getUserInfoFromMp(ssoLoginModel);
            result = userInfoPublicMp(sysUser, ssoLoginModel);
        } else {
            result = userInfo(sysUser);
        }
        return result;
    }

    @PostMapping(value = "/sso") @ApiOperation("单点登录接口")
    public Result<?> sso(@RequestBody SsoLoginModel ssoLoginModel) {
        final String prefix = "[sso] ";
        log.info(prefix + "started with ssoLoginModel=" + ssoLoginModel);

        Result<?> result = new Result<>();
        if (ssoLoginModel == null) {
            log.error(prefix + "ssoLoginModel IS NULL");
            result.error500("登录失败");
            return result;
        }

        // TODO: authenticationTicket should be used to validate cas
        final String username = ssoLoginModel.getLoginid();
        final String stamp = ssoLoginModel.getStamp();
        final String token = ssoLoginModel.getToken();
        if (EncryptionUtils.loginCheck(username, stamp, token)) {
            final String name = "sysadmin".equals(username) ? "admin" : "bii";
            final SysUser sysUser = sysUserService.getUserByName(name);
            result = sysUserService.checkUserIsEffective(sysUser);
            if (result.isSuccess()) {
                if ("admin".equals(name)) {
                    result = userInfo(sysUser);
                } else {
                    getUserInfoFromMp(ssoLoginModel);
                    result = userInfoPublicMp(sysUser, ssoLoginModel);
                }
            }
        } else {
            log.error(prefix + "loginCheck FAILED");
            result.error500("登录失败！");
        }

        log.info(prefix + "Finished with result=" + result);
        return result;
    }

    @PostMapping(value = "/ssoMobile") @ApiOperation("移动端单点登录接口")
    public Result<?> ssoMobile(@RequestBody SsoLoginModel ssoLoginModel) {
        if (ssoLoginModel == null) {
            return Result.error("用户认证失败！");
        }
        Result<?> result = new Result<>();
        String username = ssoLoginModel.getLoginid();
        final String qiqiaoUserId = ssoLoginModel.getQiqiaoUserId();
        if (StringUtils.isNotEmpty(qiqiaoUserId)) {
            // 验证七巧用户
            final JSONObject usersInfoJson = qiqiaoService.usersInfo(qiqiaoUserId);
            log.info("usersInfoJson: " + usersInfoJson);
            if (usersInfoJson != null) {
                final String wxid = usersInfoJson.getString("account");
                final JSONObject userInfoByWxid = publicManagementService.getUserInfoByWxid(wxid);
                if (userInfoByWxid != null) {
                    final String oaId = userInfoByWxid.getString("account");
                    if (!"0".equals(oaId)) {
                        username = userInfoByWxid.getString("userName");
                        ssoLoginModel.setLoginid(username);
                    }
                }
            }
        } else {
            // TODO: authenticationTicket should be used to validate cas
            final String stamp = ssoLoginModel.getStamp();
            final String token = ssoLoginModel.getToken();
            if (!EncryptionUtils.mobileLoginCheck(username, stamp, token)) {
                result.error500("登录失败！");
                return result;
            }
        }

        final String name = "sysadmin".equals(username) ? "admin" : "bii";
        SysUser sysUser = sysUserService.getUserByName(name);
        if (sysUser == null) {
            result.error500("登录失败！");
            return result;
        }

        result = sysUserService.checkUserIsEffective(sysUser);
        if (!result.isSuccess()) {
            return result;
        }
        if ("admin".equals(name)) {
            result = userInfo(sysUser);
        } else {
            getUserInfoFromMp(ssoLoginModel);
            result = userInfoPublicMp(sysUser, ssoLoginModel);
        }
        return result;
    }

    @ApiOperation("登录接口") @PostMapping(value = "/login")
    public Result<?> login(@RequestBody SysLoginModel sysLoginModel) {
        if (!ApplicationProfile.DEV.equals(profile)) {
            return Result.error("登录失败！");
        }

        Result<?> result = new Result<>();
        //update-begin-author:taoyan date:20190828 for:校验验证码
        final String captcha = sysLoginModel.getCaptcha();
        if (captcha == null) {
            result.error500("验证码无效");
            return result;
        }

        final String lowerCaseCaptcha = captcha.toLowerCase();
        final String realKey = MD5Util.MD5Encode(lowerCaseCaptcha + sysLoginModel.getCheckKey(), "utf-8");
        final Object checkCode = redisUtil.get(realKey);
        //当进入登录页时，有一定几率出现验证码错误 #1714
        if (checkCode == null || !checkCode.toString().equals(lowerCaseCaptcha)) {
            result.error500("验证码错误");
            return result;
        }

        final String username = sysLoginModel.getUsername();
        final String password = sysLoginModel.getPassword();

        //1. 校验用户是否有效
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        final SysUser sysUser = sysUserService.getOne(queryWrapper);
        result = sysUserService.checkUserIsEffective(sysUser);
        if (!result.isSuccess()) {
            return result;
        }

        //2. 校验用户名或密码是否正确
        final String userpassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
        final String syspassword = sysUser.getPassword();
        if (!syspassword.equals(userpassword)) {
            result.error500("用户名或密码错误");
            return result;
        }

        //用户登录信息
        result = userInfo(sysUser);
        redisUtil.del(realKey);
        final LoginUser loginUser = new LoginUser();
        BeanUtils.copyProperties(sysUser, loginUser);
        baseCommonService.addLog("用户名: " + username + ",登录成功！", CommonConstant.LOG_TYPE_1, null, loginUser);

        return result;
    }

    /**
     * 退出登录
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/logout") public Result<Object> logout(HttpServletRequest request,
        HttpServletResponse response) {
        //用户退出逻辑
        String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
        if (oConvertUtils.isEmpty(token)) {
            return Result.error("退出登录失败！");
        }
        String username = JwtUtil.getUsername(token);
        LoginUser sysUser = sysBaseAPI.getUserByName(username);
        if (sysUser != null) {
            //update-begin--Author:wangshuai  Date:20200714  for：登出日志没有记录人员
            baseCommonService.addLog("用户名: " + sysUser.getRealname() + ",退出成功！", CommonConstant.LOG_TYPE_1, null,
                sysUser);
            //update-end--Author:wangshuai  Date:20200714  for：登出日志没有记录人员
            log.info(" 用户名:  " + sysUser.getRealname() + ",退出成功！ ");
            //清空用户登录Token缓存
            redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + token);
            //清空用户登录Shiro权限缓存
            redisUtil.del(CommonConstant.PREFIX_USER_SHIRO_CACHE + sysUser.getId());
            //清空用户的缓存信息（包括部门信息），例如sys:cache:user::<username>
            redisUtil.del(String.format("%s::%s", CacheConstant.SYS_USERS_CACHE, sysUser.getUsername()));
            //调用shiro的logout
            SecurityUtils.getSubject().logout();
            return Result.OK("退出登录成功！");
        } else {
            return Result.error("Token无效!");
        }
    }

    /**
     * 获取访问量
     *
     * @return
     */
    @GetMapping("loginfo") public Result<JSONObject> loginfo() {
        Result<JSONObject> result = new Result<JSONObject>();
        JSONObject obj = new JSONObject();
        //update-begin--Author:zhangweijian  Date:20190428 for：传入开始时间，结束时间参数
        // 获取一天的开始和结束时间
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date dayStart = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date dayEnd = calendar.getTime();
        // 获取系统访问记录
        Long totalVisitCount = logService.findTotalVisitCount();
        obj.put("totalVisitCount", totalVisitCount);
        Long todayVisitCount = logService.findTodayVisitCount(dayStart, dayEnd);
        obj.put("todayVisitCount", todayVisitCount);
        Long todayIp = logService.findTodayIp(dayStart, dayEnd);
        //update-end--Author:zhangweijian  Date:20190428 for：传入开始时间，结束时间参数
        obj.put("todayIp", todayIp);
        result.setResult(obj);
        result.success("登录成功");
        return result;
    }

    /**
     * 获取访问量
     *
     * @return
     */
    @GetMapping("visitInfo") public Result<List<Map<String, Object>>> visitInfo() {
        Result<List<Map<String, Object>>> result = new Result<List<Map<String, Object>>>();
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date dayEnd = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date dayStart = calendar.getTime();
        List<Map<String, Object>> list = logService.findVisitCount(dayStart, dayEnd);
        result.setResult(oConvertUtils.toLowerCasePageList(list));
        return result;
    }

    /**
     * 登陆成功选择用户当前部门
     *
     * @param user
     * @return
     */
    @PostMapping(value = "/selectDepart") public Result<JSONObject> selectDepart(@RequestBody SysUser user) {
        Result<JSONObject> result = new Result<JSONObject>();
        String username = user.getUsername();
        if (oConvertUtils.isEmpty(username)) {
            LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
            username = sysUser.getUsername();
        }
        String orgCode = user.getOrgCode();
        sysUserService.updateUserDepart(username, orgCode);
        SysUser sysUser = sysUserService.getUserByName(username);
        JSONObject obj = new JSONObject();
        obj.put("userInfo", sysUser);
        result.setResult(obj);
        return result;
    }

    /**
     * 用户信息
     *
     * @param sysUser
     * @return
     */
    private Result<?> userInfo(final SysUser sysUser) {
        final String syspassword = sysUser.getPassword();
        final String username = sysUser.getUsername();
        // 生成token
        final String token = JwtUtil.sign(username, syspassword);
        // 设置token缓存有效时间
        setRedisSysUser(token, sysUser);

        // 获取用户部门信息
        JSONObject obj = new JSONObject();
        List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
        obj.put("departs", departs);
        if (departs == null || departs.size() == 0) {
            obj.put("multi_depart", 0);
        } else if (departs.size() == 1) {
            sysUserService.updateUserDepart(username, departs.get(0).getOrgCode());
            obj.put("multi_depart", 1);
        } else {
            //查询当前是否有登录部门
            // update-begin--Author:wangshuai Date:20200805 for：如果用戶为选择部门，数据库为存在上一次登录部门，则取一条存进去
            SysUser sysUserById = sysUserService.getById(sysUser.getId());
            if (oConvertUtils.isEmpty(sysUserById.getOrgCode())) {
                sysUserService.updateUserDepart(username, departs.get(0).getOrgCode());
            }
            // update-end--Author:wangshuai Date:20200805 for：如果用戶为选择部门，数据库为存在上一次登录部门，则取一条存进去
            obj.put("multi_depart", 2);
        }
        obj.put("token", token);
        obj.put("userInfo", sysUser);
        obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
        return Result.OK(obj);
    }

    /**
     * 获取加密字符串
     *
     * @return
     */
    @GetMapping(value = "/getEncryptedString") public Result<Map<String, String>> getEncryptedString() {
        Result<Map<String, String>> result = new Result<Map<String, String>>();
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", EncryptedString.key);
        map.put("iv", EncryptedString.iv);
        result.setResult(map);
        return result;
    }

    /**
     * 后台生成图形验证码 ：有效
     *
     * @param response
     * @param key
     */
    @ApiOperation("获取验证码") @GetMapping(value = "/randomImage/{key}") public Result<String> randomImage(
        HttpServletResponse response, @PathVariable String key) {
        Result<String> res = new Result<>();
        try {
            String code = RandomUtil.randomString(BASE_CHECK_CODES, 4);
            String lowerCaseCode = code.toLowerCase();
            String realKey = MD5Util.MD5Encode(lowerCaseCode + key, "utf-8");
            redisUtil.set(realKey, lowerCaseCode, 60);
            String base64 = RandImageUtil.generate(code);
            res.setSuccess(true);
            res.setResult(base64);
        } catch (Exception e) {
            res.error500("获取验证码出错" + e.getMessage());
            e.printStackTrace();
        }
        return res;
    }

    /**
     * app登录
     *
     * @param sysLoginModel
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/mLogin") public Result<JSONObject> mLogin(@RequestBody SysLoginModel sysLoginModel) {
        Result<JSONObject> result = null;
        String username = sysLoginModel.getUsername();
        String password = sysLoginModel.getPassword();

        //1. 校验用户是否有效
        SysUser sysUser = sysUserService.getUserByName(username);
        result = sysUserService.checkUserIsEffective(sysUser);
        if (!result.isSuccess()) {
            return result;
        }

        //2. 校验用户名或密码是否正确
        String userpassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
        String syspassword = sysUser.getPassword();
        if (!syspassword.equals(userpassword)) {
            result.error500("用户名或密码错误");
            return result;
        }

        String orgCode = sysUser.getOrgCode();
        if (oConvertUtils.isEmpty(orgCode)) {
            //如果当前用户无选择部门 查看部门关联信息
            List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
            if (departs == null || departs.size() == 0) {
                result.error500("用户暂未归属部门,不可登录!");
                return result;
            }
            orgCode = departs.get(0).getOrgCode();
            sysUser.setOrgCode(orgCode);
            sysUserService.updateUserDepart(username, orgCode);
        }
        JSONObject obj = new JSONObject();
        //用户登录信息
        obj.put("userInfo", sysUser);

        // 生成token
        String token = JwtUtil.sign(username, syspassword);
        // 设置超时时间
        setRedisSysUser(token, sysUser);

        //token 信息
        obj.put("token", token);
        result.setResult(obj);
        result.setSuccess(true);
        result.setCode(200);
        baseCommonService.addLog("用户名: " + username + ",登录成功[移动端]！", CommonConstant.LOG_TYPE_1, null);
        return result;
    }

    /**
     * 图形验证码
     *
     * @param sysLoginModel
     * @return
     */
    @PostMapping(value = "/checkCaptcha") public Result<?> checkCaptcha(@RequestBody SysLoginModel sysLoginModel) {
        String captcha = sysLoginModel.getCaptcha();
        String checkKey = sysLoginModel.getCheckKey();
        if (captcha == null) {
            return Result.error("验证码无效");
        }
        String lowerCaseCaptcha = captcha.toLowerCase();
        String realKey = MD5Util.MD5Encode(lowerCaseCaptcha + checkKey, "utf-8");
        Object checkCode = redisUtil.get(realKey);
        if (checkCode == null || !checkCode.equals(lowerCaseCaptcha)) {
            return Result.error("验证码错误");
        }
        return Result.OK();
    }

    private Result<?> userInfoPublicMp(SysUser sysUser, SsoLoginModel ssoLoginModel) {
        Result<JSONObject> result = new Result<>();
        final String syspassword = sysUser.getPassword();
        final String username = sysUser.getUsername();
        final String mpUserId = ssoLoginModel.getMpUserId();
        if (StringUtils.isEmpty(mpUserId)) {
            result.error500("登录失败！");
        } else {
            // 生成token
            // 防止用户同时登录产生相同token的问题
            final String token = JwtUtil.sign((username + mpUserId), syspassword);
            final SysTokenUser sysTokenUser = new SysTokenUser().setToken(token).setLoginid(ssoLoginModel.getLoginid())
                .setOaId(ssoLoginModel.getOaId()).setRealname(ssoLoginModel.getRealname())
                .setRoleCodes(ssoLoginModel.getRoleCodes()).setMpUserId(ssoLoginModel.getMpUserId())
                .setDeptOaId(ssoLoginModel.getDeptOaId()).setDept(ssoLoginModel.getDept())
                .setCompany(ssoLoginModel.getCompany()).setCompanyOaId(ssoLoginModel.getCompanyOaId());
            // 设置token缓存有效时间, sysTokenUser也存在redis里
            JwtUtil.setRedisSysTokenUser(redisUtil, token, sysTokenUser);

            // 获取用户部门信息
            final JSONObject obj = new JSONObject();
            obj.put("token", token);
            obj.put("userInfo", sysUser);
            obj.put("sysTokenUser", sysTokenUser);
            obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
            result.setResult(obj);
            result.success("登录成功");
        }

        return result;
    }

    private void setRedisSysUser(String token, SysUser sysUser) {
        SysTokenUser sysTokenUser = new SysTokenUser();
        BeanUtils.copyProperties(sysUser, sysTokenUser);
        JwtUtil.setRedisSysTokenUser(redisUtil, token, sysTokenUser);
    }

    private List<String> convertRoleCodesList(String roleStr) {
        if (StringUtils.isNotEmpty(roleStr)) {
            roleStr = roleStr.replaceAll("\\\\", "");
            roleStr = roleStr.replaceAll("\\[", "");
            roleStr = roleStr.replaceAll("\"", "");
            roleStr = roleStr.replaceAll("]", "");
            return Arrays.asList(roleStr.split(","));
        }

        return new ArrayList<>();
    }

    private void completeUserInfo(SsoLoginModel ssoLoginModel, JSONObject userInfo) {
        if (ssoLoginModel != null && userInfo != null) {
            final String permissionRoleIds = userInfo.getString("permissionRoleIds");
            final List<String> roleCodeList = convertRoleCodesList(permissionRoleIds);
            StringBuilder roleCodes = new StringBuilder();
            for (int i = 0; i < roleCodeList.size(); ++i) {
                roleCodes.append(roleCodeList.get(i));
                if (i != roleCodeList.size() - 1) {
                    roleCodes.append(",");
                }
            }

            ssoLoginModel.setCompanyOaId(userInfo.getInteger("companyOrgOaId"))
                .setDeptOaId(userInfo.getInteger("orgOaId")).setMpUserId(userInfo.getString("userId"))
                .setRealname(userInfo.getString("nickName")).setLoginid(userInfo.getString("userName"))
                .setOaId(userInfo.getInteger("account")).setCompany(userInfo.getString("companyOrgName"))
                .setDept(userInfo.getString("orgName")).setRoleCodes(roleCodes.toString());
        }
    }

    private void getUserInfoFromMp(final SsoLoginModel ssoLoginModel) {
        if (ssoLoginModel == null) {
            return;
        }
        JSONObject userInfo = publicManagementService.getUserInfoByUserName(ssoLoginModel.getLoginid());
        if (userInfo != null) {
            completeUserInfo(ssoLoginModel, userInfo);
        } else {
            // 外部用户以手机号为用户名
            // 尝试用手机号登录系统
            userInfo = publicManagementService.getUserInfoByPhoneNumber(ssoLoginModel.getLoginid());
            if (userInfo == null) {
                log.error("手机号有误: " + ssoLoginModel);
            } else {
                completeUserInfo(ssoLoginModel, userInfo);
            }
        }
    }
}
