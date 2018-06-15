package za.sabob.tempo;

import java.util.*;
import java.util.logging.*;
import javax.persistence.*;
import za.sabob.tempo.util.*;
import static za.sabob.tempo.util.EMUtils.addSuppressed;
import za.sabob.tempo.util.Stack;

public class EMContext {

    private final static Logger LOGGER = Logger.getLogger( EMContext.class.getName() );

    private static final ThreadLocal<Map<EntityManagerFactory, CloseHandle>> OPEN_IN_VIEW_HANDLE_HOLDER
        = new ThreadLocal<Map< EntityManagerFactory, CloseHandle>>();

    private final EntityManager em;

    // Instead of using EM.getEntityManagerFactory, we need to track the EMF that created the EM, because frameworks proxy the EntityManagerFactories
    // so that EM.getEntityManagerFactory returns a different instance that Tempo used.
    private EntityManagerFactory emf = null;

    private final Stack<Object> callstack = new Stack();

    private CloseHandle closeHandle = null;

    public EMContext( EntityManager em, EntityManagerFactory emf ) {
        this.em = em;
        this.emf = emf;
    }

    public EMContext( EntityManager em, EntityManagerFactory emf, CloseHandle closeHandle ) {
        this( em, emf );
        this.closeHandle = closeHandle;
    }

    public EntityManager getEM() {
        return em;
    }

    public EntityManager beginTransaction() {
        try {

        if ( getEM().getTransaction().isActive() ) {

            if ( !EMConfig.isJoinableTransactions() ) {
                throw new IllegalStateException(
                    "You are not allowed to start nested transactions for the same EntityManagerFactory. An EntityManager is already busy with a transaction."
                    + " To allow transactions to join set EMConfig.setJoinableTransactions( true )" );

            }
        }

        incrementCallstack();

        EntityManager em = getEM();

        if ( !canTransact() ) {
            return em;
        }

        if ( em.getTransaction().isActive() ) {
            throw new RuntimeException( "You are trying to begin a transaction while another transaction is already active. "
                + "Ensure you cleanup the transaction before starting a new one." );
        }

        em.getTransaction().begin();

            return em;

        } catch ( Exception e ) {
            RuntimeException exception = cleanupTransactionQuietly( e );
            throw EMUtils.toRuntimeException( exception );

        }
    }

    public void rollbackTransaction() {
        // always rollback even in nested TX.

//        if ( !canRollback() ) {
//            return;
//        }
        EntityManager em = getEM();

        try {

            if ( em.isOpen() && em.getTransaction().isActive() ) {

                em.getTransaction().rollback();
            }

        } catch ( Exception e ) {
            throw new RuntimeException( e );

        }
    }

    public void commitTransaction() {

        if ( em.getTransaction().getRollbackOnly() ) {
            rollbackTransaction();
            return;
        }

        if ( !canCommit() ) {
            return;
        }

        EntityManager em = getEM();

        if ( em.isOpen() && em.getTransaction().isActive() ) {
            em.getTransaction().commit();
        }
    }

    public void cleanupTransaction( CloseHandle closeHandle ) {
        decrementCallstack();

        if ( canClose() ) {

            performCleanupTransaction( closeHandle );

        } else {

            // cannot close because EntityManagers is still used, but if the passed in handle is a valid handle, we assume something is wrong
            // and that the open EntityManagers in the callstack should have been cleaned up
            if ( isMatchingHandleAnObject( closeHandle ) ) {

                // TODO close the EM anyway in order to stop leaks
                RuntimeException re = performCleanupTransactionQuietly( closeHandle );


                IllegalStateException ise = new IllegalStateException(
                    "The CloseHandle used to cleanupTransaction is the same CoseHandle used with EM.openInView(), however"
                    + " there are EntityManagers that are still busy with Transactions. These transactions must be committed or rolled back." );

                Exception ex = EMUtils.addSuppressed( ise, re );
                EMUtils.throwAsRuntimeIfException( ex );
            }

        }
    }

    public RuntimeException cleanupTransactionQuietly( Exception exception ) {
        try {
            cleanupTransaction();

        } catch ( Exception ex ) {
            exception = addSuppressed( ex, exception );
        }

        return EMUtils.toRuntimeException( exception );

    }

    public RuntimeException cleanupTransactionQuietly() {

        try {
            cleanupTransaction();
            return null;

        } catch ( Exception e ) {
            return EMUtils.toRuntimeException( e );
        }
    }


    public void cleanupTransaction() {
        cleanupTransaction( null );
    }

    public boolean canTransact() {
        return callstack.size() <= 1;
    }

    public boolean canRollback() {
        return canTransact();
    }

    public boolean canCommit() {
        return canTransact();
    }

    public boolean canClose() {
        return callstack.size() == 0;
    }

