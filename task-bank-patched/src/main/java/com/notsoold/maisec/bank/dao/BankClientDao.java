package com.notsoold.maisec.bank.dao;

import com.notsoold.maisec.bank.model.BankClient;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankClientDao extends CrudRepository<BankClient, Long> {

    Optional<BankClient> findByClientName(String name);

}
