package codes.recursive.messaging;

import codes.recursive.domain.Reading;
import codes.recursive.repository.ReadingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.context.env.Environment;
import io.micronaut.http.MediaType;
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.websocket.WebSocketBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@MqttSubscriber
public class GreenThumbConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(GreenThumbConsumer.class);
    private final WebSocketBroadcaster broadcaster;
    private final ReadingRepository readingRepository;
    private final Environment environment;

    public GreenThumbConsumer(WebSocketBroadcaster broadcaster, ReadingRepository readingRepository, Environment environment) {
        this.broadcaster = broadcaster;
        this.readingRepository = readingRepository;
        this.environment = environment;
    }

    @Topic("greenthumb/readings")
    public void receive(Map<String, Object> data) throws JsonProcessingException {
        Reading reading = new Reading(data);
        if( environment.getActiveNames().contains("oraclecloud") ) {
            readingRepository.saveAsync(reading);
        }
        else {
            LOG.info("[localhost]: skipping persistence");
        }
        broadcaster.broadcastAsync(data, MediaType.APPLICATION_JSON_TYPE);
    }
}