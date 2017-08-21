package org.nostalie.auto.modify;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.nostalie.auto.pojo.ColumnInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Created by nostalie on 17-8-21.
 */
public class KylinSerializer extends JsonSerializer<BlackJadeKylin> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KylinSerializer.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public void serialize(BlackJadeKylin blackJadeKylin, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        Map<String, Class<?>> type = blackJadeKylin.getType();
        jsonGenerator.writeStartObject();
        for(String name : type.keySet()){
            try {
                if(type.get(name) == Date.class){
                    DateTime date = new DateTime(blackJadeKylin.get(name));
                    jsonGenerator.writeStringField(name,FORMATTER.print(date));
                }else {
                    jsonGenerator.writeObjectField(name, blackJadeKylin.get(name));
                }
            } catch (Exception e) {
                LOGGER.error("BlackJadeKylin json序列化失败,BlackJadeKylin: {}",blackJadeKylin,e);
                throw new RuntimeException("BlackJadeKylin json序列化失败");
            }
        }
        jsonGenerator.writeEndObject();
    }

    public static void main(String[] args) throws NotFoundException, CannotCompileException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, IOException {
        BlackJadeKylin kylin = BlackJadeKylin.builder().setField("name", String.class)
                .setField("age", int.class)
                .setField("bitthday", Date.class)
                .setField("saraly", BigDecimal.class).build();
        kylin.set("name","nsotalie")
                .set("age",23)
                .set("bitthday",new Date())
                .set("saraly",new BigDecimal("123.23130"));

        ObjectMapper mapper = new ObjectMapper();

        String value = mapper.writeValueAsString(kylin);
        System.out.println("value" + "=" +value);

        BlackJadeKylin kylin1 = mapper.readValue(value,BlackJadeKylin.class);
        System.out.println(kylin1.get("name"));
        System.out.println(kylin1.get("age"));
        System.out.println(kylin1.get("bitthday"));
        System.out.println(kylin1.get("saraly"));
    }
}
