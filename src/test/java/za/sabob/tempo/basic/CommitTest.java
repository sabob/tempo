package za.sabob.tempo.basic;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;
import za.sabob.tempo.domain.*;

public class CommitTest extends BaseTest {

    @Test
    public void commitTest() {

        EntityManager em = EM.beginTransaction();
        Assert.assertTrue( em.isOpen() );

        Person person = new Person();
        person.setName( "Test" );
        em.persist( person );

        EM.commitTransaction( em );
        EM.cleanupTransaction( em );
        Assert.assertFalse( em.isOpen() );

        em = EM.getEM();
        Person savedPerson = em.find( Person.class, person.getId() );
        Assert.assertNotNull( savedPerson );

        removePersons();

        em = EM.getEM();
        savedPerson = em.find( Person.class, person.getId() );
        Assert.assertNull( savedPerson );
    }
}
