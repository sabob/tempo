package za.sabob.tempo.openinview;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;
import za.sabob.tempo.util.*;

public class MultipleOpenInViewTest extends BaseTest {

    @Test
    public void testMultipleOpenInView() {

        try {
            EntityManagerFactory emf = EMF.getDefault();

            CloseHandle handle = EM.openInView();
            EM.close( handle );

            Assert.assertFalse( EM.hasEM( emf ) );

            handle = EM.openInView();
            EM.close( handle );
            Assert.assertFalse( EM.hasEM( emf ) );

        } catch ( Exception e ) {
            Assert.fail( "Second EM.openInView call should succeed", e );

        }
    }
}
