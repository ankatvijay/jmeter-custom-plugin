/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.ankat.config;

import com.ankat.utils.VariableSettings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class KafkaProducerConfig<K, V> extends ConfigTestElement implements ConfigElement, TestBean, TestStateListener, Serializable {

    @Getter @Setter private KafkaProducer<K, V> kafkaProducer = null;
    @Getter @Setter private List<VariableSettings> extraConfigs;
    @Getter @Setter private String kafkaBrokers;
    @Getter @Setter private String batchSize; // default: 16384
    @Getter @Setter private String clientId;
    @Getter @Setter private String serializerKey;
    @Getter @Setter private String serializerValue;
    @Getter @Setter private String securityType;
    @Getter @Setter private String kafkaSslKeystore; // Kafka ssl keystore (include path information); e.g; "server.keystore.jks"
    @Getter @Setter private String kafkaSslKeystorePassword; // Keystore Password
    @Getter @Setter private String kafkaSslTruststore;
    @Getter @Setter private String kafkaSslTruststorePassword;
    @Getter @Setter private String kafkaSslPrivateKeyPass;
    @Getter @Setter private String kafkaProducerClientVariableName;

    @Override
    public void addConfigElement(ConfigElement config) {
    }

    @Override
    public void testStarted() {
        this.setRunningVersion(true);
        TestBeanHelper.prepare(this);
        JMeterVariables variables = getThreadContext().getVariables();
        if (variables.getObject(kafkaProducerClientVariableName) != null) {
            log.error("Kafka Client is already running.");
        } else {
            if ((Objects.nonNull(getSerializerKey()) && !getSerializerKey().trim().isEmpty()) && (Objects.nonNull(getSerializerValue()) && !getSerializerValue().trim().isEmpty())) {
                synchronized (this) {
                    try {
                        Serializer<K> producerSerializerKey = createSerializer(getSerializerKey());
                        Serializer<V> producerSerializerValue = createSerializer(getSerializerValue());
                        kafkaProducer = new KafkaProducer<>(getProperties(), producerSerializerKey, producerSerializerValue);
                        variables.putObject(kafkaProducerClientVariableName, kafkaProducer);
                        variables.put("producerSerializerKeyVariableName", getSerializerKey());
                        variables.put("producerSerializerValueVariableName", getSerializerValue());
                        log.info("Kafka Producer client successfully Initialized");
                    } catch (Exception e) {
                        log.error("Error establishing Kafka producer client!", e);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Serializer<T> createSerializer(String serializerClass) throws ReflectiveOperationException {
        return (Serializer<T>) Class.forName(serializerClass).getDeclaredConstructor().newInstance();
    }

    private Properties getProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokers());
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, getBatchSize());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, getClientId());

        //props.put(ProducerConfig.RETRIES_CONFIG, 20);
        // props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, "500");
        // props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "5000");
        // props.put(ProducerConfig.ACKS_CONFIG, "all");

        props.put("security.protocol", getSecurityType().replaceAll("securityType.", "").toUpperCase());

        log.debug("Additional Config Size::: " + getExtraConfigs().size());
        if (!getExtraConfigs().isEmpty()) {
            log.info("Setting up Additional properties");
            for (VariableSettings entry : getExtraConfigs()) {
                props.put(entry.getConfigKey(), entry.getConfigValue());
                log.debug(String.format("Adding property : %s", entry.getConfigKey()));
            }
        }

        if (getSecurityType().equalsIgnoreCase("securityType.ssl") || getSecurityType().equalsIgnoreCase("securityType.sasl_ssl")) {
            log.info("Kafka security type: " + getSecurityType().replaceAll("securityType.", "").toUpperCase());
            log.info("Setting up Kafka {} properties", getSecurityType());
            if (!getKafkaSslKeystore().isEmpty()) {
                props.put("ssl.truststore.location", getKafkaSslTruststore());
                props.put("ssl.truststore.password", getKafkaSslTruststorePassword());
            }
            if (!getKafkaSslKeystore().isEmpty()) {
                props.put("ssl.keystore.location", getKafkaSslKeystore());
                props.put("ssl.keystore.password", getKafkaSslKeystorePassword());
                props.put("ssl.key.password", getKafkaSslPrivateKeyPass());
            }
        }

        JMeterUtils.getJMeterProperties().put("avroFile", Objects.nonNull(camSchema()) ? camSchema() : "");
        return props;
    }

    private Schema camSchema() {
        String filePath = getThreadContext().getProperties().getProperty("kafka.avro");
        if (Objects.nonNull(filePath) && !filePath.trim().isEmpty()) {
            try {
                File avroFile = new File(filePath);
                Schema.Parser parser = new Schema.Parser();
                return parser.parse(avroFile);
            } catch (IOException | NullPointerException exception) {
                log.error("Error camSchema: {}", exception.getMessage());
            }
        }
        return null;
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public void testEnded() {
        if (Objects.nonNull(kafkaProducer)) {
            try {
                Thread.sleep(10000L);
                kafkaProducer.close();
                log.info("Kafka Producer client connection terminated");
            } catch (Exception e) {
                log.error("Exception: {}", e);
            }
        }
    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }
}