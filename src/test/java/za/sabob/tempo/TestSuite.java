package za.sabob.tempo;

import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import org.testng.*;
import org.testng.collections.*;

public class TestSuite {

    private final static Logger LOGGER = Logger.getLogger( TestSuite.class.getName() );

    public static void main( String[] args ) {

        try {
            TestListenerAdapter tla = new TestSuite.MyListener();
            TestNG testng = new TestNG();
//testng.setTestClasses(new Class[] { Run2.class });
            List suites = Lists.newArrayList();

            URL url = TestSuite.class.getResource( "testng.xml" );
            String filepath = Paths.get(url.toURI()).toFile().getCanonicalPath();
            suites.add( filepath );
            testng.setTestSuites( suites );
            testng.addListener( tla );
            testng.run();

            //System.out.println( "DONE " + getUnclosedConnections() );
        } catch ( Exception ex ) {
            throw new RuntimeException( ex );
            //System.out.println( "DONE " + getUnclosedConnections() );

        }
    }

    public static class MyListener extends TestListenerAdapter {

        @Override
        public void onTestFailedButWithinSuccessPercentage( ITestResult result ) {
            
            log( result );
        }

        @Override
        public void onTestFailure( ITestResult result ) {
            log( result );
        }

        private void log( ITestResult result ) {
            Throwable t = result.getThrowable();
            LOGGER.log( Level.SEVERE, "Name=" + result.getName() + " testName=" + result.getTestName() + ", Statu=" + result.getStatus(), t );

        }

    }

}
