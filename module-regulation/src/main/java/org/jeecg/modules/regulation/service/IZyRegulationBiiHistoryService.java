package org.jeecg.modules.regulation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiHistory;

import java.util.Date;
import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
public interface IZyRegulationBiiHistoryService extends IService<ZyRegulationBiiHistory> {
    List<ZyRegulationBiiHistory> queryByIdentifier(String identifier);

    int updateAbolishTime(String identifier, Date abolishTime);
}
