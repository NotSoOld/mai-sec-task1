package com.notsoold.maisec.bank.model;

import javax.persistence.*;

@Entity
@Table(name = "maisec_bank_client")
public class BankClient {

    @Id
    @GeneratedValue
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "money_amount", nullable = false)
    private Long moneyAmount;

    public Long getClientId() {
	return clientId;
    }

    public String getClientName() {
	return clientName;
    }

    public void setClientName(String clientName) {
	this.clientName = clientName;
    }

    public Long getMoneyAmount() {
	return moneyAmount;
    }

    public void setMoneyAmount(Long moneyAmount) {
	this.moneyAmount = moneyAmount;
    }
}
