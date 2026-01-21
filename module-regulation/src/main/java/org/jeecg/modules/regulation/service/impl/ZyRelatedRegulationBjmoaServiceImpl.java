package org.jeecg.modules.regulation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.regulation.entity.ZyRelatedRegulationBjmoa;
import org.jeecg.modules.regulation.mapper.ZyRelatedRegulationBjmoaMapper;
import org.jeecg.modules.regulation.service.IZyRelatedRegulationBjmoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Service public class ZyRelatedRegulationBjmoaServiceImpl
    extends ServiceImpl<ZyRelatedRegulationBjmoaMapper, ZyRelatedRegulationBjmoa>
    implements IZyRelatedRegulationBjmoaService {
    @Autowired private ZyRelatedRegulationBjmoaMapper zyRelatedRegulationBjmoaMapper;

    @Override public boolean saveRelation(final String regulationIdentifierA, final String versionA, final String codeA,
        final String regulationIdentifierB, final String versionB, final String codeB, final String regulationType,
        final String regulationName, final String traceid) {
        if (StringUtils.isEmpty(regulationIdentifierA) || StringUtils.isEmpty(regulationIdentifierB) || StringUtils
            .isEmpty(regulationType) || StringUtils.isEmpty(regulationName) || StringUtils.isEmpty(traceid)) {
            log.warn(traceid + " SAVE RELATION: INPUT EMPTY!");
            return false;
        }

        if (regulationIdentifierA.equals(regulationIdentifierB)) {
            log.warn(traceid + " SAME REGULATION IDENTIFIER " + regulationIdentifierA);
            return false;
        }

        final ZyRelatedRegulationBjmoa zyRelatedRegulationBjmoa = new ZyRelatedRegulationBjmoa();
        zyRelatedRegulationBjmoa.setRegulationIdentifierA(regulationIdentifierA);
        zyRelatedRegulationBjmoa.setRegulationIdentifierB(regulationIdentifierB);
        zyRelatedRegulationBjmoa.setRegulationType(regulationType);
        zyRelatedRegulationBjmoa.setRegulationName(regulationName);
        zyRelatedRegulationBjmoa.setCodeA(codeA);
        zyRelatedRegulationBjmoa.setCodeB(codeB);
        zyRelatedRegulationBjmoa.setVersionA(versionA);
        zyRelatedRegulationBjmoa.setVersionB(versionB);
        return save(zyRelatedRegulationBjmoa);
    }

    @Override public List<ZyRelatedRegulationBjmoa> queryByRegulationIdentifier(final String regulationIdentifier) {
        if (StringUtils.isEmpty(regulationIdentifier)) {
            return new ArrayList<>();
        }

        return zyRelatedRegulationBjmoaMapper.queryByRegulationIdentifier(regulationIdentifier);
    }

    @Override
    public List<ZyRelatedRegulationBjmoa> queryByRegulationIdentifierAndVersion(final String regulationIdentifier,
        final String regulationVersion) {
        if (StringUtils.isEmpty(regulationIdentifier) || StringUtils.isEmpty(regulationIdentifier)) {
            return new ArrayList<>();
        }

        return zyRelatedRegulationBjmoaMapper.queryByRegulationIdentifierAndVersion(regulationIdentifier,
            regulationVersion);
    }

    @Override
    public List<ZyRelatedRegulationBjmoa> queryByRegulationIdentifierAndVersionAndCode(final String regulationIdentifier,
       final String regulationVersion, final String regulationCode) {
        if (StringUtils.isEmpty(regulationIdentifier) || StringUtils.isEmpty(regulationIdentifier) || StringUtils.isEmpty(regulationCode)) {
            return new ArrayList<>();
        }

        return zyRelatedRegulationBjmoaMapper.queryByRegulationIdentifierAndVersionAndCode(regulationIdentifier,
                regulationVersion, regulationCode);
    }

    @Override public int deleteByRegulationIdentifier(final String regulationIdentifier) {
        if (StringUtils.isEmpty(regulationIdentifier)) {
            return 0;
        }

        return zyRelatedRegulationBjmoaMapper.deleteByRegulationIdentifier(regulationIdentifier);
    }

    @Override public void truncateTable() {
        zyRelatedRegulationBjmoaMapper.truncateTable();
    }

}
