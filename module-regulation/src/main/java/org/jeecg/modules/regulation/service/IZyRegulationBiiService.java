package org.jeecg.modules.regulation.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.regulation.dto.RegulationQueryDTO;
import org.jeecg.modules.regulation.entity.ZyRegulationBii;
import org.jeecg.modules.regulation.vo.ZyRegulationBiiVO;

import java.util.Set;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
public interface IZyRegulationBiiService extends IService<ZyRegulationBii> {
    void syncOldRegulationList();

    Page<ZyRegulationBiiVO> queryNewestVersionPageList(Page<ZyRegulationBiiVO> page, RegulationQueryDTO queryDTO);

    int inactivateById(Integer id);

    void inactivateByIdentifier(String identifier);

    void rebuildIndex(Set<String> fileIdentifiersToAdd, Set<String> fileIdentifiersToUpdate,
                      Set<String> fileIdentifiersToDelete);

    ZyRegulationBiiVO queryById(Integer id, String mark);

    void createOrEdit(final String qiqiaoRegulationId);

    ZyRegulationBii queryByIdentifier(String identifier);

    void pdfConversionCallback(JSONObject jsonObject);

    void syncToQiqiao(Integer minId, Integer maxId);

    void insertRegulationToQiqiao(Integer regulationBiiId);
}
