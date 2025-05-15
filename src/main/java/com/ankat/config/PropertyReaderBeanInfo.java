package com.ankat.config;

import org.apache.jmeter.testbeans.BeanInfoSupport;

import java.beans.PropertyDescriptor;

public class PropertyReaderBeanInfo extends BeanInfoSupport {

    //create a variable for each field
    private static final String FIELD_PROPERTY_FILE_PATH = "propFilePath";

    //create a zero-parameter constructor
    public PropertyReaderBeanInfo() {
        //call super(the class implementing the logic for prop file reader)
        super(PropertyReader.class);

        //add the new field in the GUI & its default settings
        PropertyDescriptor p = property(FIELD_PROPERTY_FILE_PATH);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setDisplayName("Property file path");
        p.setShortDescription("Property file default is current directory where the jmx is getting loaded");
    }
}