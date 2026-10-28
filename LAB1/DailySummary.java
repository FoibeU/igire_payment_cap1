package LAB1;

import java.math.BigDecimal;
import java.time.LocalDate;
public class DailySummary {

    private LocalDate date;
    private TransactionType transactionType;
    private long count;
    private BigDecimal totalAmount;



    public DailySummary(LocalDate date, TransactionType transactionType, long count, BigDecimal totalAmount) {
        this.date = date;
        this.transactionType = transactionType;
        this.count = count;
        this.totalAmount = totalAmount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }


    public long getCount() {
        return count;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    @Override
    public String toString() {
        return "DailySummary{date=" + date + ", type=" + transactionType + ", count=" + count + ", total=" + Money.format(totalAmount) + "}";
    }
}
