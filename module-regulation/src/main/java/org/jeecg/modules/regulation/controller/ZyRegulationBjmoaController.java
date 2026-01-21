package org.jeecg.modules.regulation.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.content.service.IContentManagementService;
import org.jeecg.modules.regulation.dto.RegulationQueryDTO;
import org.jeecg.modules.regulation.dto.RegulationTempQueryDTO;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoa;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaDept;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaCarouselPictureService;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaDeptService;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaService;
import org.jeecg.modules.regulation.vo.ZyRegulationBjmoaStatisticsVO;
import org.jeecg.modules.regulation.vo.ZyRegulationBjmoaVO;
import org.jeecg.modules.regulation.vo.ZyRegulationTempBjmoaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Api(tags = "自研-轨道运营制度") @RestController @RequestMapping("/regulation_bjmoa") @Slf4j
public class ZyRegulationBjmoaController extends JeecgController<ZyRegulationBjmoa, IZyRegulationBjmoaService> {
    @Autowired private IZyRegulationBjmoaService zyRegulationBjmoaService;
    @Autowired private IZyRegulationBjmoaCarouselPictureService zyRegulationBjmoaCarouselPictureService;
    @Autowired private IZyRegulationBjmoaDeptService zyRegulationBjmoaDeptService;
    @Autowired @Qualifier("bjmoaContentManagementService") private IContentManagementService contentManagementService;

    @AutoLog("自研-轨道运营制度-分页列表查询") @ApiOperation("分页列表查询") @GetMapping("/list")
    public Result<Page<ZyRegulationBjmoaVO>> queryPageList(RegulationQueryDTO queryDTO,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("[regulation_bjmoa/list] " + queryDTO);
        Page<ZyRegulationBjmoaVO> page = new Page<>(pageNo, pageSize);
        if (queryDTO == null) {
            queryDTO = new RegulationQueryDTO();
        }

        // @todo 权限控制
        page = zyRegulationBjmoaService.queryNewestVersionPageList(page, queryDTO);
        return Result.OK(page);
    }

    @AutoLog("自研-轨道运营制度-根据id查询") @ApiOperation("根据id查询") @GetMapping("/query_by_id")
    public Result<ZyRegulationBjmoaVO> queryById(@RequestParam Integer id) {
        log.info("[regulation_bjmoa/query_by_id] " + id);
        LoginUser sysUser = null;
        String mark = "";
        try {
            sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                mark = sysUser.getRealname() + " " + sysUser.getDept();
            }
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }

        if (sysUser == null) {
            return Result.OK(null);
        }

        // 权限控制
        final ZyRegulationBjmoaVO zyRegulationBjmoaVO = zyRegulationBjmoaService.queryById(id, mark);
        String levleId = zyRegulationBjmoaVO.getLevelId();
        String regulationCode = zyRegulationBjmoaVO.getCode();
        String version = zyRegulationBjmoaVO.getVersion();
        String identifier = zyRegulationBjmoaVO.getIdentifier();
        List<ZyRegulationBjmoaDept> deptList = zyRegulationBjmoaDeptService.getByRegulationCodeAndVersion(regulationCode, version);
        String responsibleDepartment = null;
        if (deptList == null || deptList.size() == 0) {
            log.warn("deptList IS NULL");
        } else {
            responsibleDepartment = deptList.get(0).getQiqiaoDeptId();
        }
        boolean wordDownloadPermission = zyRegulationBjmoaService.queryDownloadWordPermission(sysUser.getLoginid(), responsibleDepartment);
        boolean pdfDownloadPermission = zyRegulationBjmoaService.queryDownloadPdfPermission(sysUser.getLoginid(), levleId, responsibleDepartment, identifier);

        // 如果是主责部门文件管理员，提供word和pdf下载链接
        if (wordDownloadPermission) {
            zyRegulationBjmoaVO.setDownloadUrl(contentManagementService.getDownloadUrl(zyRegulationBjmoaVO.getContentFileId()));
            zyRegulationBjmoaVO.setPdfDownloadUrl(contentManagementService.getDownloadUrl(zyRegulationBjmoaVO.getWatermarkPdfContentFileId()));
        } else if (pdfDownloadPermission) {
            // 如果不是主责部门文件管理员，但是拥有pdf下载权限，只提供pdf下载链接
            zyRegulationBjmoaVO.setPdfDownloadUrl(contentManagementService.getDownloadUrl(zyRegulationBjmoaVO.getWatermarkPdfContentFileId()));
        }

