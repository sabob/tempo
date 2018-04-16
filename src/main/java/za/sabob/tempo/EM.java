package za.sabob.tempo;

import za.sabob.tempo.util.CloseHandle;
import za.sabob.tempo.util.EMUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import static za.sabob.tempo.util.EMUtils.addSuppressed;

public class EM {

    private final static Logger LOGGER = Logger.getLogger( EM.class.getName() );

    public static EntityManager getOrCreateEM( EntityManagerFactory emf ) {
        EntityManager em = EMF.getEM( emf );
        return em;
    }

    public static EntityManager getEM( EntityManagerFactory emf ) {
        return getOrCreateEM( emf );
    }

    public static EntityManager getEM() {
        return EMF.getEM();
    }

    public static boolean hasEM( EntityManagerFactory emf ) {
        if ( !EMF.hasContainer() ) {
            return false;
        }

        EMFContainer container = EMF.getOrCreateContainer();

        if ( container.isEmpty() ) {
            return false;
        }

        return container.hasEMContext( emf );
    }

    public static CloseHandle openInView() {
        EntityManagerFactory emf = EMF.get();
        return openInView( emf );
    }

    public static CloseHandle openInView( EntityManagerFactory emf ) {

        CloseHandle handle = EMContext.getOpenInViewHandle( emf );
        if ( handle != null ) {
            throw new IllegalStateException( "EMContext is already open, close it first before opening again." );
        }

        handle = createHandle();
        EMContext.setOpenInViewHandle( emf, handle );
        return handle;
    }
    
    public static void closeAllEM() {
        if (EMF.hasContainer()) {
            EMFContainer container = EMF.getOrCreateContainer();
            container.forceClose();
        }
    }

    public static void close() {
        EntityManagerFactory emf = EMF.get();
        close( emf );
    }

    public static void close( EntityManagerFactory emf ) {

        if ( hasEM( emf ) ) {
            EntityManager em = getOrCreateEM( emf );
            cleanupTransaction( em );
        }
    }

    public static void close( CloseHandle handle ) {
        EntityManagerFactory emf = EMF.get();
        close( emf, handle );
    }

    public static void close( EntityManagerFactory emf, CloseHandle handle ) {

        if ( hasEM( emf ) ) {
            EntityManager em = getOrCreateEM( emf );
            cleanupTransaction( em, handle );
        }
    }

    public static EntityManager beginTransaction() {
        EntityManagerFactory emf = EMF.get();
        return beginTransaction( emf );
    }

    public static EntityManager beginTransaction( EntityManagerFactory emf ) {
        EMContext ctx = getEMContext( emf );
        return ctx.beginTransaction();
    }

    public static void commitTransaction( EntityManager em ) {
        EMContext ctx = getContextForEM( em );
        ctx.commitTransaction();
    }

    public static void rollbackTransaction( EntityManager em ) {

        EMContext ctx = getContextForEM( em );
        ctx.rollbackTransaction();
    }

    public static RuntimeException rollbackTransactionSilently( EntityManager em ) {

        try {
            rollbackTransaction( em );
            return null;

        } catch ( Exception e ) {
            return EMUtils.toRuntimeException( e );
        }
    }

    public static RuntimeException rollbackTransactionSilently( EntityManager em, Exception exception ) {

        try {

            rollbackTransaction( em );

        } catch ( Exception e ) {
            exception = addSuppressed( e, exception );
        }
        return EMUtils.toRuntimeException( exception );
    }

    public static RuntimeException rollbackTransaction( EntityManager em, Exception exception ) {

        if ( exception == null ) {
            throw new IllegalArgumentException( "exception cannot be null" );
        }

        return rollbackTransactionSilently( em, exception );
    }

    public static void cleanupTransaction( EntityManager em ) {
        EMContext ctx = getContextForEM( em );
        ctx.cleanupTransaction();
    }

    public static RuntimeException cleanupTransaction( EntityManager em, Exception exception ) {

        try {
            EM.cleanupTransaction( em );

        } catch ( Exception ex ) {
            exception = addSuppressed( ex, exception );
        }
        return EMUtils.toRuntimeException( exception );
    }

    public static RuntimeException cleanupTransactionSilently( EntityManager em ) {

        try {
            cleanupTransaction( em );
            return null;

        } catch ( Exception e ) {
            return EMUtils.toRuntimeException( e );
        }
    }

    public static RuntimeException cleanupTransactionSilently( EntityManager em, CloseHandle closeHandle ) {

        try {
            cleanupTransaction( em, closeHandle );
            return null;

        } catch ( Exception e ) {
            return EMUtils.toRuntimeException( e );
        }
    }

    public static RuntimeException cleanupTransactionSilently( EntityManager em, Exception exception ) {

        try {

            cleanupTransaction( em );
            return EMUtils.toRuntimeException( exception );

        } catch ( Exception e ) {
            exception = addSuppressed( e, exception );
            return EMUtils.toRuntimeException( exception );
        }
    }

    public static void cleanupTransaction( EntityManager em, CloseHandle closeHandle ) {

        if ( em == null ) {
            return;
        }

        Exception exception = null;
        EMContext ctx = getContextForEM( em );

        try {

            ctx.cleanupTransaction( closeHandle );

        } catch ( Exception ex ) {

            LOGGER.log( Level.SEVERE, "Error closing EntityManager", ex );
            exception = addSuppressed( ex, exception );

        } finally {
            RuntimeException ex = ctx.closeSilently( closeHandle );
            exception = addSuppressed( ex, exception );
        }

        EMUtils.throwAsRuntimeIfException( exception );
    }

    public static EMContext getEMContext( EntityManagerFactory emf ) {
        EMFContainer container = EMF.getOrCreateContainer();
        EMContext ctx = container.getOrCreateEMContext( emf );
        return ctx;
    }

    protected static EMContext getContextForEM( EntityManager em ) {
        EMFContainer container = EMF.getOrCreateContainer();
        return container.getEMContext( em );
    }

    protected static CloseHandle createHandle() {
        return new CloseHandle();
    }
}
