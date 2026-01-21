package org.jeecg.modules.regulation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaDept;

import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
public interface IZyRegulationBjmoaDeptService extends IService<ZyRegulationBjmoaDept> {
    List<ZyRegulationBjmoaDept> getByRegulationCodeAndVersion(String regulationCode, String version);

    List<ZyRegulationBjmoaDept> getByRegulationIdentifier(String regulationIdentifier);

    List<ZyRegulationBjmoaDept> getByQiqiaoDeptIdList(List<String> deptIdList);
}
