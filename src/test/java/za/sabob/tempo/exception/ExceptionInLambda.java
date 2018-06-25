package za.sabob.tempo.exception;

import java.io.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;

public class ExceptionInLambda extends BaseTest {

    @Test
    public void testExceptionInLambda() {

        try {

            EM.inTransaction( (em) -> {

                if (true) throw new IOException( "IO IO" );

            } );

            Assert.fail( "Exception should be thrown" );

        } catch ( IOException e ) {
            System.out.println( "IOException caught : " + e.getMessage() );
        }
    }
}
