package za.sabob.tempo;

import javax.persistence.*;
import javax.sql.*;
import org.testng.*;

public class BaseTest {

    public static DataSource ds;

    public BaseTest() {
    }

    public void removePerson( Person person ) {

        EntityManager em = EM.beginTransaction();
        Person savedPerson = em.find( Person.class, person.getId() );
        em.remove( person );
        EM.commitTransaction( em );
        EM.cleanupTransaction( em );

        em = EM.getEM();
        savedPerson = em.find( Person.class, savedPerson.getId() );
        Assert.assertNull( savedPerson );

    }
}
