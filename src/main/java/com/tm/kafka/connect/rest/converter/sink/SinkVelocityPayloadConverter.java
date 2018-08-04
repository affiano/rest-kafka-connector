package com.tm.kafka.connect.rest.converter.sink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tm.kafka.connect.rest.RestSinkConnectorConfig;
import com.tm.kafka.connect.rest.converter.SinkRecordToPayloadConverter;
import com.tm.kafka.connect.rest.http.payload.Payload;
import com.tm.kafka.connect.rest.http.payload.StringPayload;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;


public class SinkVelocityPayloadConverter implements SinkRecordToPayloadConverter {

  private ObjectMapper mapper = new ObjectMapper();

  private VelocityContext globalContext;
  private Template template;

  public Payload<String> convert(SinkRecord record) throws IOException {
    StringWriter sw = new StringWriter();

    VelocityContext context = new VelocityContext(globalContext);

    context.put("topic", record.topic());
    context.put("partition", record.kafkaPartition());
    context.put("key", record.key());
    context.put("timestamp", record.timestamp());
    context.put("schema", record.valueSchema());
    context.put("value", mapper.readValue((String) record.value(), Map.class));

    template.merge(context, sw);

    return new StringPayload(sw.toString());
  }

  @Override
  public void start(RestSinkConnectorConfig config) {
    Velocity.init();
    globalContext = new VelocityContext();
    template = Velocity.getTemplate(config.getVelocityTemplate());
  }

}
