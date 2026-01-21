package org.jeecg.modules.regulation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.content.service.IContentManagementService;
import org.jeecg.modules.regulation.dto.RegulationQueryDTO;
import org.jeecg.modules.regulation.entity.ZyBiiRegulationAdmin;
import org.jeecg.modules.regulation.entity.ZyRegulationBii;
import org.jeecg.modules.regulation.service.IZyBiiRegulationAdminService;
import org.jeecg.modules.regulation.service.IZyRegulationBiiService;
import org.jeecg.modules.regulation.vo.ZyRegulationBiiHistoryVO;
import org.jeecg.modules.regulation.vo.ZyRegulationBiiVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Api(tags = "自研-京投本部制度") @RestController @RequestMapping("/regulation_bii") @Slf4j
public class ZyRegulationBiiController extends JeecgController<ZyRegulationBii, IZyRegulationBiiService> {
    @Autowired private IZyRegulationBiiService zyRegulationBiiService;
    @Autowired private IZyBiiRegulationAdminService zyBiiRegulationAdminService;
    @Autowired @Qualifier("biiContentManagementService") private IContentManagementService contentManagementService;

    @AutoLog("自研-京投本部制度-分页列表查询") @ApiOperation("分页列表查询") @GetMapping("/list")
    public Result<Page<ZyRegulationBiiVO>> queryPageList(RegulationQueryDTO queryDTO,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("[regulation_bii/list] " + queryDTO);
        Page<ZyRegulationBiiVO> page = new Page<>(pageNo, pageSize);
        if (queryDTO == null) {
            queryDTO = new RegulationQueryDTO();
        }

        page = zyRegulationBiiService.queryNewestVersionPageList(page, queryDTO);
        return Result.OK(page);
    }

    @AutoLog("自研-京投本部制度-同步旧制度系统") @ApiOperation("同步旧制度系统") @GetMapping("/sync")
    public Result<?> sync() {
        log.info("[regulation_bii/sync] started");
        zyRegulationBiiService.syncOldRegulationList();
        log.info("[regulation_bii/sync] finished");
        return Result.OK();
    }

    @AutoLog("自研-京投本部制度-重建索引库") @ApiOperation("重建索引库") @GetMapping("/rebuild")
    public Result<?> rebuild() {
        log.info("[regulation_bii/rebuild] started");
        Set<String> fileIdentifiersToAdd = new HashSet<>();
        Set<String> fileIdentifiersToUpdate = new HashSet<>();
        Set<String> fileIdentifiersToDelete = new HashSet<>();

        final List<ZyRegulationBii> zyRegulationBiiList = zyRegulationBiiService.list();
        for (final ZyRegulationBii zyRegulationBii : zyRegulationBiiList) {
            if (zyRegulationBii.getActive() == 1) {
                fileIdentifiersToUpdate.add(zyRegulationBii.getIdentifier());
            }
        }
        zyRegulationBiiService.rebuildIndex(fileIdentifiersToAdd, fileIdentifiersToUpdate, fileIdentifiersToDelete);
        log.info("[regulation_bii/rebuild] finished");
        return Result.OK();
    }

    @AutoLog("自研-京投本部制度-根据id查询") @ApiOperation("根据id查询") @GetMapping("/query_by_id")
    public Result<ZyRegulationBiiVO> queryById(@RequestParam Integer id) {
        final Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            return Result.OK(null);
        }

        final LoginUser sysUser = (LoginUser)subject.getPrincipal();
        if (sysUser == null) {
            return Result.OK(null);
        }

        final String mark = sysUser.getRealname() + " " + sysUser.getDept();
        log.info("[regulation_bii/query_by_id] " + id + " " + mark);

        final List<ZyBiiRegulationAdmin> adminList =
            zyBiiRegulationAdminService.lambdaQuery().eq(ZyBiiRegulationAdmin::getLoginid, sysUser.getLoginid()).list();

        final ZyRegulationBiiVO zyRegulationBiiVO = zyRegulationBiiService.queryById(id, mark);

        // 如果是管理员，那么可以提供下载链接
        if (zyRegulationBiiVO != null && CollectionUtils.isNotEmpty(adminList)) {
            final List<ZyRegulationBiiHistoryVO> historyList = zyRegulationBiiVO.getHistoryList();

            final String latestCode = zyRegulationBiiVO.getCode();
            if (StringUtils.isNotEmpty(latestCode) && CollectionUtils.isNotEmpty(historyList)) {
                for (final ZyRegulationBiiHistoryVO history : historyList) {
                    // 提供pdf下载链接
                    history.setPdfDownloadUrl(contentManagementService.getDownloadUrl(history.getContentFileId()));
                    if (latestCode.equalsIgnoreCase(history.getCode())) {
                        // 提供word下载链接
                        history.setDocxDownloadUrl(
                            contentManagementService.getDownloadUrl(zyRegulationBiiVO.getContentFileId()));
                    }
                }
            }
        }

        return Result.OK(zyRegulationBiiVO);
    }

    @AutoLog("自研-京投本部制度-新建/修订制度发布") @ApiOperation("新建/修订制度发布") @GetMapping("/create_or_edit")
    public Result<?> createOrEdit(@RequestParam String qiqiaoRegulationId) {
        log.info("[regulation_bii/create_or_edit] qiqiaoRegulationId: " + qiqiaoRegulationId);
        zyRegulationBiiService.createOrEdit(qiqiaoRegulationId);
        return Result.OK();
    }

    @AutoLog("自研-京投本部制度-制度废弃") @ApiOperation("制度废弃") @GetMapping("/inactivate")
    public Result<?> inactivateByIdentifier(@RequestParam String identifier) {
        log.info("[inactivateByIdentifier] identifier: " + identifier);
        zyRegulationBiiService.inactivateByIdentifier(identifier);
        return Result.OK();
    }

    @AutoLog("自研-京投本部制度-同步到七巧") @ApiOperation("同步到七巧") @GetMapping("/sync_to_qiqiao")
    public Result<?> syncToQiqiao(@RequestParam(required = false) Integer minId,
        @RequestParam(required = false) Integer maxId) {
        log.info("[syncToQiqiao] minId: " + minId + ", maxId: " + maxId);
        zyRegulationBiiService.syncToQiqiao(minId, maxId);
        return Result.OK();
    }
}
