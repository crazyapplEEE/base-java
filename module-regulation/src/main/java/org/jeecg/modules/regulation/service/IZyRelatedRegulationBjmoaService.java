package org.jeecg.modules.regulation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.regulation.entity.ZyRelatedRegulationBjmoa;

import java.util.List;

/**
 * @author Ran Meng
 * @date 2023-09-23
 */
public interface IZyRelatedRegulationBjmoaService extends IService<ZyRelatedRegulationBjmoa> {
    boolean saveRelation(String regulationIdentifierA, String versionA, String codeA, String regulationIdentifierB, String versionB, String codeB, String regulationType, String regulationName, String traceid);

    List<ZyRelatedRegulationBjmoa> queryByRegulationIdentifier(String regulationIdentifier);

    List<ZyRelatedRegulationBjmoa> queryByRegulationIdentifierAndVersion(String regulationIdentifier, String regulationVersionA);

    List<ZyRelatedRegulationBjmoa> queryByRegulationIdentifierAndVersionAndCode(String regulationIdentifier,
                                                                                String regulationVersion, String regulationCode);

    int deleteByRegulationIdentifier(String regulationIdentifier);

    void truncateTable();
}
