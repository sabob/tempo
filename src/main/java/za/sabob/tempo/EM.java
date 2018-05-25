package za.sabob.tempo;

import java.util.logging.*;
import javax.persistence.*;
import za.sabob.tempo.transaction.*;
import za.sabob.tempo.util.*;
import static za.sabob.tempo.util.EMUtils.addSuppressed;

public class EM {

    private final static Logger LOGGER = Logger.getLogger( EM.class.getName() );

    public static EntityManager getEM( EntityManagerFactory emf ) {
        return EMF.getEM( emf );
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
        EntityManagerFactory emf = EMF.getDefault();
        return openInView( emf );
    }

    public static CloseHandle openInView( EntityManagerFactory emf ) {

        CloseHandle handle = EMContext.getOpenInViewHandle( emf );
        if ( handle != null ) {
            throw new IllegalStateException( "EMContext is already openInView, close the EntityManager first before opening again." );
        }

        handle = createHandle();
        EMContext.setOpenInViewHandle( emf, handle );
        return handle;
    }

    public static void closeAll() {
        if ( EMF.hasContainer() ) {
            EMFContainer container = EMF.getOrCreateContainer();
            container.forceClose();
        }
    }

    public static void close() {
        EntityManagerFactory emf = EMF.getDefault();
        close( emf );
    }

    public static void close( EntityManagerFactory emf ) {

        if ( hasEM( emf ) ) {
            EntityManager em = getEM( emf );
            cleanupTransaction( em );
        }
    }

    public static void close( CloseHandle handle ) {
        EntityManagerFactory emf = EMF.getDefault();
        close( emf, handle );
    }

    public static void close( EntityManagerFactory emf, CloseHandle handle ) {

        if ( hasEM( emf ) ) {
            EntityManager em = getEM( emf );
            cleanupTransaction( em, handle );
        }
    }

    public static <X extends Exception> void updateInTransaction( TransactionUpdater<X> executor ) throws X {

        EntityManagerFactory emf = EMF.getDefault();
        updateInTransaction( emf, executor );
    }

    public static <X extends Exception> void updateInTransaction( EntityManagerFactory emf, TransactionUpdater<X> updater ) throws X {

        EntityManager em = beginTransaction( emf );

        try {

            updater.update( em );

            commitTransaction( em );

        } catch ( Exception ex ) {
            throw rollbackTransaction( em, ex );

        } finally {

            cleanupTransaction( em );
        }
    }

    public static <R, X extends Exception> R executeInTransaction( EntityManagerFactory emf, TransactionExecutor<R, X> executor ) throws X {

        EntityManager em = beginTransaction( emf );

        try {

            R result = executor.execute( em );

            commitTransaction( em );

            return result;

        } catch ( Exception ex ) {
            throw rollbackTransaction( em, ex );

        } finally {

            cleanupTransaction( em );
        }
    }

    public static <R, X extends Exception> R executeInTransaction( TransactionExecutor<R, X> executor ) throws X {

        EntityManagerFactory emf = EMF.getDefault();
        return executeInTransaction( emf, executor );
    }

    public static EntityManager beginTransaction() {
        EntityManagerFactory emf = EMF.getDefault();
        return beginTransaction( emf );
    }

    public static EntityManager beginTransaction( EntityManagerFactory emf ) {

        EMContext ctx = EMF.getOrCreateEMContext( emf );
        return ctx.beginTransaction();
    }

    public static void commitTransaction( EntityManager em ) {
        EMContext ctx = getContextForEM( em );
        ctx.commitTransaction();
    }

    public static void rollbackTransaction( EntityManager em ) {

        if ( em == null ) {
            return;
        }

        EMContext ctx = getContextForEM( em );
        ctx.rollbackTransaction();
    }

    public static RuntimeException rollbackTransactionQuietly( EntityManager em ) {

        try {
            rollbackTransaction( em );
            return null;

        } catch ( Exception e ) {
            return EMUtils.toRuntimeException( e );
        }
    }

    public static RuntimeException rollbackTransactionQuietly( EntityManager em, Exception exception ) {

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

        RuntimeException result = rollbackTransactionQuietly( em, exception );

        EMUtils.throwAsRuntimeIfException( result );
        return result;
    }

    public static void cleanupTransaction( EntityManager em ) {
        if ( em == null ) {
            return;
        }

        EMContext ctx = getContextForEM( em );
        ctx.cleanupTransaction();
    }

    public static RuntimeException cleanupTransaction( EntityManager em, Exception exception ) {

        EMContext ctx = getContextForEM( em );
        RuntimeException e = ctx.cleanupTransactionQuietly( exception );
        return e;
    }

    public static RuntimeException cleanupTransactionQuietly( EntityManager em ) {

        try {
            cleanupTransaction( em );
            return null;

        } catch ( Exception e ) {
            return EMUtils.toRuntimeException( e );
        }
    }

    public static RuntimeException cleanupTransactionQuietly( EntityManager em, CloseHandle closeHandle ) {

        try {
            cleanupTransaction( em, closeHandle );
            return null;

        } catch ( Exception e ) {
            return EMUtils.toRuntimeException( e );
        }
    }

    public static RuntimeException cleanupTransactionQuietly( EntityManager em, Exception exception ) {

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
            RuntimeException ex = ctx.closeQuietly( closeHandle );
            exception = addSuppressed( ex, exception );
        }

        EMUtils.throwAsRuntimeIfException( exception );
    }

    protected static EMContext getContextForEM( EntityManager em ) {
        EMFContainer container = EMF.getOrCreateContainer();
        return container.getEMContext( em );
    }

    protected static CloseHandle createHandle() {
        return new CloseHandle();
    }
}
