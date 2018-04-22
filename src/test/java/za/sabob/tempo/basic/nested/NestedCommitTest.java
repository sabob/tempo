package za.sabob.tempo.basic.nested;

import java.util.logging.*;
import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;
import za.sabob.tempo.domain.*;

public class NestedCommitTest extends BaseTest {

    @Test
    public void nestedCommitTest() {

        EntityManager em = EM.beginTransaction();
        em = EM.beginTransaction();

        Assert.assertTrue( em.isOpen() );

        Person person = new Person();
        person.setName( "Test" );
        em.persist( person );

        EM.commitTransaction( em ); // ignored
        EM.cleanupTransaction( em ); // callstack is popped but EM not closed
        Assert.assertTrue( em.isOpen() );
        Assert.assertTrue( em.getTransaction().isActive() );

        Level level = Logger.getLogger( EMContext.class.getName() ).getLevel();

        Logger.getLogger( EMContext.class.getName() ).setLevel( Level.OFF ); // switch off logging for the next statement because it will log an error since
        // the transaction is still in progress.

        EM.cleanupTransaction( em ); // this will rollback, log an exception and close connection

        // Switch back logging
        Logger.getLogger( EMContext.class.getName() ).setLevel( level );

        em = EM.getEM();
        Person savedPerson = em.find( Person.class, person.getId() );
        Assert.assertNull( savedPerson );
    }
}
