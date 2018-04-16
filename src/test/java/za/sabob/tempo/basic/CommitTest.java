package za.sabob.tempo.basic;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.Test;
import za.sabob.tempo.*;

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

        removePerson( person );
    }
}
