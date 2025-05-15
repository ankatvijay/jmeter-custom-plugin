package com.ankat.avro;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class AvroSerializer<T extends SpecificRecordBase> implements Serializer<T> {
    private Schema schema;

    @Override
    public void configure(Map<String, ?> map, boolean flag) {
        log.info("###Before configure: {}", map.get("avroFilePath"));
        schema = (Schema) map.get("avroFilePath");
    }

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            byte[] result = null;
            if (Objects.nonNull(data)) {
                log.debug("data='{}'", data);
                schema = (Schema) JMeterUtils.getJMeterProperties().get("avroFile");
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                BinaryEncoder binaryEncoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null);
                DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
                datumWriter.write(data, binaryEncoder);
                binaryEncoder.flush();
                byteArrayOutputStream.close();
                result = byteArrayOutputStream.toByteArray();
                log.debug("serialized data='{}'", DatatypeConverter.printHexBinary(result));
            }
            return result;
        } catch (IOException ex) {
            throw new SerializationException("Can't serialize data='" + data + "' for topic='" + topic + "'", ex);
        } catch (Exception ex) {
            throw new SerializationException("Can't serialize data='" + data + "' for topic='" + topic + "'", ex);
        }
    }
}