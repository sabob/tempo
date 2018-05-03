package za.sabob.tempo.transaction;

import javax.persistence.*;

public interface TransactionExecutor<T, X extends Exception> {

    public T execute( EntityManager em ) throws X;
}
