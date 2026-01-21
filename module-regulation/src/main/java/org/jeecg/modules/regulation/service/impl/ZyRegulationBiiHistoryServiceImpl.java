package org.jeecg.modules.regulation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiHistory;
import org.jeecg.modules.regulation.mapper.ZyRegulationBiiHistoryMapper;
import org.jeecg.modules.regulation.service.IZyRegulationBiiHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Service public class ZyRegulationBiiHistoryServiceImpl
        extends ServiceImpl<ZyRegulationBiiHistoryMapper, ZyRegulationBiiHistory>
        implements IZyRegulationBiiHistoryService {
    @Autowired private ZyRegulationBiiHistoryMapper zyRegulationBiiHistoryMapper;

    @Override public List<ZyRegulationBiiHistory> queryByIdentifier(String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            return new ArrayList<>();
        }
        return zyRegulationBiiHistoryMapper.queryByIdentifier(identifier);
    }

    @Override public int updateAbolishTime(final String identifier, final Date abolishTime) {
        if (StringUtils.isEmpty(identifier) || abolishTime == null) {
            log.warn("INPUT IS EMPTY! identifier=" + identifier + ", abolishTime=" + abolishTime);
            return 0;
        }
        return zyRegulationBiiHistoryMapper.updateAbolishTime(identifier, abolishTime);
    }
}
