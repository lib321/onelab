package com.onelab.microservices.event;

import com.onelab.microservices.dto.KafkaMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String topic, String action, Object message) {
        KafkaMessageDTO kafkaMessage = new KafkaMessageDTO(action, message);
        kafkaTemplate.send(topic, kafkaMessage);
        log.info("Отправлено Kafka сообщение: topic='{}', action='{}', data={}", topic, action, message);
    }
}
