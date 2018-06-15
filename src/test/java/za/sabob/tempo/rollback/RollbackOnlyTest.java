package za.sabob.tempo.rollback;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;
import za.sabob.tempo.domain.*;

public class RollbackOnlyTest extends BaseTest {

    @Test
    public void testCreateNewEM() {

        try {
            EntityManager em = EM.beginTransaction();

            Person person = new Person();
            person.setName( "Test" );
            em.persist( person );

            Person savedPerson = em.find( Person.class, person.getId() );
            Assert.assertNotNull( savedPerson );

            EM.setRollbackOnly( em );
            EM.commitTransaction( em );

            savedPerson = em.find( Person.class, person.getId() );
            Assert.assertNull( savedPerson );

            EM.cleanupTransaction( em );

        } catch ( Exception e ) {
            Assert.fail( "Second EM.getEM() call should succeed", e );

        }
    }
}
