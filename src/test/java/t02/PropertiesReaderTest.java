package t02;

import org.junit.Before;
import org.junit.Test;
import t02.PropertiesReader;

import java.io.FileOutputStream;
import java.util.Properties;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

/**
 * Created by Aleksandr Shevkunenko on 17.07.2017.
 */
public class PropertiesReaderTest {

    Properties propsForTests;

    @Before
    public void setUp() throws Exception {
        try (FileOutputStream f = new FileOutputStream("src\\main\\resources\\forTest.properties")) {
            propsForTests = new Properties();
            propsForTests.setProperty("parameter1", "value1");
            propsForTests.setProperty("parameter2", "value2");
            propsForTests.setProperty("parameter3", "value3");
            propsForTests.store(f, "Property file created for tests");
        }
    }

    @Test
    public void readAllTest() throws Exception {
        PropertiesReader pr = new PropertiesReader();

        assertThat(pr.getStatus(), is(PropertiesReader.statusMessage[0]));
        assertThat(pr.getError(), is(PropertiesReader.errorMessage[0]));
        assertNull(pr.getProperties());

        pr.readAll("src\\main\\resources\\forTest__x.properties");

        assertThat(pr.getStatus(), is(PropertiesReader.statusMessage[0]));
        assertThat(pr.getError(), is(PropertiesReader.errorMessage[1]));
        assertNull(pr.getProperties());

        pr.readAll("src\\main\\resources\\forTest.properties");

        assertThat(pr.getStatus(), is(PropertiesReader.statusMessage[1]));
        assertThat(pr.getError(), is(PropertiesReader.errorMessage[0]));
        assertThat(pr.getProperties(), is(propsForTests));

//        System.out.println(pr.getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void readPropertyWithNullTest() throws Exception {
        PropertiesReader pr = new PropertiesReader();
        pr.readProperty("src\\main\\resources\\forTest.properties", null);
    }

    @Test
    public void readPropertyTest() throws Exception {
        PropertiesReader pr = new PropertiesReader();

        String value = pr.readProperty("src\\main\\resources\\forTest.properties", "parameter1");

        assertThat(value, is(propsForTests.getProperty("parameter1")));

        value = pr.readProperty("src\\main\\resources\\forTest.properties", "parameter10");

        assertThat(value, is("No value found for the key parameter10"));
    }
}