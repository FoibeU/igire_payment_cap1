package LAB1;

import exception.InvalidAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;


public final class Money {

    public static final int SCALE = 2;
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000000.00");

//    transaction validation
    public static BigDecimal requirePositive(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Amount is required.");
        }
        if (amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
        if (amount.stripTrailingZeros().scale() > SCALE) {
            throw new InvalidAmountException("Amount can have at most 2 decimal places.");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidAmountException("Amount must not exceed " + format(MAX_AMOUNT) + ".");
        }
        return amount.setScale(SCALE, RoundingMode.UNNECESSARY);
    }


    public static BigDecimal parse(String text) {
        String cleaned = text == null ? "" : text.trim().replace(",", "").replace(" ", "");
        if (cleaned.isEmpty()) {
            throw new InvalidAmountException("Amount is required.");
        }
        try {
            return requirePositive(new BigDecimal(cleaned));
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Amount must be a number, e.g. 15000 or 15000.50.");
        }
    }

    public static BigDecimal round(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(SCALE) : value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static String format(BigDecimal value) {
        return String.format("%,.2f", value == null ? BigDecimal.ZERO : value);
    }
}
