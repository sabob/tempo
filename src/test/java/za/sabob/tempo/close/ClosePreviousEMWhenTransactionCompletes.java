package za.sabob.tempo.close;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;
import za.sabob.tempo.domain.*;

public class ClosePreviousEMWhenTransactionCompletes extends BaseTest {

    @Test
    public void testClosePreviousEMWhenTransactionCompletes() {

        try {

            EntityManager em1 = EM.getEM();
            Person person = em1.find( Person.class, 1L );
            Assert.assertTrue( em1.isOpen() );


            EntityManager em2 = EM.beginTransaction();

            Assert.assertEquals( em1, em2 );
            EM.commitTransaction( em2 );
            EM.cleanupTransaction( em2 );

            Assert.assertFalse( em1.isOpen() );

        } catch ( Exception e ) {
            Assert.fail( "Second EM.getEM() call should succeed", e );

        }
    }
}
