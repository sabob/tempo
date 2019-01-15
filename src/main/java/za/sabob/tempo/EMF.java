package za.sabob.tempo;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import javax.persistence.*;

import za.sabob.tempo.util.*;

public final class EMF {

    private final static Logger LOGGER = Logger.getLogger( EMF.class.getName() );

    private static final ThreadLocal<EMFContainer> CONTAINER_HOLDER = new ThreadLocal<>();

    private static final Map<String, EntityManagerFactory> FACTORIES = new ConcurrentHashMap<>();

    private static EntityManagerFactory defaultEMF;

    public EMF() {
    }

    public static void registerDefault( EntityManagerFactory emf ) {

        if ( defaultEMF != null ) {
            if ( defaultEMF.isOpen() ) {
                throw new IllegalStateException( "EMF already has a default EntityManagerFactory that is open. You should prpobably close "
                        + "the current default instance before setting a new default" );
            }
        }

        defaultEMF = emf;
    }

    public static void register( String emfName, EntityManagerFactory emf ) {
        FACTORIES.put( emfName, emf );
    }

    public static boolean hasDefault() {
        return defaultEMF != null;

    }

    public static EntityManagerFactory getDefault() {

        if ( defaultEMF == null ) {
            throw new IllegalStateException( "No default EntityManagerFactory have been registered. Register a default factory or use EMF.get(factoryName) " );
        }
        return defaultEMF;

    }

    public static EntityManagerFactory get( String emfName ) {

        EntityManagerFactory emf = FACTORIES.get( emfName );

        if ( emf == null ) {
            throw new IllegalStateException( "EntityManagerFactory called '" + emfName + "' is not registered." );
        }

        return emf;
    }

    public static void setEM( EntityManagerFactory emf, EntityManager em ) {
        EMFContainer container = EMF.getOrCreateContainer();
        EMContext ctx = container.createEMContext( emf, em );
    }

    public static EntityManager getEM() {
        EntityManagerFactory emf = getDefault();
        return getEM( emf );
    }

    public static EntityManager getEM( EntityManagerFactory emf ) {
        EMContext ctx = getOrCreateEMContext( emf );
        EntityManager em = ctx.getEM();

        return em;
    }

    public static EMContext getOrCreateEMContext( EntityManagerFactory emf ) {
        EMFContainer container = getOrCreateContainer();
        EMContext ctx = container.getOrCreateEMContext( emf );
        return ctx;
    }

    public static EMFContainer getOrCreateContainer() {
        EMFContainer container;

        container = CONTAINER_HOLDER.get();

        if ( container == null ) {
            container = new EMFContainer();
            setContainer( container );
        }
        return container;
    }

    static void removeEMF( EntityManagerFactory emf ) {

        if ( hasContainer() ) {
            EMFContainer container = getOrCreateContainer();
            container.removeEMContext( emf );
        }
    }

    public static void cleanupTransactions() {
        RuntimeException ex = cleanupTransactionsQuietly();
        EMUtils.throwAsRuntimeIfException( ex );
    }

    public static RuntimeException cleanupTransactionsQuietly() {

        RuntimeException ex = getOrCreateContainer().cleanupTransactionsQuietly();

        EMF.setContainer( null );
        return ex;
    }

    public static void closeDefault() {
        close( getDefault() );

    }

    public static void closeAllEM() {

        Exception exception = null;

        try {

            closeAllEM( getDefault() );

        } catch ( Exception e ) {
            exception = EMUtils.addSuppressed( e, exception );
        }


        for ( EntityManagerFactory emf : FACTORIES.values() ) {

            try {

                closeAllEM( emf );

            } catch ( Exception e ) {
                exception = EMUtils.addSuppressed( e, exception );
            }
        }

        EMUtils.throwAsRuntimeIfException( exception );
    }


    public static void closeAll() {

        Exception exception = null;

        try {

            close( getDefault() );

        } catch ( Exception e ) {
            exception = EMUtils.addSuppressed( e, exception );
        }


        for ( EntityManagerFactory emf : FACTORIES.values() ) {

            try {

                close( emf );

            } catch ( Exception e ) {
                exception = EMUtils.addSuppressed( e, exception );
            }
        }

        EMUtils.throwAsRuntimeIfException( exception );
    }

    public static void close( EntityManagerFactory emf ) {

        if ( emf != null && emf.isOpen() ) {


            Exception exception = null;

            try {
                closeAllEM( emf );

            } catch ( RuntimeException e ) {
                exception = EMUtils.addSuppressed( e, exception );
            }

            try {
                emf.close();

            } catch ( RuntimeException e ) {
                exception = EMUtils.addSuppressed( e, exception );
            }

            EMUtils.throwAsRuntimeIfException( exception );
        }
    }

    public static void closeAllEM( EntityManagerFactory emf ) {

        if ( emf != null && emf.isOpen() ) {

            Exception exception = null;

            try {
                closeContainer( emf );

            } catch ( RuntimeException e ) {
                exception = EMUtils.addSuppressed( e, exception );
            }

            EMUtils.throwAsRuntimeIfException( exception );
        }

    }

    public static void close( String emfName ) {
        EntityManagerFactory emf = get( emfName );
        close( emf );

    }

    public static void setContainer( EMFContainer container ) {
        CONTAINER_HOLDER.set( container );
    }

    public static boolean isEmpty() {
        if ( hasContainer() ) {
            EMFContainer container = getOrCreateContainer();
            return container.isEmpty();
        }
        return true;

    }

    public static boolean hasContainer() {
        EMFContainer container = CONTAINER_HOLDER.get();
        if ( container == null ) {
            return false;
        } else {
            return true;
        }
    }

    public static void closeContainer( EntityManagerFactory emf ) {

        if ( hasContainer() ) {

            EMFContainer container = getOrCreateContainer();

            if ( container.isEmpty() ) {
                return;
            }

            RuntimeException e = container.closeQuietly( emf );
            EMUtils.throwAsRuntimeIfException( e );
        }
    }
}
