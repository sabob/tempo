package za.sabob.tempo.openinview;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;
import za.sabob.tempo.util.*;

public class OpenInViewCleanupTest extends BaseTest {

    @Test
    public void testMuktipleOpenInView() {

        try {
            EntityManagerFactory emf = EMF.getDefault();
            CloseHandle handle = EM.openInView();

            EntityManager em = EM.beginTransaction();
            EM.rollbackTransaction( em );
            EM.cleanupTransaction( em );

            Assert.assertTrue( EM.hasEM( emf ) );

            EM.close( handle );

            Assert.assertFalse( EM.hasEM( emf ) );

        } catch ( Exception e ) {
            Assert.fail( "Second EM.getEM() call should succeed", e );

        }
    }
}
