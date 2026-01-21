package org.jeecg.modules.regulation.service;

public interface IZyOaRequestBiiService {
    String createRegulationPlan(String qiqiaoUserId, String qiqiaoRegulationPlanId);

    void approveRegulationPlan(String requestId);

    void rejectRegulationPlan(String requestId);

    String createSpecialAudit(String qiqiaoUserId, String qiqiaoRegulationInfoId);

    void approveSpecialAudit(String requestId, String creatorId, String oaDocIds);

    void rejectSpecialAudit(String requestId);

    String createShiyebuSpecialAudit(String qiqiaoUserId, String qiqiaoRegulationInfoId);

    void approveShiyebuSpecialAudit(String requestId);

    void rejectShiyebuSpecialAudit(String requestId);
}
