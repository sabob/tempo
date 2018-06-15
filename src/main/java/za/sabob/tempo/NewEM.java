package za.sabob.tempo;

import java.util.logging.*;
import javax.persistence.*;
import za.sabob.tempo.transaction.*;
import za.sabob.tempo.util.*;
import static za.sabob.tempo.util.EMUtils.addSuppressed;

class NewEM {

    private final static Logger LOGGER = Logger.getLogger( NewEM.class.getName() );

    public <X extends Exception> void doInTransactionInNewEM( EntityManagerFactory emf, TransactionUpdater<X> updater ) throws X {

        EntityManager em = beginTransaction( emf );
        Exception exception = null;

        try {
            updater.update( em );

            commitTransaction( em );

        } catch ( Exception ex ) {
            exception = rollbackTransactionAndReturnError( em, ex );
            throw (X) exception;

        } finally {
            //exception = cleanupTransactionQuietly( em, exception );
            //EMUtils.throwAsRuntimeIfException( exception );
            cleanupTransactionAndThrowIfError( em, exception );
        }
    }

    public <R, X extends Exception> R getInTransactionInNewEM( EntityManagerFactory emf, TransactionExecutor<R, X> executor ) throws X {

        EntityManager em = beginTransaction( emf );
        Exception exception = null;

        try {

            R result = executor.execute( em );

            commitTransaction( em );

            return result;

        } catch ( Exception ex ) {
            //exception = ex;
            //throw rollbackTransaction( em, ex );

            exception = rollbackTransactionAndReturnError( em, ex );
            throw (X) exception;

        } finally {
            //exception = cleanupTransactionQuietly( em, exception );
            //EMUtils.throwAsRuntimeIfException( exception );
            cleanupTransactionAndThrowIfError( em, exception );
        }
    }

    public EntityManager beginTransaction() {
        EntityManagerFactory emf = EMF.getDefault();
        return beginTransaction( emf );
    }

    public EntityManager beginTransaction( EntityManagerFactory emf ) {

        EntityManager em = createEM( emf );

        try {

            em.getTransaction().begin();
            return em;

        } catch ( Exception e ) {
            RuntimeException exception = cleanupTransactionQuietly( em, e );
            throw EMUtils.toRuntimeException( exception );

        }
    }

    public void commitTransaction( EntityManager em ) {

        if ( em.getTransaction().getRollbackOnly() ) {
            rollbackTransaction( em );
            return;
        }

        em.getTransaction().commit();
    }


    public EntityManager createEM() {
        EntityManagerFactory emf = EMF.getDefault();
        return createEM( emf );
    }

    public EntityManager createEM( EntityManagerFactory emf ) {
        EntityManager em = emf.createEntityManager();
        return em;
    }

    public void cleanupTransaction( EntityManager em ) {

        if ( em == null ) {
            return;
        }

        Exception exception = null;

        try {

            exception = cleanupTransactionQuietly( em, null );

        } catch ( Exception ex ) {

            LOGGER.log( Level.SEVERE, "Error closing EntityManager", ex );
            exception = addSuppressed( ex, exception );
            throw EMUtils.toRuntimeException( exception );

        } finally {
        }

        EMUtils.throwAsRuntimeIfException( exception );
    }

    public void cleanupTransaction( EntityManager em, Exception exception ) {
        RuntimeException e = cleanupTransactionQuietly( em, exception );
        EMUtils.throwAsRuntimeIfException( e );
    }

    public RuntimeException cleanupTransactionQuietly( EntityManager em, Exception exception ) {
        try {
            performCleanupTransaction( em );

        } catch ( Exception ex ) {
            exception = addSuppressed( ex, exception );
        }

        return EMUtils.toRuntimeException( exception );

    }

    void performCleanupTransaction( EntityManager em ) {

        Exception exception = null;

        try {

            if ( em.isOpen() ) {
                if ( em.getTransaction().isActive() ) {

                    Throwable t = new Throwable( "Transaction is still active. Rolling transaction back in order to cleanup" );
                    LOGGER.log( Level.SEVERE, t.getMessage(), t );
                    rollbackTransaction( em );
                }

            } else {
                LOGGER.warning( "EntityManager is closed" );
            }

        } catch ( Exception ex ) {
            exception = addSuppressed( ex, exception );

        } finally {
            RuntimeException ex = closeQuietly( em );
            exception = addSuppressed( ex, exception );

        }

        EMUtils.throwAsRuntimeIfException( exception );
    }

    public RuntimeException rollbackTransaction( EntityManager em, Exception exception ) {

        if ( exception == null ) {
            throw new IllegalArgumentException( "exception cannot be null" );
        }

        RuntimeException result = rollbackTransactionQuietly( em, exception );

        EMUtils.throwAsRuntimeIfException( result );
        return result;
    }

    public RuntimeException rollbackTransactionQuietly( EntityManager em, Exception exception ) {

        try {

            rollbackTransaction( em );

        } catch ( Exception e ) {
            exception = addSuppressed( e, exception );
        }
        return EMUtils.toRuntimeException( exception );
    }

    public void rollbackTransaction( EntityManager em ) {

        try {

            if ( em.isOpen() && em.getTransaction().isActive() ) {

                em.getTransaction().rollback();
            }

        } catch ( Exception e ) {
            throw new RuntimeException( e );

        }
    }

    public RuntimeException closeQuietly( EntityManager em ) {

        try {
            close( em );
        } catch ( Exception e ) {
            return EMUtils.toRuntimeException( e );
        }
        return null;
    }

    void close( EntityManager em ) {

        if ( em.isOpen() ) {
            em.close();

        } else {
            LOGGER.warning( "EntityManager is already closed" );
        }
    }

    <X extends Throwable> X rollbackTransactionAndReturnError( EntityManager em, Exception exception ) {

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

    public void cleanupTransactionAndThrowIfError( EntityManager em, Exception exception ) {

        try {

            cleanupTransaction( em );

        } catch ( Exception e ) {
            exception = addSuppressed( e, exception );
            //return EMUtils.toRuntimeException( exception );
            EMUtils.throwIfException( exception );
        }

        EMUtils.throwIfException( exception );
    }

}
