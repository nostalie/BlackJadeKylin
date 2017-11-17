package org.nostalie.auto.modify;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * @author nostalie on 17-8-21.
 */
public class KylinDeserializer extends JsonDeserializer<BlackJadeKylin> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KylinDeserializer.class);

    public BlackJadeKylin deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        BlackJadeKylin kylin;
        try {
            BlackJadeKylin.Builder builder = BlackJadeKylin.builder();
            JsonNode nodes = jp.getCodec().readTree(jp);
            for (Iterator<Map.Entry<String, JsonNode>> it = nodes.getFields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                builder.setField(entry.getKey(), entry.getValue().getClass());
            }
            kylin = builder.build();
            for (Iterator<Map.Entry<String, JsonNode>> it = nodes.getFields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                kylin.set(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            LOGGER.debug("BlackJadeKylin 反序列化失败",e);
            throw new RuntimeException("BlackJadeKylin 反序列化失败",e);
        }
        return kylin;
    }

    public static void main(String[] args) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime dateTime = new DateTime(new Date());
        System.out.println(formatter.print(dateTime));
    }
}
