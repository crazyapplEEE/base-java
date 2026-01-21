package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.service.ISysTenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * 租户配置信息
 */
@Slf4j @RestController @RequestMapping("/sys/tenant") public class SysTenantController {

    @Autowired private ISysTenantService sysTenantService;

    /**
     * 获取列表数据
     *
     * @param sysTenant
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @PermissionData(pageComponent = "system/TenantList") @GetMapping(value = "/list")
    public Result<IPage<SysTenant>> queryPageList(SysTenant sysTenant,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        Result<IPage<SysTenant>> result = new Result<>();
        QueryWrapper<SysTenant> queryWrapper = QueryGenerator.initQueryWrapper(sysTenant, req.getParameterMap());
        Page<SysTenant> page = new Page<>(pageNo, pageSize);
        IPage<SysTenant> pageList = sysTenantService.page(page, queryWrapper);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
     * 添加
     *
     * @param
     * @return
     */
    @PostMapping(value = "/add") public Result<SysTenant> add(@RequestBody SysTenant sysTenant) {
        Result<SysTenant> result = new Result<>();
        if (sysTenantService.getById(sysTenant.getId()) != null) {
            return result.error500("该编号已存在!");
        }
        try {
            sysTenantService.save(sysTenant);
            result.success("添加成功！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败");
        }
        return result;
    }

    /**
     * 编辑
     *
     * @param
     * @return
     */
    @PostMapping(value = "/edit") public Result<SysTenant> edit(@RequestBody SysTenant tenant) {
        Result<SysTenant> result = new Result<>();
        SysTenant sysTenant = sysTenantService.getById(tenant.getId());
        if (sysTenant == null) {
            result.error500("未找到对应实体");
        } else {
            boolean ok = sysTenantService.updateById(tenant);
            if (ok) {
                result.success("修改成功!");
            }
        }
        return result;
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @PostMapping(value = "/delete") public Result<?> delete(@RequestParam(name = "id") String id) {
        sysTenantService.removeById(id);
        return Result.OK("删除成功");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @PostMapping(value = "/deleteBatch") public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        Result<?> result = new Result<>();
        if (oConvertUtils.isEmpty(ids)) {
            result.error500("未选中租户！");
        } else {
            List<String> ls = Arrays.asList(ids.split(","));
            sysTenantService.removeByIds(ls);
            result.success("删除成功!");
        }
        return result;
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById") public Result<SysTenant> queryById(@RequestParam(name = "id") String id) {
        Result<SysTenant> result = new Result<>();
        SysTenant sysTenant = sysTenantService.getById(id);
        if (sysTenant == null) {
            result.error500("未找到对应实体");
        } else {
            result.setResult(sysTenant);
            result.setSuccess(true);
        }
        return result;
    }

    /**
     * 查询有效的 租户数据
     *
     * @return
     */
    @GetMapping(value = "/queryList") public Result<List<SysTenant>> queryList(
        @RequestParam(name = "ids", required = false) String ids) {
        Result<List<SysTenant>> result = new Result<>();
        LambdaQueryWrapper<SysTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysTenant::getStatus, 1);
        if (oConvertUtils.isNotEmpty(ids)) {
            query.in(SysTenant::getId, ids.split(","));
        }
        //此处查询忽略时间条件
        List<SysTenant> ls = sysTenantService.list(query);
        result.setSuccess(true);
        result.setResult(ls);
        return result;
    }
}
