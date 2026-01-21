package org.jeecg.modules.regulation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.regulation.entity.ZyRelatedRegulation;
import org.jeecg.modules.regulation.mapper.ZyRelatedRegulationMapper;
import org.jeecg.modules.regulation.service.IZyRelatedRegulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Service
public class ZyRelatedRegulationServiceImpl extends ServiceImpl<ZyRelatedRegulationMapper, ZyRelatedRegulation> implements IZyRelatedRegulationService {
    @Autowired private ZyRelatedRegulationMapper zyRelatedRegulationMapper;

    @Override public boolean saveRelation(final String regulationIdentifierA, final String regulationIdentifierB) {
        if (StringUtils.isEmpty(regulationIdentifierA) || StringUtils.isEmpty(regulationIdentifierB)) {
            log.warn("INPUT EMPTY!");
            return false;
        }

        if (regulationIdentifierA.equals(regulationIdentifierB)) {
            log.warn("SAME REGULATION IDENTIFIER " + regulationIdentifierA);
            return false;
        }

        final ZyRelatedRegulation zyRelatedRegulation = new ZyRelatedRegulation();
        zyRelatedRegulation.setRegulationIdentifierA(regulationIdentifierA);
        zyRelatedRegulation.setRegulationIdentifierB(regulationIdentifierB);
        return save(zyRelatedRegulation);
    }

    @Override public List<ZyRelatedRegulation> queryByRegulationIdentifier(final String regulationIdentifier) {
        if (StringUtils.isEmpty(regulationIdentifier)) {
            return new ArrayList<>();
        }

        return zyRelatedRegulationMapper.queryByRegulationIdentifier(regulationIdentifier);
    }

    @Override public int deleteByRegulationIdentifier(final String regulationIdentifier) {
        if (StringUtils.isEmpty(regulationIdentifier)) {
            return 0;
        }

        return zyRelatedRegulationMapper.deleteByRegulationIdentifier(regulationIdentifier);
    }
}
