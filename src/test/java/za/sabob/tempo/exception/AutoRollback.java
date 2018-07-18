package za.sabob.tempo.exception;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;
import za.sabob.tempo.util.*;

public class AutoRollback extends BaseTest {

    @Test
    public void testAutoRollbackEM() {

        EMConfig.ON_AUTO_ROLLBACK_THROW_EXCEPTION = true;

        try {
            EntityManager em = EM.beginTransaction();
            EM.closeAll();

            Assert.fail("cleanupTransaction should throw exception because beginTransaction wasn't followed by a commit or rollback" );

        } catch ( Exception e ) {
            Assert.assertTrue( EMF.isEmpty() );
        }
    }

    @Test
    public void testAutoRollbackHandle() {

        EMConfig.ON_AUTO_ROLLBACK_THROW_EXCEPTION = true;

        CloseHandle handle = EM.openInView();

        try {
            EntityManager em = EM.beginTransaction();
            EM.close( handle );

            Assert.fail("cleanupTransaction should throw exception because beginTransaction wasn't followed by a commit or rollback" );

        } catch ( Exception e ) {
            Assert.assertTrue( EMF.isEmpty() );
        }
    }
}
