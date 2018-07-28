package za.sabob.tempo.transaction;

import javax.persistence.*;

@FunctionalInterface
public interface TransactionalOperation<X extends Exception> {

    public void run( EntityManager em ) throws X;
}
