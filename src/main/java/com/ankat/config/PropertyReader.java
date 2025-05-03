package com.ankat.config;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PropertyReader extends ConfigTestElement implements TestBean, TestStateListener {
    private static final Logger log = LoggerFactory.getLogger(PropertyReader.class);
    private String propFilePath;

    public PropertyReader() {
        super();
    }

    public void testEnded() {

    }

    public void testEnded(String arg0) {
        testEnded();
    }

    public void testStarted() {
        if (getPropFilePath() != null && getPropFilePath().length() > 0) {
            try {
                Path path = Paths.get(getPropFilePath());
                if (!path.isAbsolute())
                    path = Paths.get(FileServer.getFileServer().getBaseDir(), path.toString());
                JMeterUtils.getJMeterProperties().load(new FileInputStream(path.toString()));
                log.info("Property file reader - loading the properties from " + path);

            } catch (FileNotFoundException e) {
                log.error(e.getMessage());
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void testStarted(String arg0) {
        testStarted();
    }

    /**
     * @return the file path
     */
    public String getPropFilePath() {
        return this.propFilePath;
    }

    /**
     * @param propFilePath the file path to read
     */
    public void setPropFilePath(String propFilePath) {
        this.propFilePath = propFilePath;
    }

}