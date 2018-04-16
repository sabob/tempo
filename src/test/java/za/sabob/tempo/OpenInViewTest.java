package za.sabob.tempo;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import za.sabob.tempo.util.CloseHandle;

import javax.persistence.EntityManager;

public class OpenInViewTest extends BaseTest {

    public OpenInViewTest() {
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
        Assert.assertTrue( em.isOpen() );// em is still open because no handle was passed to cleanup

        EM.cleanupTransaction( em, handle ); // em will be closed now
        Assert.assertFalse( em.isOpen() );
    }
    
    //@Test
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
