package com.splitapp.dto;

import java.math.BigDecimal;

/**
 * Represents one simplified "debtor owes creditor amount" entry.
 * Now also carries member IDs so the Settle button can POST them.
 */
public class DebtEntry {

    private String     debtor;
    private String     creditor;
    private BigDecimal amount;
    private Long       debtorId;
    private Long       creditorId;

    public DebtEntry(String debtor, String creditor, BigDecimal amount,
                     Long debtorId, Long creditorId) {
        this.debtor     = debtor;
        this.creditor   = creditor;
        this.amount     = amount;
        this.debtorId   = debtorId;
        this.creditorId = creditorId;
    }

    public String     getDebtor()     { return debtor; }
    public String     getCreditor()   { return creditor; }
    public BigDecimal getAmount()     { return amount; }
    public Long       getDebtorId()   { return debtorId; }
    public Long       getCreditId()  { return creditorId; } // kept short for Thymeleaf
    public Long       getCreditorId() { return creditorId; }
}