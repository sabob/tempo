package za.sabob.tempo;

import java.util.*;
import javax.persistence.*;
import za.sabob.tempo.util.*;

public class EMFContainer {

    final private Map<EntityManagerFactory, EMContext> emByFactory = new HashMap<>();

    final private Map<EntityManager, EMContext> contextByEM = new HashMap<>();

    public EMContext getOrCreateEMContext( EntityManagerFactory emf ) {

        EMContext ctx = getEMContext( emf );

        if ( ctx == null ) {
            ctx = createEMContext( emf );

        } else {
            EntityManager em = ctx.getEM();
            if ( em == null || !em.isOpen() ) {
                ctx = createEMContext( emf );
            }
        }

        emByFactory.put( emf, ctx );
        contextByEM.put( ctx.getEM(), ctx );

        return ctx;
    }

    public boolean hasEMContext( EntityManagerFactory emf ) {

        return emByFactory.get( emf ) != null;
    }

    protected EMContext createEMContext( EntityManagerFactory emf ) {
        EntityManager em = emf.createEntityManager();
        CloseHandle closeHandle = EMContext.getOpenInViewHandle( emf ); // In case there was a openInView call made, associate that closeHandle with the EMContext

        return new EMContext( em, emf, closeHandle );
    }

    protected EMContext getEMContext( EntityManagerFactory emf ) {
        EMContext ctx = emByFactory.get( emf );
        return ctx;
    }

    public EMContext getEMContext( EntityManager em ) {
        return contextByEM.get( em );
    }

    public boolean isEmpty() {
        return emByFactory.isEmpty();
    }

    public void forceClose() {
        Exception ex1 = cleanupTransactionsSilently();

        // No need to close below since cleanupTransactionsSilently already closes the EM
        //Exception ex2 = closeQuietly();
        //ex2 = EMUtils.addSuppressed( ex2, ex1 );

        EMUtils.throwAsRuntimeIfException( ex1 );
    }

    public void cleanupTransactions() {
        RuntimeException ex = cleanupTransactionsSilently();
        EMUtils.throwAsRuntimeIfException( ex );
    }

    public RuntimeException cleanupTransactionsSilently() {

        Exception exception = null;

        for ( EMContext ctx : emByFactory.values() ) {

            try {
                ctx.forceCleanupTransaction();

            } catch ( Exception e ) {
                exception = EMUtils.addSuppressed( e, exception );
            }
        }

        return EMUtils.toRuntimeException( exception );
    }

    public RuntimeException closeQuietly() {

        Exception exception = null;

        for ( EMContext ctx : emByFactory.values() ) {
            try {
                ctx.forceClose();

            } catch ( Exception e ) {
                exception = EMUtils.addSuppressed( e, exception );
            }
        }
        return EMUtils.toRuntimeException( exception );
    }
}
