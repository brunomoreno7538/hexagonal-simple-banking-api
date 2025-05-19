package moreno.corebanking_natixis.domain.exception;

public class BankingBusinessException extends RuntimeException {
    public BankingBusinessException(String message) {
        super(message);
    }
}