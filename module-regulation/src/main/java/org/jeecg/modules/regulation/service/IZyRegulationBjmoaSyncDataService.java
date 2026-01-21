package org.jeecg.modules.regulation.service;

/**
 * @author zhouwei
 * @date 2024/10/29
 */
public interface IZyRegulationBjmoaSyncDataService {

    void syncEmergencyRegulationFromQiqiaoToDatabase();

    void syncPublishedEmergencyRegulation(String qiqiaoEmergencyId);

    void syncModifiedRegulation(String qiqiaoRegulationId);

    void syncPublishedRegulation(String qiqiaoRegulationId);

    void syncRegulationToDatabase(String qiqiaoRegulationId);

}
