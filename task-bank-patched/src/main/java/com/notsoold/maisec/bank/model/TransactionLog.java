package com.notsoold.maisec.bank.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "maisec_transaction_log")
public class TransactionLog {

    @Id
    @GeneratedValue
    @Column(name = "obj_id", nullable = false)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "related_client_id", nullable = false)
    private Long relatedClientId;

    public Long getId() {
	return id;
    }

    public Date getCreationDate() {
	return creationDate;
    }

    public TransactionLog setCreationDate(Date creationDate) {
	this.creationDate = creationDate;
	return this;
    }

    public String getMessage() {
	return message;
    }

    public TransactionLog setMessage(String message) {
	this.message = message;
	return this;
    }

    public Long getRelatedClientId() {
	return relatedClientId;
    }

    public TransactionLog setRelatedClientId(Long relatedClientId) {
	this.relatedClientId = relatedClientId;
	return this;
    }

}
