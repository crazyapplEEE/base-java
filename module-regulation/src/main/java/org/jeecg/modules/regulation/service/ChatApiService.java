package org.jeecg.modules.regulation.service;

import javax.websocket.Session;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zmy
 */
public interface ChatApiService {
    void requestMsg(Session session, String msg, String source, Set<String> identifierSet, Map<String, List<String>> identifier2KnowledgeList);
}
