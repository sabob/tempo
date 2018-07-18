package za.sabob.tempo.reuse;

import java.util.*;
import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;
import za.sabob.tempo.domain.*;

public class ReuseEMTest extends BaseTest {

    @Test
    public void testReuseEM() {
        // Test that multiple begin transaction/commit work without cleaning up the EM

        EntityManager em = EM.beginTransaction();
        Person person = new Person();
        person.setName( "Bob" );
        em.persist( person );
        EM.commitTransaction( em );

        em = EM.beginTransaction();
        person = new Person();
        person.setName( "Jeff" );
        em.persist( person );
        EM.commitTransaction( em );

        Query query = EM.getEM().createQuery( "from Person" );
        List<Person> persons = query.getResultList();
        Assert.assertEquals( persons.size(), 2 );

        Assert.assertFalse( EMF.isEmpty() );
        EM.cleanupTransaction( em );
        Assert.assertTrue( EMF.isEmpty() );
    }
}