    public static void setOpenInViewHandle( EntityManagerFactory emf, CloseHandle handle ) {

        Map<EntityManagerFactory, CloseHandle> map = getOpenInViewMap();
        map.put( emf, handle );
    }

    public static CloseHandle getOpenInViewHandle( EntityManagerFactory emf ) {
        Map<EntityManagerFactory, CloseHandle> map = getOpenInViewMap();
        return map.get( emf );
    }

    public CloseHandle openInView( EntityManagerFactory emf ) {

        if ( closeHandle != null ) {
            throw new IllegalStateException( "EMContext is already open, close it first before opening again." );
        }

        closeHandle = getOpenInViewHandle( emf );

        if ( closeHandle == null ) {
            closeHandle = new CloseHandle();

        }
        return closeHandle;

    }

    public boolean isOpenInView() {
        return closeHandle != null;
    }

    public RuntimeException closeQuietly( CloseHandle handle ) {

        try {
            close( handle );
        } catch ( Exception e ) {
            return EMUtils.toRuntimeException( e );
        }
        return null;
    }

    public void close( CloseHandle handle ) {

        if ( isMatchingHandle( handle ) ) {

            performClose();

        }
    }

    boolean isMatchingHandleAnObject( CloseHandle handle ) {

        if ( handle == null ) {
            return false;
        }

        if ( !(handle instanceof CloseHandle) ) {
            return false;
        }

        return this.closeHandle == handle;
    }

    boolean isMatchingHandle( CloseHandle handle ) {
        return this.closeHandle == handle;
    }

    public void close() {
        close( null );
    }

    RuntimeException performCleanupTransactionQuietly( CloseHandle closeHandle ) {

        try {
            performCleanupTransaction( closeHandle );

        } catch ( Exception e ) {
            return EMUtils.toRuntimeException( e );
        }
        return null;
    }

    void performCleanupTransaction( CloseHandle closeHandle ) {

        EntityManager em = getEM();

        Exception exception = null;

        try {

            if ( em.isOpen() ) {
                if ( em.getTransaction().isActive() ) {

                    Throwable t = new Throwable( "Transaction is still active. Rolling transaction back in order to cleanup" );
                    LOGGER.log( Level.SEVERE, t.getMessage(), t );
                    //LOGGER.log( Level.INFO, t.getMessage(), t );
                    rollbackTransaction();
                }

            } else {
                LOGGER.warning( "EntityManager is closed" );
            }

        } catch ( Exception ex ) {
            exception = addSuppressed( ex, exception );

        } finally {
            RuntimeException ex = closeQuietly( closeHandle );
            exception = addSuppressed( ex, exception );

        }

        EMUtils.throwAsRuntimeIfException( exception );
    }

    void performClose() {
        EntityManager em = getEM();

        if ( em.isOpen() ) {
            em.close();
            cleanupResources();

        } else {
            LOGGER.warning( "EntityManager is already closed" );
        }

        this.closeHandle = null;
    }

    void cleanupResources() {
        EMF.removeEMF( this.emf );
        cleanupOpenInView();
    }

    void cleanupOpenInView() {
        cleanupOpenInView( emf );
    }

    static void cleanupOpenInView( EntityManagerFactory emf ) {
        Map<EntityManagerFactory, CloseHandle> map = getOpenInViewMap();
        map.remove( emf );
    }

    static void cleanupOpenInView( CloseHandle handle ) {

        if ( handle == null ) {
            return;
        }

        EntityManagerFactory emf = getEMFForCloseHandle( handle );
        if ( emf == null ) {
            return;
        }

        cleanupOpenInView( emf );
    }

    static EntityManagerFactory getEMFForCloseHandle( CloseHandle handle ) {
        Map<EntityManagerFactory, CloseHandle> map = getOpenInViewMap();
        for ( Map.Entry<EntityManagerFactory, CloseHandle> entry : map.entrySet() ) {

            CloseHandle value = entry.getValue();

            if ( value == handle ) {
                return entry.getKey();
            }
        }

        return null;

    }

    void forceCleanupTransaction() {
        performCleanupTransaction( this.closeHandle );
    }

    void forceClose() {
        performClose();
    }

    public boolean isEmpty() {
        return callstack.isEmpty();
    }

    public int size() {
        return callstack.size();
    }

    protected void incrementCallstack() {
        callstack.add( callstack.size() + 1 );
    }

    protected void decrementCallstack() {
        callstack.pop();
    }

    @Override
    public String toString() {
        return callstack.toString();
    }

    private static Map<EntityManagerFactory, CloseHandle> getOpenInViewMap() {
        Map<EntityManagerFactory, CloseHandle> map = OPEN_IN_VIEW_HANDLE_HOLDER.get();
        if ( map == null ) {
            map = new HashMap();
            OPEN_IN_VIEW_HANDLE_HOLDER.set( map );
        }
        return map;

    }
}
