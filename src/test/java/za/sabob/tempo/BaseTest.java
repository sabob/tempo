package za.sabob.tempo;

import javax.persistence.*;
import org.testng.annotations.*;
import za.sabob.tempo.util.*;

public class BaseTest {

    //public static DataSource ds;
    @BeforeClass
    public void beforeClass() {

        System.out.println( "Before class" );

        //ds = TestUtils.getDS();
        //TestUtils.createDatabase( ds );
        //TestUtils.populateDatabase( ds );
        if ( EMF.hasDefault() ) {
            EMF.closeDefault();
        }

        EntityManagerFactory emf = TestUtils.createEntityManagerFactory();
        EMF.registerDefault( emf );

    }

    @AfterClass
    public void afterClass() {
        System.out.println( "After class" );

    }

    public void removePersons() {

        EM.doInTransaction( em -> {

            em.createQuery( "delete from Person" ).executeUpdate();

        } );

    }
}
