package org.jeecg.modules.regulation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaDept;
import org.jeecg.modules.regulation.mapper.ZyRegulationBjmoaDeptMapper;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Service public class ZyRegulationBjmoaDeptServiceImpl
    extends ServiceImpl<ZyRegulationBjmoaDeptMapper, ZyRegulationBjmoaDept> implements IZyRegulationBjmoaDeptService {
    @Autowired private ZyRegulationBjmoaDeptMapper zyRegulationBjmoaDeptMapper;

    @Override public List<ZyRegulationBjmoaDept> getByRegulationCodeAndVersion(final String regulationCode,
        final String version) {
        if (StringUtils.isEmpty(regulationCode) || StringUtils.isEmpty(version)) {
            return new ArrayList<>();
        }

        return zyRegulationBjmoaDeptMapper.getByRegulationCodeAndVersion(regulationCode, version);
    }

    @Override
    public List<ZyRegulationBjmoaDept> getByRegulationIdentifier(String regulationIdentifier) {
        if (StringUtils.isEmpty(regulationIdentifier)) {
            return new ArrayList<>();
        }

        return zyRegulationBjmoaDeptMapper.getByIdentifier(regulationIdentifier);
    }

    @Override public List<ZyRegulationBjmoaDept> getByQiqiaoDeptIdList(final List<String> deptIdList) {
        if (CollectionUtils.isEmpty(deptIdList)) {
            return null;
        }

        return zyRegulationBjmoaDeptMapper.getByQiqiaoDeptIdList(deptIdList);
    }
}
