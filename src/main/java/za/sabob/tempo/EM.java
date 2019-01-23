package za.sabob.tempo;

import java.util.logging.*;
import javax.persistence.*;
import za.sabob.tempo.transaction.*;
import za.sabob.tempo.util.*;
import static za.sabob.tempo.util.EMUtils.addSuppressed;

public class EM {

    private final static Logger LOGGER = Logger.getLogger( EM.class.getName() );

    public static <X extends Exception> void inTransaction( TransactionalOperation<X> operation ) throws X {

        EntityManagerFactory emf = EMF.getDefault();
        EM.inTransaction( emf, operation );
    }

    public static <X extends Exception> void inTransaction( EntityManagerFactory emf, TransactionalOperation<X> operation ) throws X {

        EntityManager em = beginTransaction( emf );
        Exception exception = null;

        try {

            operation.run( em );

            commitTransaction( em );

        } catch ( Exception ex ) {
            //throw rollbackTransaction( em, ex );
            exception = rollbackTransactionAndReturnError( em, ex );
            throw (X) exception;

        } finally {
            cleanupTransactionAndThrowIfError( em, exception );
//            RuntimeException re = cleanupTransactionQuietly( em, exception );
//            if ( re != null ) {
//                EMUtils.throwIfException( re );
//            }
        }
    }

    public static <X extends Exception> void inTransactionInNewEM( TransactionalOperation<X> operation ) throws X {

        EntityManagerFactory emf = EMF.getDefault();
        EM.inTransactionInNewEM( emf, operation );
    }

    public static <X extends Exception> void inTransactionInNewEM( EntityManagerFactory emf, TransactionalOperation<X> operation ) throws X {

        NewEM newEm = new NewEM();
        newEm.inTransactionInNewEM( emf, operation );
    }

    public static <R, X extends Exception> R inTransaction( EntityManagerFactory emf, TransactionalQuery<R, X> query ) throws X {

        Exception exception = null;
        EntityManager em = beginTransaction( emf );

        try {

            R result = query.get( em );

            commitTransaction( em );

            return result;

        } catch ( Exception ex ) {
//            exception = ex;
//            throw rollbackTransaction( em, ex );
            exception = rollbackTransactionAndReturnError( em, ex );
            throw (X) exception;

        } finally {
            //exception = cleanupTransactionQuietly( em, exception );
            //EMUtils.throwAsRuntimeIfException( exception );
            cleanupTransactionAndThrowIfError( em, exception );
        }
    }

    public static <R, X extends Exception> R inTransaction( TransactionalQuery<R, X> query ) throws X {

        EntityManagerFactory emf = EMF.getDefault();
        return EM.inTransaction( emf, query );
    }

    public static <R, X extends Exception> R inTransactionInNewEM( TransactionalQuery<R, X> query ) throws X {

        EntityManagerFactory emf = EMF.getDefault();
        return getInTransactionInNewEM( emf, query );
    }

    public static <R, X extends Exception> R getInTransactionInNewEM( EntityManagerFactory emf, TransactionalQuery<R, X> query ) throws X {

        NewEM newEm = new NewEM();
        return newEm.inTransactionInNewEM( emf, query );
    }

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

    public static void close( EntityManager em ) {
        cleanupTransaction( em );
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

        } else {
            cleanupResources( handle );
            //System.out.println( "EM.close caled but EMF has no EM" );
        }
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

