package za.sabob.tempo.basic;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;

public class CloseEMFTest extends BaseTest {

    @Test
    public void testCloseEMF() {

        try {
            EntityManager em = EM.beginTransaction();
            EM.commitTransaction( em );
            EM.cleanupTransaction( em );
            EMF.closeDefault();

            Assert.assertFalse( EMF.getDefault().isOpen() );

            em = EM.getEM();
            Assert.fail( "Second EM.getEM() call should succeed" );

        } catch ( Exception e ) {
            // expected

        }
    }
}
