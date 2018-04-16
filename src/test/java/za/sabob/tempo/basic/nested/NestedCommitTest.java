package za.sabob.tempo.basic.nested;

import org.testng.Assert;
import org.testng.annotations.Test;
import za.sabob.tempo.BaseTest;
import za.sabob.tempo.EM;
import za.sabob.tempo.Person;

import javax.persistence.EntityManager;

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
        
        EM.cleanupTransaction( em ); // this will rollback and close connection
        
        em = EM.getEM();
        Person savedPerson = em.find( Person.class, person.getId() );
        Assert.assertNull( savedPerson );
    }
}
