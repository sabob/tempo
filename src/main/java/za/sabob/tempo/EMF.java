package za.sabob.tempo;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import za.sabob.tempo.util.*;

public final class EMF {

    private final static Logger LOGGER = Logger.getLogger( EMF.class.getName() );

    private static final ThreadLocal<EMFContainer> CONTAINER_HOLDER = new ThreadLocal<>();

    private static final Map<String, EntityManagerFactory> FACTORIES = new ConcurrentHashMap<>();

    private static EntityManagerFactory defaultEMF;

    private EMF() {
    }

    public static void register( EntityManagerFactory emf ) {

    }

    public static void setDefault( EntityManagerFactory emf ) {
        defaultEMF = emf;
    }

    public static EntityManagerFactory get() {

        if ( defaultEMF == null ) {
            throw new IllegalStateException( "No default EntityManagerFactory have been registered. Set a default factory or use EMF.get(factoryName) " );
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

    public static EntityManager getEM() {
        EntityManagerFactory emf = get();
        return getEM( emf );
    }

    public static EntityManager getEM( EntityManagerFactory emf ) {
        EMFContainer container = getOrCreateContainer();

        EMContext ctx = container.getOrCreateEMContext( emf );
        EntityManager em = ctx.getEM();

        return em;
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

    public static void cleanupTransactions() {
        RuntimeException ex = cleanupTransactionsSilently();
        EMUtils.throwAsRuntimeIfException( ex );
    }

    public static RuntimeException cleanupTransactionsSilently() {

        RuntimeException ex = getOrCreateContainer().cleanupTransactionsSilently();

        EMF.setContainer( null );
        return ex;
    }

    public static void setContainer( EMFContainer container ) {
        CONTAINER_HOLDER.set( container );
    }

    public static boolean hasContainer() {
        EMFContainer container = CONTAINER_HOLDER.get();
        if ( container == null ) {
            return false;
        } else {
            return true;
        }
    }
}
