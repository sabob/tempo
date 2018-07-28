package za.sabob.tempo.transaction;

import javax.persistence.*;

@FunctionalInterface
public interface TransactionalQuery<T, X extends Exception> {

    public T get( EntityManager em ) throws X;
}
