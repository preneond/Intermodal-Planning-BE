package utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;

public class SerializationUtils {

    private static final Logger logger = LogManager.getLogger(SerializationUtils.class);

    public static void writeObjectToFile(Object serObj,File file) {
        try {

            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serObj);
            objectOut.close();
            logger.info("The object was succesfully written to a file");

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    public static Object readObjectFromFile(File file) {
        try {
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Object obj = objectIn.readObject();
            objectIn.close();
            logger.info("The object was red successfully from the file");
            return obj;

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }
}