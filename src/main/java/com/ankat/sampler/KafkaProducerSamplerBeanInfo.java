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
package com.ankat.sampler;

import com.ankat.utils.VariableSettings;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;

public class KafkaProducerSamplerBeanInfo extends BeanInfoSupport {

    public KafkaProducerSamplerBeanInfo() {
        super(KafkaProducerSampler.class);

        createPropertyGroup("Variable Name bound to Kafka Client", new String[]{"kafkaProducerClientVariableName"});
        createPropertyGroup("Message to Produce", new String[]{"kafkaTopic", "partitionString", "kafkaMessageKey", "kafkaMessageValue", "messageHeaders"});

        PropertyDescriptor kafkaProducerClientVariableNamePropDesc = property("kafkaProducerClientVariableName");
        kafkaProducerClientVariableNamePropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        kafkaProducerClientVariableNamePropDesc.setValue(DEFAULT, "KafkaProducerClient");
        kafkaProducerClientVariableNamePropDesc.setDisplayName("Variable Name of Producer Client declared in Config element");
        kafkaProducerClientVariableNamePropDesc.setShortDescription("Variable name declared in Kafka Producer client config");

        PropertyDescriptor producerSettingsPropDesc = property("kafkaTopic");
        producerSettingsPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        producerSettingsPropDesc.setValue(DEFAULT, "kafka_topic");
        producerSettingsPropDesc.setDisplayName("Kafka Topic");

        producerSettingsPropDesc = property("partitionString");
        producerSettingsPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        producerSettingsPropDesc.setValue(DEFAULT, "");
        producerSettingsPropDesc.setDisplayName("Partition String");
        producerSettingsPropDesc.setShortDescription("Leave it blank/empty if not required");

        producerSettingsPropDesc = property("kafkaMessageKey");
        producerSettingsPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        producerSettingsPropDesc.setValue(DEFAULT, "Key");
        producerSettingsPropDesc.setDisplayName("Kafka Message Key");
        producerSettingsPropDesc.setShortDescription("Kafka Message Key (blank/empty for null key)");

        producerSettingsPropDesc = property("kafkaMessageValue", TypeEditor.TextAreaEditor);
        producerSettingsPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        producerSettingsPropDesc.setValue(DEFAULT, "{\"Message\": \"It's a Hello from DI Kafka Sampler!\"}");
        producerSettingsPropDesc.setDisplayName("Kafka Message");

        PropertyDescriptor headerTable = property("messageHeaders", TypeEditor.TableEditor);
        headerTable.setValue(TableEditor.CLASSNAME, VariableSettings.class.getName());
        headerTable.setValue(TableEditor.HEADERS, new String[]{"Key", "Value"});
        headerTable.setValue(TableEditor.OBJECT_PROPERTIES, new String[]{VariableSettings.KEY, VariableSettings.VALUE});
        headerTable.setValue(DEFAULT, new ArrayList<>());
        headerTable.setValue(NOT_UNDEFINED, Boolean.TRUE);
        headerTable.setDisplayName("Message Headers (Optional)");
    }
}