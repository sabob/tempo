package za.sabob.tempo.basic.nested;

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
        EM.cleanupTransaction( em ); // ignored but callstack is popped
        Assert.assertTrue( em.isOpen() );
        Assert.assertTrue( em.getTransaction().isActive() );

        EM.cleanupTransaction( em ); // this will rollback, log an exception and close connection

        em = EM.getEM();
        Person savedPerson = em.find( Person.class, person.getId() );
        Assert.assertNull( savedPerson );
    }
}