    public static void setRollbackOnly( EntityManager em ) {

        // Guard against em not having transaction. I've only seen this happen once with OutOfMemory exception.
        if (em.getTransaction().isActive()) {
            em.getTransaction().setRollbackOnly();
        }
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

    //static X rollbackTransactionAndThrow( EntityManager em, Exception exception ) throws X {
    static <X extends Throwable> X rollbackTransactionAndReturnError( EntityManager em, Exception exception ) {

        if ( exception == null ) {
            IllegalArgumentException ex = new IllegalArgumentException( "exception cannot be null" );
            throw ex;
        }

        try {

            rollbackTransaction( em );

        } catch ( Exception e ) {
            exception = addSuppressed( e, exception );
        }

        //EMUtils.throwIfException( exception );
        return (X) exception;
    }

    public static void cleanupTransaction( EntityManager em ) {
        if ( em == null ) {
            return;
        }

        EMContext ctx = getContextForEM( em );
        ctx.cleanupTransaction();
    }

    public static void cleanupTransaction( EntityManager em, Exception exception ) {

        EMContext ctx = getContextForEM( em );
        RuntimeException e = ctx.cleanupTransactionQuietly( exception );
        EMUtils.throwAsRuntimeIfException( e );
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

    public static void cleanupTransactionAndThrowIfError( EntityManager em, Exception exception ) {

        try {

            cleanupTransaction( em );

        } catch ( Exception e ) {
            exception = addSuppressed( e, exception );
            //return EMUtils.toRuntimeException( exception );
            EMUtils.throwIfException( exception );
        }

        EMUtils.throwIfException( exception );
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

    public static RuntimeException cleanupTransactionQuietly( EntityManager em, Exception exception, CloseHandle closeHandle ) {

        try {

            cleanupTransaction( em, closeHandle );
            return EMUtils.toRuntimeException( exception );

        } catch ( Exception e ) {
            exception = addSuppressed( e, exception );
            return EMUtils.toRuntimeException( exception );
        }
    }

    public static void cleanupTransaction( EntityManager em, Exception exception, CloseHandle closeHandle ) {
        if ( em == null ) {
            return;
        }

        EMContext ctx = getContextForEM( em );

        try {

            ctx.cleanupTransaction( closeHandle );

        } catch ( Exception ex ) {

            //LOGGER.log( Level.SEVERE, "Error closing EntityManager", ex );
            exception = addSuppressed( ex, exception );

        } finally {
            //RuntimeException ex = ctx.closeQuietly( closeHandle );
            //exception = addSuppressed( ex, exception );
            EMUtils.throwAsRuntimeIfException( exception );
        }
    }

    public static void cleanupTransaction( EntityManager em, CloseHandle closeHandle ) {
        Exception exception = null;
        cleanupTransaction( em, exception, closeHandle );
    }

    public static EntityManager createEM() {
        EntityManagerFactory emf = EMF.getDefault();
        return createEM( emf );
    }

    public static EntityManager createEM( EntityManagerFactory emf ) {
        NewEM newEm = new NewEM();
        return newEm.createEM( emf );
    }

    public static EntityManager beginTransactionInNewEM() {
        EntityManagerFactory emf = EMF.getDefault();
        return beginTransactionInNewEM( emf );

    }

    public static EntityManager beginTransactionInNewEM( EntityManagerFactory emf ) {

        NewEM newEm = new NewEM();
        EntityManager em = newEm.beginTransaction( emf );
        return em;
    }

    public void rollbackTransactionInNewEM( EntityManager em ) {
        NewEM newEm = new NewEM();
        newEm.rollbackTransaction( em );
    }

    public RuntimeException rollbackTransactionInNewEM( EntityManager em, Exception exception ) {
        NewEM newEm = new NewEM();
        return newEm.rollbackTransaction( em, exception );
    }

    public RuntimeException rollbackTransactionQuietlyInNewEM( EntityManager em, Exception exception ) {
        NewEM newEm = new NewEM();
        return newEm.rollbackTransactionQuietly( em, exception );
    }

    public void cleanupTransactionInNewEM( EntityManager em ) {
        NewEM newEm = new NewEM();
        newEm.cleanupTransaction( em );
    }

    public void cleanupTransactionInNewEM( EntityManager em, Exception ex ) {
        NewEM newEm = new NewEM();
        newEm.cleanupTransaction( em, ex );
    }

    public RuntimeException cleanupTransactionQuietlyInNewEM( EntityManager em, Exception ex ) {
        NewEM newEm = new NewEM();
        return newEm.cleanupTransactionQuietly( em, ex );
    }

    protected static EMContext getContextForEM( EntityManager em ) {
        EMFContainer container = EMF.getOrCreateContainer();
        EMContext ctx = container.getEMContext( em );
        ensureContextFound( ctx );
        return ctx;
    }

    protected static CloseHandle createHandle() {
        return new CloseHandle();
    }

    private static void ensureContextFound( EMContext ctx ) {
        if ( ctx == null ) {
            throw new IllegalStateException(
                "The given EntityManager is not registered with Tempo. This can occur if you created your own EntityManager. Use EM.beginTransaction() instead" );
        }
    }

    private static void cleanupResources( CloseHandle handle ) {

        EMContext.cleanupOpenInView( handle );
    }
}
