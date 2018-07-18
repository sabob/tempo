package za.sabob.tempo;

import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.domain.*;

public class BasicEMTest extends BaseTest {

    public BasicEMTest() {
    }

    @BeforeMethod
    public void setUp() {
    }

    @Test
    public void testNestedBeginIgnoreFirstClose() {

        EntityManager em = EM.beginTransaction();
        em = EM.beginTransaction();

        EM.commitTransaction( em );
        EM.cleanupTransaction( em );

        Assert.assertTrue( em.isOpen() );

        EM.commitTransaction( em );
        EM.cleanupTransaction( em );

        Assert.assertFalse( em.isOpen() );
    }

    @Test
    public void testNestedBeginIgnoreFirstCommit() {

        EntityManager em = EM.beginTransaction();
        em = EM.beginTransaction();

        Person person = new Person();
        person.setName( "Test" );
        em.persist( person );

        EM.commitTransaction( em ); // should be ignored
        EM.cleanupTransaction( em );//pop context but no cleanup

        EM.rollbackTransaction( em ); // should be ignored
        //em.getTransaction().rollback();
        EM.cleanupTransaction( em );// cleanup occurs

        em = EM.getEM();
        Person savedPerson = em.find( Person.class, person.getId() );
        Assert.assertNull( savedPerson );
        EM.cleanupTransaction( em );
        Assert.assertFalse( em.isOpen() );
    }

    @Test
    public void testNestedBeginCommitOnSecondCall() {

        EntityManager em = EM.beginTransaction();
        em = EM.beginTransaction();

        //EM.commitTransaction( em );
        Person person = new Person();
        person.setName( "Test" );
        em.persist( person );

        EM.commitTransaction( em ); // should be ignored

        EM.cleanupTransaction( em );

        EM.commitTransaction( em ); // should work now

        EM.cleanupTransaction( em ); // Should be closed now
        Assert.assertFalse( em.isOpen() );

        removePersons();
    }

    @Test
    public void testNestedBeginIgnoreFirstRollback() {

        EntityManager em = EM.beginTransaction();
        em = EM.beginTransaction();

        Person person = new Person();
        person.setName( "Test" );

        em.persist( person );
        //Assert.assertNull( saga.getId() );

        EM.rollbackTransaction( em ); // rollback always succeeds, even nested

        EM.cleanupTransaction( em );

        EM.commitTransaction( em ); // ignored, already rolled back so transaction isn't active
        EM.cleanupTransaction( em );

        em = EM.getEM();
        Person savedPerson = em.find( Person.class, person.getId() );
        Assert.assertNull( savedPerson );

        EM.rollbackTransaction( em );
        EM.cleanupTransaction( em );

        removePersons();
    }

    @Test
    public void testNestedBeginRollbackOnSecondCall() {

        EMContext ctx = EMF.getOrCreateEMContext( EMF.getDefault() );

        EntityManager em = EM.beginTransaction();
        em = EM.beginTransaction();

        System.out.println( "CS : " + ctx.getCallstackSize());

        //EM.commitTransaction( em );
        Person person = new Person();
        person.setName( "Test" );
        em.persist( person );

        System.out.println( "CS : " + ctx.getCallstackSize());
        EM.rollbackTransaction( em ); // should be ignored
        System.out.println( "CS : " + ctx.getCallstackSize());

        EM.cleanupTransaction( em );

        System.out.println( "CS : " + ctx.getCallstackSize());
        EM.rollbackTransaction( em ); // should work now
        System.out.println( "CS : " + ctx.getCallstackSize());

        EM.cleanupTransaction( em ); // Should be closed now
        Assert.assertFalse( em.isOpen() );

        em = EM.beginTransaction();
        Person savedPerson = em.find( Person.class, person.getId() );
        Assert.assertNull( savedPerson );
        EM.commitTransaction( em );
        EM.cleanupTransaction( em );
    }
}
