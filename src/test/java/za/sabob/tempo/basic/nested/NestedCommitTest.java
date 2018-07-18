package za.sabob.tempo.basic.nested;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;
import za.sabob.tempo.domain.*;

public class NestedCommitTest extends BaseTest {

    public NestedCommitTest() {
        EMConfig.ON_AUTO_ROLLBACK_LOG = false;
    }

    @Test
    public void nestedCommitTest() {

        EntityManager em = EM.beginTransaction();
        em = EM.beginTransaction();

        Assert.assertTrue( em.isOpen() );

        Person person = new Person();
        person.setName( "Test" );
        em.persist( person );

        EM.commitTransaction( em ); // ignored callstack is popped only
        EM.cleanupTransaction( em ); // ignored
        Assert.assertTrue( em.isOpen() );
        Assert.assertTrue( em.getTransaction().isActive() );

        EM.rollbackTransaction( em );
        EM.cleanupTransaction( em );
        Assert.assertFalse(em.isOpen() );
        Assert.assertFalse(em.getTransaction().isActive() );

        em = EM.getEM();
        Person savedPerson = em.find( Person.class, person.getId() );
        Assert.assertNull( savedPerson );
    }

    @Test
    public void nestedCommitTest2() {

        EntityManager em = EM.beginTransaction();
        EM.commitTransaction( em );
        EM.cleanupTransaction( em );

        em = EM.beginTransaction();

        Assert.assertTrue(em.getTransaction().isActive() );
        EM.commitTransaction( em );
        Assert.assertFalse(em.getTransaction().isActive() );

        Assert.assertTrue( em.isOpen());
        EM.cleanupTransaction( em );
        Assert.assertFalse( em.isOpen());
    }
}
