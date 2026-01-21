package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author scott
 * @Date 2019/9/23 14:12
 * @Description: 编程校验token有效性
 */
@Slf4j public class TokenUtils {

    /**
     * 获取 request 里传递的 token
     *
     * @param request
     * @return
     */
    public static String getTokenByRequest(HttpServletRequest request) {
        String token = request.getParameter("token");
        if (token == null) {
            token = request.getHeader("X-Access-Token");
        }
        return token;
    }

    /**
     * 验证Token
     */
    public static boolean verifyToken(HttpServletRequest request, CommonAPI commonAPI, RedisUtil redisUtil) {
        log.debug(" -- url --" + request.getRequestURL());
        String token = getTokenByRequest(request);

        if (StringUtils.isBlank(token)) {
            throw new AuthenticationException("Token不能为空!");
        }

        // 解密获得username，用于和数据库进行对比
        String username = JwtUtil.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("Token非法无效!");
        }

        // 查询用户信息
        LoginUser user = commonAPI.getUserByName(username);
        if (user == null) {
            throw new AuthenticationException("用户不存在!");
        }
        // 判断用户状态
        if (user.getStatus() != 1) {
            throw new AuthenticationException("账号已锁定,请联系管理员!");
        }
        // 校验token是否超时失效 & 或者账号密码是否错误
        if (!jwtTokenRefresh(token, redisUtil)) {
            throw new AuthenticationException("Token失效，请重新登录");
        }
        return true;
    }

    /**
     * 刷新token（保证用户在线操作不掉线）
     *
     * @param token
     * @param redisUtil
     * @return
     */
    public static boolean jwtTokenRefresh(String token, RedisUtil redisUtil) {
        if (redisUtil.get(CommonConstant.PREFIX_USER_TOKEN + token) != null) {
            redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME / 1000);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 验证Token
     */
    public static boolean verifyToken(String token, CommonAPI commonAPI, RedisUtil redisUtil) {
        if (StringUtils.isBlank(token)) {
            throw new AuthenticationException("token不能为空!");
        }

        // 解密获得username，用于和数据库进行对比
        String username = JwtUtil.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("token非法无效!");
        }

        // 查询用户信息
        LoginUser user = commonAPI.getUserByName(username);
        if (user == null) {
            throw new AuthenticationException("用户不存在!");
        }
        // 判断用户状态
        if (user.getStatus() != 1) {
            throw new AuthenticationException("账号已被锁定,请联系管理员!");
        }
        // 校验token是否超时失效 & 或者账号密码是否错误
        if (!jwtTokenRefresh(token, redisUtil)) {
            throw new AuthenticationException("Token失效，请重新登录!");
        }
        return true;
    }

}
