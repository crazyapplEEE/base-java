package org.jeecg.modules.system.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.modules.system.mapper.SysDictMapper;
import org.jeecg.modules.system.model.DuplicateCheckVo;
import org.jeecg.modules.system.security.DictQueryBlackListHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Title: DuplicateCheckAction
 * @Description: 重复校验工具
 * @Author 张代浩
 * @Date 2019-03-25
 * @Version V1.0
 */
@Api(tags = "JEECG-重复校验") @Slf4j @RestController @RequestMapping("/sys/duplicate")
public class DuplicateCheckController {
    @Autowired private SysDictMapper sysDictMapper;
    @Autowired private DictQueryBlackListHandler dictQueryBlackListHandler;

    /**
     * 校验数据是否在系统中是否存在
     *
     * @return
     */
    @GetMapping(value = "/check") @ApiOperation("重复校验接口") public Result<Object> doDuplicateCheck(
        DuplicateCheckVo duplicateCheckVo, HttpServletRequest request) {
        Long num = null;

        log.info("----duplicate check------：" + duplicateCheckVo.toString());
        //关联表字典（举例：sys_user,realname,id）
        //SQL注入校验（只限制非法串改数据库）
        final String[] sqlInjCheck = {duplicateCheckVo.getTableName(), duplicateCheckVo.getFieldName()};
        SqlInjectionUtil.filterContent(sqlInjCheck);
        if (StringUtils.isNotBlank(duplicateCheckVo.getDataId())) {
            // [2].编辑页面校验
            num = sysDictMapper.duplicateCheckCountSql(duplicateCheckVo);
        } else {
            // [1].添加页面校验
            num = sysDictMapper.duplicateCheckCountSqlNoDataId(duplicateCheckVo);
        }

        if (num == null || num == 0) {
            // 该值可用
            return Result.OK("该值可用！");
        } else {
            // 该值不可用
            log.info("该值不可用，系统中已存在！");
            return Result.error("该值不可用，系统中已存在！");
        }
    }
}
