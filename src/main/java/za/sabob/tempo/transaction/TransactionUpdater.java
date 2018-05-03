package za.sabob.tempo.transaction;

import javax.persistence.*;

public interface TransactionUpdater<X extends Exception> {

    public void update( EntityManager em ) throws X;
}
