package org.jeecg.modules.regulation.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.jeecg.modules.content.constant.WpsOperateType;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service @Slf4j public class TopicOfficeConvertConsumer
    implements MessageListenerConcurrently, RocketMQPushConsumerLifecycleListener {
    @Autowired private IZyRegulationBjmoaService zyRegulationBjmoaService;
    @Value("${rocketmq.consumer.office_convert.group}") private String consumerGroup;
    @Value("${rocketmq.consumer.office_convert.topic_bjmoa}") private String topicBjmoa;
    @Value("${rocketmq.name-server}") private String nameServer;
    private DefaultMQPushConsumer consumer;

    @PostConstruct public void init() throws MQClientException {
        consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setNamesrvAddr(nameServer);
        // Set the consumer group to broadcast mode
        consumer.setMessageModel(MessageModel.BROADCASTING);

        // Set the starting offset for each topic
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        // Subscribe to multiple topics
        consumer.subscribe(topicBjmoa, "*");

        consumer.registerMessageListener(this);
        consumer.start();
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
        for (MessageExt message : messages) {
            try {
                final String body = new String(message.getBody());
                log.info("Received message: " + body);
                final JSONObject jsonObject = JSON.parseObject(body);
                final String method = jsonObject.getString("method");

                final String topic = message.getTopic();
                log.info("Received message from topic {}: {}", topic, body);
                // Process the message based on the topic
                if (topicBjmoa.equals(topic)) {
                    if (WpsOperateType.OFFICE_WRAP_HEADER.equals(method)) {
                        zyRegulationBjmoaService.wrapHeaderCallback(jsonObject);
                    } else if (WpsOperateType.OFFICE_CONVERT.equals(method)) {
                        zyRegulationBjmoaService.pdfConversionCallback(jsonObject);
                    } else {
                        zyRegulationBjmoaService.addWatermarkCallback(jsonObject);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Exception caught while processing message: " + e.getMessage());
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    @Override public void prepareStart(DefaultMQPushConsumer defaultMQPushConsumer) {
        log.info("prepareStart: consumergroup =" + defaultMQPushConsumer.getConsumerGroup());
    }

    public void shutdown() {
        // Shutdown the consumer
        consumer.shutdown();
    }
}
