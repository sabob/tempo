package za.sabob.tempo.reopen;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;

public class RecreateEMTest extends BaseTest {

    @Test
    public void testCreateNewEM() {

        try {
            EntityManager em = EM.beginTransaction();
            EM.commitTransaction( em );
            EM.cleanupTransaction( em );

            em = EM.getEM();

        } catch ( Exception e ) {
            Assert.fail( "Second EM.getEM() call should succeed", e );

        }
    }
}
