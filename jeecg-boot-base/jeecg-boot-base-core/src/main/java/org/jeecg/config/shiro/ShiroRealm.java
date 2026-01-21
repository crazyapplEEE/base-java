package org.jeecg.config.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysTokenUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Description: 用户登录鉴权和获取用户授权
 * @Author: Scott
 * @Date: 2019-4-23 8:13
 * @Version: 1.1
 */
@Component @Slf4j public class ShiroRealm extends AuthorizingRealm {
    @Lazy @Resource private CommonAPI commonAPI;

    @Lazy @Resource private RedisUtil redisUtil;

    /**
     * 必须重写此方法，不然Shiro会报错
     */
    @Override public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 权限信息认证(包括角色以及权限)是用户访问controller的时候才进行验证(redis存储的此处权限信息)
     * 触发检测用户权限时才会调用此方法，例如checkRole,checkPermission
     *
     * @param principals 身份信息
     * @return AuthorizationInfo 权限信息
     */
    @Override protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        log.info("===============Shiro权限认证开始============ [ roles、permissions]==========");
        LoginUser sysUser = null;
        List<String> roleList = null;
        if (principals != null) {
            sysUser = (LoginUser)principals.getPrimaryPrincipal();

            String roleCodes = sysUser.getRoleCodes();
            if (roleCodes != null) {
                roleList = Arrays.asList(roleCodes.split(","));
            }

            if (roleList == null) {
                roleList = new ArrayList<>();
            }
            // 加上jeecg本身的role
            Set<String> roleSet = commonAPI.queryUserRoles(sysUser.getUsername());
            for (final String role : roleSet) {
                roleList.add(role);
            }
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        if (roleList != null && !roleList.isEmpty()) {
            Set<String> roleSet = new HashSet<>(roleList);
            log.info("roleSet: " + roleSet.toString());
            info.setRoles(roleSet);
        }

        // 设置用户拥有的角色集合，比如“admin,test”

        // 设置用户拥有的权限集合，比如“sys:role:add,sys:user:add”
        Set<String> permissionSet = commonAPI.queryUserAuths(sysUser.getUsername());
        info.addStringPermissions(permissionSet);
        log.info("===============Shiro权限认证成功==============");
        return info;
    }

    /**
     * 用户信息认证是在用户进行登录的时候进行验证(不存redis)
     * 也就是说验证用户输入的账号和密码是否正确，错误抛出异常
     *
     * @param auth 用户登录的账号密码信息
     * @return 返回封装了用户信息的 AuthenticationInfo 实例
     * @throws AuthenticationException
     */
    @Override protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth)
        throws AuthenticationException {
        log.debug("===============Shiro身份认证开始============doGetAuthenticationInfo==========");
        String token = (String)auth.getCredentials();
        if (token == null) {
            log.info("————————身份认证失败——————————IP地址:  " + oConvertUtils
                .getIpAddrByRequest(SpringContextUtils.getHttpServletRequest()));
            throw new AuthenticationException("token为空!");
        }
        // 校验token有效性
        LoginUser loginUser = this.checkUserTokenIsEffect(token);

        final SysTokenUser sysTokenUser = commonAPI.queryUserByToken(token);
        if (sysTokenUser != null) {
            loginUser.setLoginid(sysTokenUser.getLoginid()).setOaId(sysTokenUser.getOaId())
                .setRealname(sysTokenUser.getRealname()).setRoleCodes(sysTokenUser.getRoleCodes())
                .setMpUserId(sysTokenUser.getMpUserId()).setDeptOaId(sysTokenUser.getDeptOaId())
                .setDept(sysTokenUser.getDept()).setCompanyOaId(sysTokenUser.getCompanyOaId())
                .setCompany(sysTokenUser.getCompany()).setMpUserId(sysTokenUser.getMpUserId());
        }

        //        log.info(new StringBuilder().append("\nusername: ").append(loginUser.getUsername()).append("\nrealname: ")
        //            .append(loginUser.getRealname()).append("\nloginid: ").append(loginUser.getLoginid()).append("\noaId: ")
        //            .append(loginUser.getOaId()).append("\ncompany: ").append(loginUser.getCompany()).append("\ncompanyOaId: ")
        //            .append(loginUser.getCompanyOaId()).append("\ndept: ").append(loginUser.getDept()).append("\ndeptOaId: ")
        //            .append(loginUser.getDeptOaId()).append("\nmpUserId: ").append(loginUser.getMpUserId())
        //            .append("\nroleCodes: ").append(loginUser.getRoleCodes()).toString());

        return new SimpleAuthenticationInfo(loginUser, token, getName());
    }

    /**
     * 校验token的有效性
     *
     * @param token
     */
    public LoginUser checkUserTokenIsEffect(String token) throws AuthenticationException {
        // 解密获得username，用于和数据库进行对比
        String username = JwtUtil.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("token非法无效!");
        }

        // TODO: 需要校验是否影响外部用户
        if (username.startsWith("bii")) {
            username = "bii";
        }

        // 查询用户信息
        log.debug("———校验token是否有效————checkUserTokenIsEffect——————— " + token);
        LoginUser loginUser = commonAPI.getUserByName(username);
        if (loginUser == null) {
            throw new AuthenticationException("用户不存在!");
        }
        // 判断用户状态
        if (loginUser.getStatus() != 1) {
            throw new AuthenticationException("账号已被锁定,请联系管理员!");
        }
        // 校验token是否超时失效 & 或者账号密码是否错误
        if (!TokenUtils.jwtTokenRefresh(token, redisUtil)) {
            throw new AuthenticationException("Token失效，请重新登录!");
        }

        return loginUser;
    }

    /**
     * 清除当前用户的权限认证缓存
     *
     * @param principals 权限信息
     */
    @Override public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }

}
