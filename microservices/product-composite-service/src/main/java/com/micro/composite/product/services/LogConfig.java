package com.micro.composite.product.services;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogConfig {
    public static FileHandler fileHandler;

    public static void loggerConfiguration(Logger logger) {
        SimpleFormatter formatter = new SimpleFormatter();
        try {
            fileHandler= new FileHandler("/home/miloud/Documents/code/java/logs.log");
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileHandler.setFormatter(formatter);
        logger.addHandler(fileHandler);
    }
}
