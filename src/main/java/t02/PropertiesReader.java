package t02;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Aleksandr Shevkunenko on 17.07.2017.
 */
public class PropertiesReader {

    static final String[] statusMessage = { "properties not loaded",
                                            "properties successfully loaded" };

    static final String[] errorMessage = {  "no error",
                                            "properties file was not found",
                                            "properties file contains a malformed Unicode escape sequence",
                                            "an error occurred when reading from the properties file" };

    private Properties properties;
    private String status = statusMessage[0];
    private String error = errorMessage[0];

    public Properties getProperties() {
        return properties;
    }

    public String getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    void readAll(String filename) {
        try (FileInputStream f = new FileInputStream(filename)) {
            properties = new Properties();
            properties.load(f);
            status = statusMessage[1];
            error = errorMessage[0];
        } catch (FileNotFoundException e) {
            properties = null;
            error = errorMessage[1];
        } catch (IllegalArgumentException e) {
            properties = null;
            error = errorMessage[2];
        } catch (IOException e) {
            properties = null;
            error = errorMessage[3];
        }
    }

    public String readProperty(String filename, String key) {
        if (key == null) throw new IllegalArgumentException("The property key mustn't be null.");

        if (properties == null) readAll(filename);

        return properties.getProperty(key, "No value found for the key " + key);
    }
}
