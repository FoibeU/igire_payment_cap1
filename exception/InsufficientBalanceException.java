package exception;

import java.math.BigDecimal;

/**
 * The account balance does not cover the amount (plus any fee).
 */
public class InsufficientBalanceException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public InsufficientBalanceException(Long accountId, BigDecimal balance, BigDecimal required) {
        super("Insufficient balance on account #" + accountId + ": balance " + String.format("%,.2f", balance) + ", required " + String.format("%,.2f", required) + ".");
    }
}
