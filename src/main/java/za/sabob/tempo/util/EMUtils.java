package za.sabob.tempo.util;

import java.util.*;
import javax.persistence.*;

public class EMUtils {

    /**
     * Adds the given suppressedException to the mainException and returns the mainException, unless it is null, in which case the suppressedException is
     * returned.
     *
     * @param primaryException the primary exception that was thrown in the try block to which the suppressedException is added
     * @param supressedException the suppressed exception thrown in the finally block to add to the primaryException
     * @return the mainException or supresesdException if mainException is null
     */
    public static Exception addSuppressed( Exception primaryException, Exception supressedException ) {

        if ( supressedException == null ) {
            return primaryException;
        }

        if ( primaryException == null ) {
            return supressedException;
        }

        primaryException.addSuppressed( supressedException );
        return primaryException;
    }

    /**
     * Throws the given exception as a RuntimeException, unless the exception is null, in which case the method simply returns.
     *
     * @param exception the exception to throw as a RuntimeException
     */
    public static void throwAsRuntimeIfException( Exception exception ) {
        if ( exception == null ) {
            return;
        }

        throw toRuntimeException( exception );
    }

    public static <X extends Throwable> void throwIfException( Exception exception ) throws X {
        if ( exception == null ) {
            return;
        }

      throw (X) exception;
    }

    public static RuntimeException toRuntimeException( Exception exception ) {
        if ( exception == null ) {
            return null;
        }

        if ( exception instanceof RuntimeException ) {
            return (RuntimeException) exception;
        }
        return new RuntimeException( exception );

    }

    public static <R> R getSingleResultOrNull( Query query ) {

        List<R> results = query.getResultList();

        if ( results.isEmpty() ) {
            return null;

        } else if ( results.size() == 1 ) {
            return results.get( 0 );
        }

        throw new NonUniqueResultException();
    }

    public static <R> R getFirstResultOrNull( Query query ) {

        List<R> results = query.getResultList();

        if ( results.isEmpty() ) {
            return null;

        } else {
            return results.get( 0 );
        }
    }

    public static boolean isRollbackOnly( EntityManager em ) {
        if ( em.isOpen() && em.getTransaction().isActive() ) {
            return em.getTransaction().getRollbackOnly();
        }
        return false;
    }

//    public static boolean hasActiveTransaction( EntityManagerFactory emf ) {
//
//        if ( EM.hasEM( emf ) ) {
//
//            EntityManager em = EMF.getEM( emf );
//            if ( em.getTransaction().isActive() ) {
//                return true;
//            }
//        }
//
//        return false;
//    }

}