        return Result.OK(zyRegulationBjmoaVO);
    }

    @AutoLog("自研-轨道运营制度-新建/修订制度发布") @ApiOperation("新建/修订制度发布") @GetMapping("/create_or_edit")
    public Result<?> createOrEdit(@RequestParam(name = "qiqiaoRegulationId") String qiqiaoRegulationId, @RequestParam(name = "publishStatus", defaultValue = "1") String publishStatus) {
        log.info("[createOrEdit] qiqiaoRegulationId: " + qiqiaoRegulationId + " publishStatus: " + publishStatus);
        zyRegulationBjmoaService.createOrEdit(qiqiaoRegulationId, publishStatus);
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-新建党群及廉政建设管理制度发布") @ApiOperation("新建党群及廉政建设管理制度发布") @GetMapping("/create")
    public Result<?> create(@RequestParam(name = "qiqiaoRegulationId") String qiqiaoRegulationId) {
        log.info("[create] qiqiaoRegulationId: " + qiqiaoRegulationId);
        zyRegulationBjmoaService.create(qiqiaoRegulationId);
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-新建/修订董事会制度发布") @ApiOperation("新建/修订董事会制度发布") @GetMapping("/board_create_or_edit")
    public Result<?> createOrEditBoardRegulation(@RequestParam(name = "qiqiaoRegulationId") String qiqiaoRegulationId) {
        log.info("[createOrEditBoardRegulation] qiqiaoRegulationId: " + qiqiaoRegulationId);
        zyRegulationBjmoaService.createOrEditBoardRegulation(qiqiaoRegulationId);
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-推送OA") @ApiOperation("推送OA") @GetMapping("/initiate_oa_process")
    public Result<?> initiateOAProcess(@RequestParam(name = "qiqiaoRegulationId") String qiqiaoRegulationId ) {
        log.info("[initiateOAProcess] qiqiaoRegulationId: " + qiqiaoRegulationId);
        zyRegulationBjmoaService.initiateOAProcess(qiqiaoRegulationId);
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-董事会制度推送OA") @ApiOperation("董事会制度推送OA") @GetMapping("/initiate_board_oa_process")
    public Result<?> initiateBoardOAProcess(@RequestParam(name = "qiqiaoRegulationId") String qiqiaoRegulationId ) {
        log.info("[initiateBoardOAProcess] qiqiaoRegulationId: " + qiqiaoRegulationId);
        zyRegulationBjmoaService.initiateBoardOAProcess(qiqiaoRegulationId);
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-制度生效") @ApiOperation("制度生效") @GetMapping("/activate")
    public Result<?> activateByQiqiaoRegulationId(@RequestParam String qiqiaoRegulationId) {
        log.info("[activateByQiqiaoRegulationId] qiqiaoRegulationId: " + qiqiaoRegulationId);
        zyRegulationBjmoaService.activateByQiqiaoRegulationId(qiqiaoRegulationId);
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-制度废弃") @ApiOperation("制度废弃") @GetMapping("/inactivate")
    public Result<?> inactivateByIdentifier(@RequestParam String identifier) {
        log.info("[inactivateByIdentifier] identifier: " + identifier);
        zyRegulationBjmoaService.inactivateByIdentifier(identifier);
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-轮播图信息更新") @ApiOperation("轮播图信息更新") @GetMapping("/carousel_picture")
    public Result<?> carouselPictureUpdate() {
        final Result<?> result = zyRegulationBjmoaCarouselPictureService.carouselPicture();
        return result;
    }

    @AutoLog("自研-轨道运营制度-管理工具快捷入口列表") @ApiOperation("管理工具快捷入口列表")
    @GetMapping("/management_list") public Result<?> queryManagementToolEntryList() {
        log.info("[queryManagementToolEntryList] ");
        return zyRegulationBjmoaService.queryManagementToolEntryList();
    }

    @AutoLog("自研-轨道运营制度-制度数量查询") @ApiOperation("制度数量查询") @GetMapping("/statistics")
    public Result<List<ZyRegulationBjmoaStatisticsVO>> queryRegulationStatistics(@RequestParam Integer year) {
        List<ZyRegulationBjmoaStatisticsVO> zyRegulationBjmoaStatisticsVO =
            zyRegulationBjmoaService.queryRegulationStatistics(year);
        return Result.OK(zyRegulationBjmoaStatisticsVO);
    }

    @AutoLog("自研-轨道运营制度-临时技术变更制度数量") @ApiOperation("临时技术变更制度数量")
    @GetMapping("/temp_technical_changes_number")
    public Result<List<ZyRegulationBjmoaStatisticsVO>> queryTempTechnicalChangesRegulationNumber() {
        List<ZyRegulationBjmoaStatisticsVO> list = new ArrayList<>(1);
        ZyRegulationBjmoaStatisticsVO zyRegulationBjmoaStatisticsVO = new ZyRegulationBjmoaStatisticsVO();
        zyRegulationBjmoaStatisticsVO.setCategoryName("临时技术变更总数");
        zyRegulationBjmoaStatisticsVO.setNum(zyRegulationBjmoaService.queryTempTechnicalChangesRegulationNumber());
        list.add(zyRegulationBjmoaStatisticsVO);
        return Result.OK(list);
    }

    @AutoLog("自研-轨道运营制度-临时技术变更分页列表查询") @ApiOperation("分页列表查询") @GetMapping("/temp_technical_changes_list")
    public Result<Page<ZyRegulationTempBjmoaVO>> queryPageListTemp(RegulationTempQueryDTO queryDTO,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("[/temp_technical_changes_list] " + queryDTO);
        Page<ZyRegulationTempBjmoaVO> page = new Page<>(pageNo, pageSize);
        if (queryDTO == null) {
            queryDTO = new RegulationTempQueryDTO();
        }
        page = zyRegulationBjmoaService.queryTempPageList(page, queryDTO);
        return Result.OK(page);
    }

    @AutoLog("自研-轨道运营制度-根据id查询") @ApiOperation("根据id查询") @GetMapping("/temp_technical_changes_query_by_id")
    public Result<ZyRegulationTempBjmoaVO> tempQueryById(@RequestParam String id) {
        log.info("[temp_technical_changes_query_by_id] " + id);
        String mark = "";
        try {
            final LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null) {
                mark = sysUser.getRealname() + " " + sysUser.getDept();
            }
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }
        return Result.OK(zyRegulationBjmoaService.tempQueryById(id, mark));
    }

    @AutoLog("自研-轨道运营制度-制度发文回调接口") @ApiOperation("制度发文回调接口") @PostMapping("/oa_flow/approve")
    public Result<?> oaFlowApprove(@RequestBody String params) {
        log.info("[oaFlowApprove] params=" + params);
        try {
            final JSONObject jsonObject = JSON.parseObject(params);
            final String requestId = jsonObject.getString("requestId");
            if (StringUtils.isEmpty(requestId)) {
                log.warn("requestId is EMPTY!");
            } else {
                zyRegulationBjmoaService.updateQiqiaoRegulation(requestId);
            }
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-拉取七巧制度发布单") @ApiOperation("拉取七巧制度发布单") @PostMapping("/pull_qiqiao")
    public Result<?> pullRegulationFromQiqiqao() {
        log.info("[pullRegulationFromQiqiqao] start");
        zyRegulationBjmoaService.pullRegulationFromQiqiqao();
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-替换制度文件") @ApiOperation("替换制度文件") @PostMapping("/replace_file")
    public Result<?> replaceRegulationFile(@RequestParam String qiqiaoRegulationId, String contentfileId, String contentdocId) {
        log.info("[replace regulation file] start");
        zyRegulationBjmoaService.replaceRegulationFile(qiqiaoRegulationId, contentfileId, contentdocId);
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-替换PDF制度文件") @ApiOperation("替换PDF制度文件") @PostMapping("/replace_pdf_file")
    public Result<?> replacePDFRegulationFile(@RequestParam String qiqiaoRegulationId) {
        log.info("[replace pdf regulation file] start");
        zyRegulationBjmoaService.replacePDFRegulationFile(qiqiaoRegulationId);
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-替换批准发布流程PDF制度文件") @ApiOperation("替换批准发布流程PDF制度文件") @PostMapping("/replace_final_pdf_file")
    public Result<?> replaceFinalPDFRegulationFile(@RequestParam String qiqiaoRegulationId) {
        log.info("[replace final pdf regulation file] start");
        zyRegulationBjmoaService.replaceFinalPDFRegulationFile(qiqiaoRegulationId);
        return Result.OK();
    }

    @AutoLog("自研-轨道运营制度-七巧回调接口") @ApiOperation("七巧回调接口") @GetMapping("/qiqiao_callback")
    public Result<?> qiqiaoCallBack(@RequestParam String taskId, Map data) {
        log.info("[qiqiao callback] start");
        return Result.OK(zyRegulationBjmoaService.qiqiaoCallback(taskId, data));
    }
}
