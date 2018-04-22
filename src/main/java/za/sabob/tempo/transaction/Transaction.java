package za.sabob.tempo.transaction;

import javax.persistence.*;

public interface Transaction {

    public void doInTransaction( EntityManager em );
}
