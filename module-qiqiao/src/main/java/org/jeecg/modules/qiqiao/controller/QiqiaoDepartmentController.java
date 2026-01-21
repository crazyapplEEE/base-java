package org.jeecg.modules.qiqiao.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.common.vo.AdvTreeNode;
import org.jeecg.modules.qiqiao.service.IQiqiaoDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "自研-七巧-部门API") @RestController @RequestMapping("/qiqiao/department") @Slf4j
public class QiqiaoDepartmentController {
    @Autowired private IQiqiaoDepartmentService qiqiaoDepartmentService;

    @AutoLog(value = "自研-七巧-部门API-获取AntDesignVue的TreeData格式的部门信息")
    @ApiOperation(value = "获取AntDesignVue的TreeData格式的部门信息") @GetMapping(value = "/getDeptTreeData")
    public Result<List<AdvTreeNode>> page(@RequestParam(value = "deptId", required = false) String deptId) {
        return Result.OK(qiqiaoDepartmentService.getDeptTreeData(deptId, false));
    }
}