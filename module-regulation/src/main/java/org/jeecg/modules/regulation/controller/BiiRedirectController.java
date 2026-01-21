package org.jeecg.modules.regulation.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.common.utils.EncryptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

import static org.jeecg.modules.common.utils.EncryptionUtils.EMOBILE_SECRET;

@Api(tags = "自研系统单点") @Slf4j @RestController @RequestMapping("/bii") public class BiiRedirectController {
    @ApiOperation("单点跳转") @GetMapping("/redirect")
    public Result<?> redirect(HttpServletResponse response, @RequestParam(value = "indexUrl") String indexUrl) {
        final LoginUser user = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        final String loginId = user.getLoginid();
        final long timestamp = System.currentTimeMillis();
        final String token = EncryptionUtils.hexSHA1(EMOBILE_SECRET + loginId + timestamp);
        final String redirectUrl = indexUrl + "?loginid=" + loginId + "&stamp=" + timestamp + "&token=" + token;
        log.info("call /project/redirect 单点跳转到: {}", redirectUrl);
        return Result.OK(redirectUrl);
    }
}
