package com.notsoold.maisec.bank.dao;

import com.notsoold.maisec.bank.model.TransactionLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionLogDao extends CrudRepository<TransactionLog, Long> {


}
