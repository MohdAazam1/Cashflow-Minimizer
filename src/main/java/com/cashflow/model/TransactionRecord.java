package com.cashflow.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "transactions")
public class TransactionRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="payer_id")
    private Person payer;

    @ManyToOne(optional=false)
    @JoinColumn(name="payee_id")
    private Person payee;

    @Column(nullable=false, scale=2, precision=12)
    private BigDecimal amount;

    public TransactionRecord() {}
    public TransactionRecord(Person payer, Person payee, BigDecimal amount){
        this.payer = payer; this.payee = payee; this.amount = amount;
    }

    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }

    public Person getPayer(){ return payer; }
    public void setPayer(Person p){ this.payer = p; }

    public Person getPayee(){ return payee; }
    public void setPayee(Person p){ this.payee = p; }

    public BigDecimal getAmount(){ return amount; }
    public void setAmount(BigDecimal a){ this.amount = a; }
}
