package utils;

import master.CollectData;
import org.testng.TestNG;

public class TestRunner {
    public static TestNG testNG;
    public static void main(String[] args){
        testNG = new TestNG();
        testNG.setTestClasses(new Class[] {CollectData.class});
        testNG.run();
    }
}
