package za.sabob.tempo;

import javax.persistence.*;
import org.testng.annotations.*;
import za.sabob.tempo.util.*;

public class BaseTest {

    //public static DataSource ds;
    @BeforeClass
    public void beforeClass() {

        //ds = TestUtils.getDS();
        //TestUtils.createDatabase( ds );
        //TestUtils.populateDatabase( ds );
        if ( EMF.hasDefault() ) {
            EMF.closeDefault();
        }

        EntityManagerFactory emf = TestUtils.createEntityManagerFactory();
        EMF.registerDefault( emf );

    }

    public void removePersons() {

        EM.updateInTransaction( em -> {

            em.createQuery( "delete from Person" ).executeUpdate();

        } );

    }
}
