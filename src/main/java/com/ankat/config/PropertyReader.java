package com.ankat.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
public class PropertyReader extends ConfigTestElement implements ConfigElement, TestBean, TestStateListener, Serializable {
    @Getter @Setter private String propFilePath;

    @Override
    public void addConfigElement(ConfigElement config) {
    }

    @Override
    public void testStarted() {
        if (Objects.nonNull(getPropFilePath()) && !getPropFilePath().trim().isEmpty()) {
            try {
                Path path = Paths.get(getPropFilePath());
                if (!path.isAbsolute()) path = Paths.get(FileServer.getFileServer().getBaseDir(), path.toString());
                JMeterUtils.getJMeterProperties().load(new FileInputStream(path.toString()));
                log.info("Property file reader - loading the properties from " + path);
            } catch (FileNotFoundException e) {
                log.error(e.getMessage());
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public void testStarted(String arg0) {
        testStarted();
    }

    @Override
    public void testEnded() {
    }

    @Override
    public void testEnded(String arg0) {
        testEnded();
    }
}