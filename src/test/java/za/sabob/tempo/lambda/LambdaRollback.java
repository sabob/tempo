package za.sabob.tempo.lambda;

import java.util.*;
import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;
import za.sabob.tempo.domain.*;
import za.sabob.tempo.util.*;

public class LambdaRollback extends BaseTest {

    @Test
    public void testLambdaSetRollback() {

        try {

            EM.inTransaction( (em) -> {
                Assert.assertEquals( TestUtils.findAllPersons().size(), 0);
                Person person = new Person();
                em.persist( person);
                em.getTransaction().setRollbackOnly();
            } );

            Assert.assertEquals( TestUtils.findAllPersons().size(), 0);


        } catch ( Exception e ) {
            throw e;
        }
    }

    @Test
    public void testLambdaThrow() {

        String error = "please rollback";

        try {

            EM.inTransaction( (em) -> {
                Assert.assertEquals( TestUtils.findAllPersons().size(), 0);
                Person person = new Person();
                em.persist( person);

                if (true) throw new IllegalStateException(error);
            } );



        } catch ( Exception expected ) {
            Assert.assertEquals(expected.getMessage(), error);
            Assert.assertEquals( TestUtils.findAllPersons().size(), 0);
        }
    }
}
