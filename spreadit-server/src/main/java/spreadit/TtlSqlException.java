package spreadit;

public class TtlSqlException extends Exception {
    public TtlSqlException() {
        super("Time to live expired or user not logged in");
    }
}
