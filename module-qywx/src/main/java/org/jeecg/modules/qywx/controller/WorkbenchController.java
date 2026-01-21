package org.jeecg.modules.qywx.controller;

import org.jeecg.modules.qywx.dto.WechatJsJdkConfDTO;
import org.jeecg.modules.qywx.enums.ResponseEnum;
import org.jeecg.modules.qywx.service.WxInterface;
import org.jeecg.modules.qywx.utils.ErrorUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "支撑-企业微信JS-SDK") @RestController @RequestMapping("/workbench") public class WorkbenchController {
    @Autowired private WxInterface wxInterface;

    @ApiOperation("获取JDK配置") @GetMapping("wxConf") public Object getWxConf(@RequestParam(name = "url") String url,
        @RequestParam(name = "source", defaultValue = "PRIVATE", required = false) String source) {
        WechatJsJdkConfDTO dto = wxInterface.getJdkConf(url, source);
        if (dto == null) {
            return ErrorUtils.formalError("获取异常", ResponseEnum.RESPONSE_NO);
        }
        return ErrorUtils.success(dto, ResponseEnum.RESPONSE_YES);
    }
}
