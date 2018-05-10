package za.sabob.tempo;

public class EMConfig {

    private static final ThreadLocal<Boolean> JOINABLE_TRANSACTIONS = new ThreadLocal<>();

    private static boolean JOINABLE_TRANSACTIONS_DEFAULT = false;

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
}
