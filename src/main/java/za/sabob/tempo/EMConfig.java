package za.sabob.tempo;

import javax.persistence.*;

public class EMConfig {

    private static final ThreadLocal<Boolean> JOINABLE_TRANSACTIONS = new ThreadLocal<>();

    private static boolean JOINABLE_TRANSACTIONS_DEFAULT = true;

    public static void setJoinableTransactionsDefault( boolean value ) {
        JOINABLE_TRANSACTIONS_DEFAULT = value;
    }

    public static boolean isJoinableTransactionsDefault() {
        return JOINABLE_TRANSACTIONS_DEFAULT;
    }

    public static boolean setJoinableTransactions( boolean value ) {
        boolean currentValue = isJoinableTransactions();
        JOINABLE_TRANSACTIONS.set( value );
        return currentValue;
    }

    public static boolean isJoinableTransactions() {
        Boolean value = JOINABLE_TRANSACTIONS.get();
        if ( value == null ) {
            return isJoinableTransactionsDefault();
        }
        return value;

    }

    public static void registerDefault( EntityManagerFactory emf ) {
        EMF.registerDefault( emf );
    }

    public static EntityManagerFactory getDefault() {
        return EMF.getDefault();
    }
}
