package za.sabob.tempo.close;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;
import za.sabob.tempo.util.*;

public class CloseAllOpenTransactions extends BaseTest {

    @Test
    public void closeAllTransactionsTest() {
        EMConfig.ON_AUTO_ROLLBACK_THROW_EXCEPTION = false;
        EMConfig.ON_AUTO_ROLLBACK_LOG = false;

        EntityManager em = EM.beginTransaction();

        EM.beginTransaction();

        EM.beginTransaction();

        // create 3 contexts

        Assert.assertTrue( em.isOpen() );

        EM.cleanupTransaction( em ); // close 1st context
        Assert.assertTrue( em.isOpen() ); // em is still open because stack is deeper than 1

        EM.closeAll();

        Assert.assertFalse( em.isOpen() );// attempting a second cleanup won't work. EM is still open because no handle was passed to cleanup

        EMConfig.ON_AUTO_ROLLBACK_THROW_EXCEPTION = true;
        EMConfig.ON_AUTO_ROLLBACK_LOG = true;
    }

    @Test
    public void closeAllWillCloseOpenInViewTest() {

        EMConfig.ON_AUTO_ROLLBACK_THROW_EXCEPTION = false;
        EMConfig.ON_AUTO_ROLLBACK_LOG = false;

        CloseHandle handle = EM.openInView();

        EntityManager em = EM.beginTransaction();

        EM.beginTransaction();

        EM.beginTransaction();

        // create 3 contexts
        Assert.assertTrue( em.isOpen() );

        EM.cleanupTransaction( em ); // close 1st context
        Assert.assertTrue( em.isOpen() ); // em is still open because stack is deeper than 1

        EM.closeAll();

        Assert.assertFalse( em.isOpen() );// attempting a second cleanup won't work. EM is still open because no handle was passed to cleanup

        EMConfig.ON_AUTO_ROLLBACK_THROW_EXCEPTION = true;
        EMConfig.ON_AUTO_ROLLBACK_LOG = true;
    }
}
