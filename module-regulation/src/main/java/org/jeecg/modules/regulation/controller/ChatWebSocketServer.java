package org.jeecg.modules.regulation.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.regulation.service.ChatApiService;
import org.jeecg.modules.regulation.service.RegulationAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 描述：websocket 服务端
 *
 * @author https:www.unfbx.com
 * @date 2023-03-23
 */
@Slf4j @Component @ServerEndpoint("/websocket/chat") public class ChatWebSocketServer {

    /**
     * 为了保存在线用户信息，在方法中新建一个list存储一下【实际项目依据复杂度，可以存储到数据库或者缓存】
     */
    private final static List<Session> SESSIONS = Collections.synchronizedList(new ArrayList<>());
    private static final CopyOnWriteArraySet<ChatWebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();
    /**
     * 用来存放每个客户端对应的 ChatWebSocketServer对象
     */
    private static final ConcurrentHashMap<String, ChatWebSocketServer> webSocketMap = new ConcurrentHashMap();
    //在线总数
    private static int onlineCount;
    private static ChatApiService chatApiService;
    private static RegulationAiService regulationAiService;
    private static String promptTemplate;
    //当前会话
    private Session session;
    //用户id -目前是按浏览器随机生成
    private String sessionId;

    /**
     * 获取当前连接数
     *
     * @return
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * 当前连接数加一
     */
    public static synchronized void addOnlineCount() {
        ChatWebSocketServer.onlineCount++;
    }

    /**
     * 当前连接数减一
     */
    public static synchronized void subOnlineCount() {
        ChatWebSocketServer.onlineCount--;
    }

    @Autowired public void setChatApiService(ChatApiService chatApiService) {
        ChatWebSocketServer.chatApiService = chatApiService;
    }

    @Autowired @Lazy public void setRegulationAiService(RegulationAiService regulationAiService) {
        ChatWebSocketServer.regulationAiService = regulationAiService;
    }

    @Value("${chat-service.knowledge_base.prompt_template}") public void setPromptTemplate(String promptTemplate) {
        ChatWebSocketServer.promptTemplate = promptTemplate;
    }

    /**
     * 建立连接
     *
     * @param session
     */
    @OnOpen public void onOpen(Session session) {
        this.session = session;
        this.sessionId = session.getId();
        webSocketSet.add(this);
        SESSIONS.add(session);
        if (webSocketMap.containsKey(sessionId)) {
            webSocketMap.remove(sessionId);
            webSocketMap.put(sessionId, this);
        } else {
            webSocketMap.put(sessionId, this);
            addOnlineCount();
        }
        log.info("[连接ID:{}] 建立连接, 当前连接数:{}", this.sessionId, getOnlineCount());
    }

    /**
     * 断开连接
     */
    @OnClose public void onClose() {
        webSocketSet.remove(this);
        if (webSocketMap.containsKey(sessionId)) {
            webSocketMap.remove(sessionId);
            subOnlineCount();
        }
        log.info("[连接ID:{}] 断开连接, 当前连接数:{}", sessionId, getOnlineCount());
    }

    /**
     * 发送错误
     *
     * @param session
     * @param error
     */
    @OnError public void onError(Session session, Throwable error) {
        log.info("[连接ID:{}] 错误原因:{}", this.sessionId, error.getMessage());
        error.printStackTrace();
    }

    /**
     * 接收到客户端消息
     *
     * @param msg
     */
    @OnMessage public void onMessage(String msg) {
        log.info("[连接ID:{}] 收到消息:{}", this.sessionId, msg);

        // 查找知识库
        final JSONArray knowledgeDocs = regulationAiService.queryKnowledge(msg);

        StringBuilder knowledgeSb = new StringBuilder();

        Set<String> knowledgeSourceSet = new TreeSet<>();

        Set<String> identifierSet = new HashSet<>();
        Map<String, List<String>> identifier2KnowledgeList = new HashMap<>();
        Set<String> knowledgeSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(knowledgeDocs)) {
            for (int i = 0; i < knowledgeDocs.size(); i++) {
                final JSONObject knowledgeDoc = knowledgeDocs.getJSONObject(i);
                if (knowledgeDoc == null) {
                    continue;
                }

                final JSONObject metadata = knowledgeDoc.getJSONObject("metadata");
                log.info("metadata: " + metadata);
                if (metadata == null) {
                    continue;
                }

                String knowledge = metadata.getString("full_knowledge");
                if (StringUtils.isEmpty(knowledge)) {
                    knowledge = knowledgeDoc.getString("page_content");
                }
                if (knowledgeSet.contains(knowledge)) {
                    continue;
                }
                knowledgeSet.add(knowledge);
                knowledgeSb.append(knowledge).append("\n");

                String source = metadata.getString("source");
                final int idxLastSlash = source.lastIndexOf("/");
                final int idxLastUnderline = source.lastIndexOf("_");
                String identifier = null;

                if (idxLastSlash > 0 && idxLastUnderline > 0) {
                    identifier = source.substring(idxLastSlash + 1, idxLastUnderline);
                    log.info("identifier: " + identifier);
                    identifierSet.add(identifier);

                    if (identifier2KnowledgeList.containsKey(identifier)) {
                        identifier2KnowledgeList.get(identifier).add(knowledge);
                    } else {
                        List<String> knowledgeList = new ArrayList<>(1);
                        knowledgeList.add(knowledge);
                        identifier2KnowledgeList.put(identifier, knowledgeList);
                    }
                }

                if (idxLastUnderline > 0) {
                    source = source.substring(idxLastUnderline + 1);
                }
                final int idxLastDot = source.lastIndexOf(".");
                if (idxLastDot > 0) {
                    source = source.substring(0, idxLastDot);
                }

                final int page = metadata.getInteger("page") + 1;
                knowledgeSourceSet.add("《" + source + "》第" + page + "页");
            }
        }
        String prompt;
        String knowledge = null;
        if (knowledgeSb.length() == 0) {
            prompt = msg;
        } else {
            prompt = promptTemplate.replace("{knowledge}", knowledgeSb.toString()).replace("{question}", msg);

            StringBuilder knowledgeSourceSb = new StringBuilder();
            for (final String knowledgeSource : knowledgeSourceSet) {
                knowledgeSourceSb.append(knowledgeSource).append(",");
            }
            if (knowledgeSourceSb.length() > 0) {
                knowledge = knowledgeSourceSb.substring(0, knowledgeSourceSb.length() - 1);
            }
        }

        // 自定义比较器
        Comparator<String> comparator = (s1, s2) -> {
            // 提取中文数字部分进行比较
            int num1 = StringUtils.extractChineseNumber(s1);
            int num2 = StringUtils.extractChineseNumber(s2);
            return Integer.compare(num1, num2);
        };

        identifier2KnowledgeList.forEach((key, value) -> {
            value.sort(comparator);
        });

        log.info("prompt:{}", prompt);
        chatApiService.requestMsg(session, prompt, knowledge, identifierSet, identifier2KnowledgeList);
    }
}
