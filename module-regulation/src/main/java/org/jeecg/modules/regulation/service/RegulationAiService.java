package org.jeecg.modules.regulation.service;

import com.alibaba.fastjson.JSONArray;

public interface RegulationAiService {
    JSONArray queryKnowledge(String question);

    void updateDb(String directoryPath, String identifiersToAdd, String identifiersToUpdate, String identifiersToDelete);
}
