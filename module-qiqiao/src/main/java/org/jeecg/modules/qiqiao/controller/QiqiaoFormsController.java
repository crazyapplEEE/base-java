package org.jeecg.modules.qiqiao.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zmy
 */
@Api(tags = "自研-七巧-表单API") @RestController @RequestMapping("/qiqiao/forms") @Slf4j
public class QiqiaoFormsController {
    @Autowired private IQiqiaoFormsService qiqiaoFormsService;

    @AutoLog(value = "自研-七巧-表单API-分页查询") @ApiOperation("分页查询") @PostMapping(value = "/page")
    public JSONObject page(@RequestBody RecordVO recordVO) {
        return qiqiaoFormsService.page(recordVO);
    }

    @AutoLog(value = "自研-七巧-表单API-新增或更新") @ApiOperation("新增或更新") @PostMapping(value = "/saveOrUpdate")
    public Result<?> saveOrUpdate(@RequestBody RecordVO recordVO) {
        return Result.OK(qiqiaoFormsService.saveOrUpdate(recordVO));
    }
}
