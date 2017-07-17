package t01;

import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.Properties;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

/**
 * Created by Aleksandr Shevkunenko on 17.07.2017.
 */
public class PropertiesReaderTest {
    Properties p;

    @Before
    public void setUp() throws Exception {
        try (FileOutputStream f = new FileOutputStream("src\\main\\resources\\forTest.properties")) {
            p = new Properties();
            p.setProperty("parameter1", "value1");
            p.setProperty("parameter2", "value2");
            p.setProperty("parameter3", "value3");
            p.store(f, "Property file created for tests");
        }
    }

    @Test
    public void readAll() throws Exception {
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
        assertThat(pr.getProperties(), is(p));

//        System.out.println(pr.getProperties());
    }

}