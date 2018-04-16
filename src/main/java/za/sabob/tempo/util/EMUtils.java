package za.sabob.tempo.util;

import javax.persistence.*;
import java.util.List;

public class EMUtils {

    /**
     * Adds the given suppressedException to the mainException and returns the mainException, unless it is null, in which case the suppressedException is
     * returned.
     *
     * @param mainException      the main exception on which to add the suppressedException
     * @param supressedException the exception to add to the mainException
     * @return the mainException or supresesdException if mainException is null
     */
    public static Exception addSuppressed(Exception mainException, Exception supressedException) {

        if (supressedException == null) {
            return mainException;
        }

        if (mainException == null) {
            return supressedException;
        }

        mainException.addSuppressed(supressedException);
        return mainException;
    }

    /**
     * Throws the given exception as a RuntimeException, unless the exception is null, in which case the method simply returns.
     *
     * @param exception the exception to throw as a RuntimeException
     */
    public static void throwAsRuntimeIfException(Exception exception) {

        if (exception == null) {
            return;
        }

        if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        }

        throw new RuntimeException(exception);
    }

    public static RuntimeException toRuntimeException(Exception exception) {
        if (exception == null) {
            return null;
        }

        if (exception instanceof RuntimeException) {
            return (RuntimeException) exception;
        }
        return new RuntimeException(exception);

    }


    public static <R> R getSingleResultOrNull(Query query) {

        List<R> results = query.getResultList();

        if (results.isEmpty()) {
            return null;

        } else if (results.size() == 1) {
            return results.get(0);
        }

        throw new NonUniqueResultException();
    }

    public static <R> R getFirstResultOrNull(Query query) {

        List<R> results = query.getResultList();

        if (results.isEmpty()) {
            return null;

        } else {
            return results.get(0);
        }
    }
}
