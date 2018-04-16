package za.sabob.tempo;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;

public class BasicEMTest extends BaseTest {

    public BasicEMTest() {
    }

    @BeforeMethod
    public void setUp() {
    }
    
    //@Test
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
        
        //EM.rollbackTransaction( em ); // should be ignored
        em.getTransaction().rollback();
        EM.cleanupTransaction( em );// cleanup occurs
        
        em = EM.getEM();
        Person savedPerson = em.find( Person.class, person.getId() );
        Assert.assertNull( savedPerson );
        EM.cleanupTransaction( em );        
    }
    
    //@Test
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
        Assert.assertFalse( em.isOpen());

        removePerson(person );
    }
    
    //@Test
    public void testNestedBeginIgnoreFirstRollback() {

        EntityManager em = EM.beginTransaction();
        em = EM.beginTransaction();

        Person person = new Person();
        person.setName( "Test" );

        em.persist( person );
        //Assert.assertNull( saga.getId() );

        EM.rollbackTransaction( em ); // should be ignored

        EM.cleanupTransaction( em );
        
        EM.commitTransaction( em );
        EM.cleanupTransaction( em );
        
        em = EM.getEM();
        Person savedPerson = em.find( Person.class, person.getId() );
        Assert.assertNotNull( savedPerson );
        
        EM.rollbackTransaction( em );        
        EM.cleanupTransaction( em );        
        
        removePerson( person );
    }
    
    //@Test
    public void testNestedBeginRollbackOnSecondCall() {

        EntityManager em = EM.beginTransaction();
        em = EM.beginTransaction();

        //EM.commitTransaction( em );
        Person person = new Person();
        person.setName( "Test" );
        em.persist( person );

        EM.rollbackTransaction( em ); // should be ignored

        EM.cleanupTransaction( em );
        
        EM.rollbackTransaction( em ); // should work now
        
        EM.cleanupTransaction( em ); // Should be closed now
        Assert.assertFalse( em.isOpen());

        em = EM.beginTransaction();
        Person savedPerson= em.find( Person.class, person.getId() );
        Assert.assertNull( savedPerson );
        EM.commitTransaction( em );
        EM.cleanupTransaction( em );
    }
}
