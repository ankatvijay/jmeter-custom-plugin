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
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class KafkaConsumerConfig<K, V> extends ConfigTestElement implements ConfigElement, TestBean, TestStateListener, Serializable {
    @Getter @Setter private KafkaConsumer<K, V> kafkaConsumer;
    @Getter @Setter private List<VariableSettings> extraConfigs;
    @Getter @Setter private String kafkaBrokers;
    @Getter @Setter private String groupId;
    @Getter @Setter private String topic;
    @Getter @Setter private String numberOfMsgToPoll;
    @Getter @Setter private boolean autoCommit;
    @Getter @Setter private String deSerializerKey;
    @Getter @Setter private String deSerializerValue;
    @Getter @Setter private String securityType;
    @Getter @Setter private String kafkaSslKeystore; // Kafka ssl keystore (include path information); e.g; "server.keystore.jks"
    @Getter @Setter private String kafkaSslKeystorePassword; // Keystore Password
    @Getter @Setter private String kafkaSslTruststore;
    @Getter @Setter private String kafkaSslTruststorePassword;
    @Getter @Setter private String kafkaSslPrivateKeyPass;
    @Getter @Setter private String kafkaConsumerClientVariableName;

    @Override
    public void addConfigElement(ConfigElement config) {
    }

    @Override
    public void testStarted() {
        this.setRunningVersion(true);
        TestBeanHelper.prepare(this);
        JMeterVariables variables = getThreadContext().getVariables();

        if (variables.getObject(kafkaConsumerClientVariableName) != null) {
            log.error("Kafka consumer is already running.");
        } else {
            if ((Objects.nonNull(getDeSerializerKey()) && !getDeSerializerKey().trim().isEmpty()) && (Objects.nonNull(getDeSerializerValue()) && !getDeSerializerValue().trim().isEmpty())) {
                synchronized (this) {
                    try {
                        Deserializer<K> consumerDeserializerKey = createDeserializer(getDeSerializerKey());
                        Deserializer<V> consumerDeserializerValue = createDeserializer(getDeSerializerValue());
                        kafkaConsumer = new KafkaConsumer<>(getProperties(), consumerDeserializerKey, consumerDeserializerValue);
                        kafkaConsumer.subscribe(Collections.singletonList(getTopic()));
                        variables.putObject(kafkaConsumerClientVariableName, kafkaConsumer);
                        variables.putObject("consumerDeserializerKeyVariableName", getDeSerializerKey());
                        variables.putObject("consumerDeserializerValueVariableName", getDeSerializerValue());
                        log.info("Kafka consumer client successfully Initialized");
                    } catch (Exception e) {
                        log.error("Error establishing kafka consumer client!", e);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Deserializer<T> createDeserializer(String deserializerClass) throws ReflectiveOperationException {
        return (Deserializer<T>) Class.forName(deserializerClass).getDeclaredConstructor().newInstance();
    }

    private Properties getProperties() {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, getGroupId());//groupId
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, isAutoCommit());
        props.put("security.protocol", getSecurityType().replaceAll("securityType.", "").toUpperCase());

        log.debug("Additional Config Size::: " + getExtraConfigs().size());
        if (!getExtraConfigs().isEmpty()) {
            log.info("Setting up Additional properties");
            for (VariableSettings entry : getExtraConfigs()) {
                props.put(entry.getConfigKey(), entry.getConfigValue());
                log.debug(String.format("Adding property : %s", entry.getConfigKey()));
            }
        }

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, Math.max(Integer.parseInt(getNumberOfMsgToPoll()), 1));
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
        return props;
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public void testEnded() {
        if (kafkaConsumer != null) {
            kafkaConsumer.unsubscribe();
            kafkaConsumer.close();
            log.info("Kafka consumer client connection terminated");
        }
    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }
}