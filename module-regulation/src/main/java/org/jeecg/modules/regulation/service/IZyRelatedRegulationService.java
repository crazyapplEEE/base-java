package org.jeecg.modules.regulation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.regulation.entity.ZyRead;
import org.jeecg.modules.regulation.entity.ZyRelatedRegulation;

import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
public interface IZyRelatedRegulationService extends IService<ZyRelatedRegulation> {
    boolean saveRelation(String regulationIdentifierA, String regulationIdentifierB);

    List<ZyRelatedRegulation> queryByRegulationIdentifier(String regulationIdentifier);

    int deleteByRegulationIdentifier(String regulationIdentifier);
}
