package za.sabob.tempo.lambda;

import java.util.*;
import javax.persistence.*;
import org.testng.*;
import org.testng.annotations.*;
import za.sabob.tempo.*;
import za.sabob.tempo.domain.*;
import za.sabob.tempo.util.*;

public class LambdaCommit extends BaseTest {

    @Test
    public void testLambda() {

        try {

            EM.inTransaction( (em) -> {
                Assert.assertEquals( TestUtils.findAllPersons().size(), 0);
                Person person = new Person();
                em.persist( person);
            } );

            Assert.assertEquals( TestUtils.findAllPersons().size(), 1);


        } catch ( Exception e ) {
            throw e;
        }
    }
}
