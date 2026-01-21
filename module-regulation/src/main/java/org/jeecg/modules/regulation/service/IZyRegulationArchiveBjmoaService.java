package org.jeecg.modules.regulation.service;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zhouwei
 * @date 2024/1/30
 */
public interface IZyRegulationArchiveBjmoaService {

    JSONObject filed(String regulationIdentifier);

    void pushArchiveResultToQiqiao(JSONObject jsonObject);

    JSONObject syncPublishedRegulations();

    JSONObject batchArchiveByYear(Integer publishYear);

}
