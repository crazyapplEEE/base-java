package org.jeecg.modules.regulation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiDept;
import org.jeecg.modules.regulation.mapper.ZyRegulationBiiDeptMapper;
import org.jeecg.modules.regulation.service.IZyRegulationBiiDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Service public class ZyRegulationBiiDeptServiceImpl extends ServiceImpl<ZyRegulationBiiDeptMapper, ZyRegulationBiiDept>
    implements IZyRegulationBiiDeptService {
    @Autowired private ZyRegulationBiiDeptMapper zyRegulationBiiDeptMapper;

    @Override
    public List<ZyRegulationBiiDept> getByRegulationCodeAndVersion(final String regulationCode, final String version) {
        if (StringUtils.isEmpty(regulationCode) || StringUtils.isEmpty(version)) {
            return new ArrayList<>();
        }

        return zyRegulationBiiDeptMapper.getByRegulationCodeAndVersion(regulationCode, version);
    }

    @Override public List<ZyRegulationBiiDept> getByQiqiaoDeptIdList(final List<String> deptIdList) {
        if (CollectionUtils.isEmpty(deptIdList)) {
            return null;
        }

        return zyRegulationBiiDeptMapper.getByQiqiaoDeptIdList(deptIdList);
    }
}
