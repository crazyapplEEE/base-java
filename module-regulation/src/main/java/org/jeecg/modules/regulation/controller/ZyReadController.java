package org.jeecg.modules.regulation.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.regulation.entity.ZyRead;
import org.jeecg.modules.regulation.service.IZyReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Api(tags = "自研-制度阅读记录") @RestController @RequestMapping("/regulation_read") @Slf4j
public class ZyReadController extends JeecgController<ZyRead, IZyReadService> {
    @Autowired private IZyReadService zyReadService;

    @AutoLog("自研-制度阅读记录-保存") @ApiOperation("制度阅读记录-保存") @PostMapping("/save")
    public Result<Boolean> save(@RequestBody ZyRead zyRead) {
        log.info("[regulation_read/save] " + zyRead);
        if (zyRead != null) {
            final String identifier = zyRead.getIdentifier();
            final String version = zyRead.getVersion();
            if (StringUtils.isNotEmpty(identifier) && StringUtils.isNotEmpty(version)) {
                return Result.OK(zyReadService.save(zyRead));
            }
        }
        return Result.OK(false);
    }
}
