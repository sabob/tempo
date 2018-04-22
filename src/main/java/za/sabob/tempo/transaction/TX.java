package za.sabob.tempo.transaction;

import javax.persistence.*;
import za.sabob.tempo.*;

public class TX {

    public static void doInTransaction( EntityManagerFactory emf, Transaction transaction ) {

        EntityManager em = null;

        try {
            em = EM.beginTransaction( emf );

            transaction.doInTransaction( em );

            EM.commitTransaction( em );

        } catch ( Exception ex ) {
            throw EM.rollbackTransaction( em, ex );

        } finally {

            EM.cleanupTransaction( em );
        }
    }

    public static void doInTransaction( Transaction transaction ) {
        EntityManagerFactory emf = EMF.getDefault();
        doInTransaction( emf, transaction );
    }
}
