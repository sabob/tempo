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

    private final Stack<Object> callstack = new Stack();

    private CloseHandle closeHandle = null;

    public EMContext( EntityManager em ) {
        this.em = em;
    }

    public EMContext( EntityManager em, CloseHandle closeHandle ) {
        this.em = em;
        this.closeHandle = closeHandle;
    }

    public EntityManager getEM() {
        return em;
    }

    public EntityManager beginTransaction() {

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

        if ( !canCommit()) {
            return;
        }

        EntityManager em = getEM();
        
        if (em.isOpen() && em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
    }

    public void cleanupTransaction( CloseHandle closeHandle ) {
        decrementCallstack();

        if ( !canClose() ) {
            return;
        }

        performCleanupTransaction( closeHandle );
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

    private static Map<EntityManagerFactory, CloseHandle> getOpenInViewMap() {
        Map<EntityManagerFactory, CloseHandle> map = OPEN_IN_VIEW_HANDLE_HOLDER.get();
        if ( map == null ) {
            map = new HashMap();
            OPEN_IN_VIEW_HANDLE_HOLDER.set( map );
        }
        return map;

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

    public RuntimeException closeSilently( CloseHandle handle ) {

        try {
            close( handle );
        } catch ( Exception e ) {
            return EMUtils.toRuntimeException( e );
        }
        return null;
    }

    public void close( CloseHandle handle ) {

        if ( this.closeHandle == handle ) {

            performClose();

        }
    }

    public void close() {
        close( null );
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
            RuntimeException ex = closeSilently( closeHandle );
            exception = addSuppressed( ex, exception );

        }

        EMUtils.throwAsRuntimeIfException( exception );

    }

    void performClose() {
        EntityManager em = getEM();

        if ( em.isOpen() ) {
            em.close();

        } else {
            LOGGER.warning( "EntityManager is already closed" );
        }

        this.closeHandle = null;
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
}
