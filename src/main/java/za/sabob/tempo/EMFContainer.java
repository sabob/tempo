package za.sabob.tempo;

import za.sabob.tempo.util.CloseHandle;
import za.sabob.tempo.util.EMUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class EMFContainer {

    final private Map<EntityManagerFactory, EMContext> emByFactory = new LinkedHashMap<>();

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
        CloseHandle closeHandle = EMContext.getOpenInViewHandle(emf);
        
        return new EMContext( em, closeHandle );
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
        Exception ex2 = closeSilently();
        ex2 = EMUtils.addSuppressed( ex2, ex1 );

        EMUtils.throwAsRuntimeIfException( ex2 );
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

    public RuntimeException closeSilently() {

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
