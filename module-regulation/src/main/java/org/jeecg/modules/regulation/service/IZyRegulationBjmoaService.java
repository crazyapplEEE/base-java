package org.jeecg.modules.regulation.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.regulation.dto.RegulationQueryDTO;
import org.jeecg.modules.regulation.dto.RegulationTempQueryDTO;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoa;
import org.jeecg.modules.regulation.vo.ZyRegulationBjmoaStatisticsVO;
import org.jeecg.modules.regulation.vo.ZyRegulationBjmoaVO;
import org.jeecg.modules.regulation.vo.ZyRegulationTempBjmoaVO;

import java.util.List;
import java.util.Map;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
public interface IZyRegulationBjmoaService extends IService<ZyRegulationBjmoa> {
    ZyRegulationBjmoaVO queryById(Integer id, String mark);

    void createOrEdit(String qiqiaoRegulationId, String publishStatus);

    void create(String qiqiaoRegulationId);

    void createOrEditBoardRegulation(String qiqiaoRegulationId);

    void initiateOAProcess(String qiqiaoRegulationId);

    void initiateBoardOAProcess(String qiqiaoRegulationId);

    void activateByQiqiaoRegulationId(String qiqiaoRegulationId);

    void inactivateByIdentifier(String identifier);

    void inactivateRelatedByIdentifier(String identifier);

    void wrapHeaderCallback(JSONObject jsonObject);

    void pdfConversionCallback(JSONObject jsonObject);

    void addWatermarkCallback(JSONObject jsonObject);

    Page<ZyRegulationBjmoaVO> queryNewestVersionPageList(Page<ZyRegulationBjmoaVO> page, RegulationQueryDTO queryDTO);

    ZyRegulationBjmoa queryByIdentifier(String identifier);

    Result<?> queryManagementToolEntryList();

    List<ZyRegulationBjmoaStatisticsVO> queryRegulationStatistics(Integer year);

    Page<ZyRegulationTempBjmoaVO> queryTempPageList(Page<ZyRegulationTempBjmoaVO> page, RegulationTempQueryDTO queryDTO);
    
    int queryTempTechnicalChangesRegulationNumber();

    ZyRegulationTempBjmoaVO tempQueryById(final String id, final String mark);
    
    boolean queryDownloadWordPermission(String loginid, String deptId);

    boolean queryDownloadPdfPermission(String loginid, String levelId, String responsibleDepartment, String identifier);

    void updateQiqiaoRegulation(String requestId);

    void pullRegulationFromQiqiqao();

    void replaceRegulationFile(String qiqiaoRegulationId, String contentFileId, String ContentDocId);

    void replacePDFRegulationFile(String qiqiaoRegulationId);

    void replaceFinalPDFRegulationFile(String qiqiaoRegulationId);

    JSONObject qiqiaoCallback(String taskId, Map data);
}
