package org.jeecg.modules.regulation.service;

public interface IZyOaRequestBjmoaService {

    void approveRegulationPublish(String requestId, String yfrq);

    void rejectRegulationPublish(String requestId);
}
