package org.jeecg.modules.regulation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaHistory;

import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
public interface IZyRegulationBjmoaHistoryService extends IService<ZyRegulationBjmoaHistory> {
    List<ZyRegulationBjmoaHistory> queryByIdentifier(String identifier);

    List<ZyRegulationBjmoaHistory> queryByIdentifierAndVersion(String identifier, String version);

    List<ZyRegulationBjmoaHistory> queryByIdentifierAndVersionAndCode(String identifier, String version, String code);

    void inactivateByIdentifier(String identifier);

    void createOaRequest(ZyRegulationBjmoaHistory zyRegulationBjmoaHistory, String traceId);

    ZyRegulationBjmoaHistory queryByRequestId(String requestId);
}
