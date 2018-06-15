package za.sabob.tempo.openinview;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.BaseTest;
import za.sabob.tempo.EM;
import za.sabob.tempo.EMF;
import za.sabob.tempo.util.*;

public class CloseOpenInViewTest extends BaseTest {

    public CloseOpenInViewTest() {
    }

    @BeforeMethod
    public void setUp() {
    }

    @Test
    public void testOpenWithHandle() {

        CloseHandle handle = EM.openInView(); // Cannot close the EM until handle is pass in

        EntityManager em = EM.beginTransaction();
        EM.rollbackTransaction( em );

        Assert.assertTrue( em.isOpen() );

        EM.cleanupTransaction( em );
        Assert.assertTrue( em.isOpen() ); // em is still open because no handle was passed to cleanup

        EM.cleanupTransaction( em );
        Assert.assertTrue( em.isOpen() );// attempting a second cleanup won't work. EM is still open because no handle was passed to cleanup

        EM.cleanupTransaction( em, handle ); // em will be closed now
        Assert.assertFalse( em.isOpen() );
    }

    @Test
    public void testOpenWithHandleButCloseContainer() {

        // Closing Container will force close the EntityManager even if no handle is passed in

        CloseHandle handle = EM.openInView(); // Cannot close the EM until handle is pass in

        EntityManager em = EM.beginTransaction();
        EM.rollbackTransaction( em );

        Assert.assertTrue( em.isOpen() );
        EMF.cleanupTransactions();
        Assert.assertFalse( em.isOpen() );

    }
}
