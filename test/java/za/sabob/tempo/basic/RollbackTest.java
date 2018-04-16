package za.sabob.tempo.basic;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.Test;
import za.sabob.tempo.*;

public class RollbackTest extends BaseTest {

    @Test
    public void rollbackTest() {

        EntityManager em = EM.beginTransaction();
        Assert.assertTrue( em.isOpen() );

        Person person = new Person();
        person.setName( "Test" );
        em.persist( person );
        
        EM.rollbackTransaction( em );
        EM.cleanupTransaction( em );
        Assert.assertFalse( em.isOpen() );
        
        em = EM.getEM();
        Person savedPerson = em.find( Person.class, person.getId() );
        Assert.assertNull( savedPerson );
    }
}
