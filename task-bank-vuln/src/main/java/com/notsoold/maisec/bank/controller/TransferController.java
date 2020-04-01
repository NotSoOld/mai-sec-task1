package com.notsoold.maisec.bank.controller;

import com.notsoold.maisec.bank.dao.BankClientDao;
import com.notsoold.maisec.bank.dao.TransactionLogDao;
import com.notsoold.maisec.bank.model.BankClient;
import com.notsoold.maisec.bank.model.TransactionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Date;
import java.util.Optional;

@Controller
public class TransferController {

    @Autowired
    public TransferController(BankClientDao bankClientDao, TransactionLogDao transactionLogDao) {
        this.bankClientDao = bankClientDao;
        this.transactionLogDao = transactionLogDao;
    }

    private BankClientDao bankClientDao;
    private TransactionLogDao transactionLogDao;


    @PostMapping(value = "/transfer")
    @ResponseBody
    public String transfer(@RequestParam("accountName") String accountName, @RequestParam("amount") int amount, Principal principal) {
        if (amount <= 0) {
            return "Wrong money amount!";
	}
        // Get current client.
	Optional<BankClient> clientOpt = bankClientDao.findByClientName(principal.getName());
	if (!clientOpt.isPresent()) {
	    return "Error!";
	}
	if (principal.getName().equals(accountName)) {
	    return "You cannot do a transfer to yourself.";
	}
	BankClient currentClient = clientOpt.get();
	if (currentClient.getMoneyAmount() < amount) {
	    return "Error, no enough cash in the account!";
	}
	// Get client which should receive the transfer.
	Optional<BankClient> receivers = bankClientDao.findByClientName(accountName);
	if (!receivers.isPresent()) {
	    return "Error, no client '" + accountName + "' to make transfer to.";
	}
	BankClient receiver = receivers.get();
	receiver.setMoneyAmount(receiver.getMoneyAmount() + amount);
	currentClient.setMoneyAmount(currentClient.getMoneyAmount() - amount);

	// Log transaction.
	TransactionLog log = new TransactionLog();
	log.setCreationDate(new Date())
			.setRelatedClientId(currentClient.getClientId())
			.setMessage("Transferred $" + amount + " to " + accountName + " as "
					+ currentClient.getClientName() + ", balance: $" + currentClient.getMoneyAmount());
        transactionLogDao.save(log);
	TransactionLog mirrorLog = new TransactionLog();
	mirrorLog.setCreationDate(new Date())
			.setRelatedClientId(receiver.getClientId())
			.setMessage("Transfer of $" + amount + " from " + currentClient.getClientName()
					+ " to you, balance: $" + receiver.getMoneyAmount());
	transactionLogDao.save(mirrorLog);

	return "Success!";
    }

    @PostMapping(value = "/addbalance")
    @ResponseBody
    public String addbalance(@RequestParam("amount") int amount, Principal principal) {
        if (amount <= 0) {
            return "Wrong money amount!";
	}
        // Get current client.
	Optional<BankClient> clientOpt = bankClientDao.findByClientName(principal.getName());
	if (!clientOpt.isPresent()) {
	    return "Error!";
	}
	BankClient currentClient = clientOpt.get();
	currentClient.setMoneyAmount(currentClient.getMoneyAmount() + amount);

	// Log transaction.
	TransactionLog log = new TransactionLog();
	log.setCreationDate(new Date())
			.setRelatedClientId(currentClient.getClientId())
			.setMessage("Added $" + amount + " to "+ currentClient.getClientName()
					+ ", balance: $" + currentClient.getMoneyAmount());
        transactionLogDao.save(log);

	return "Success!";
    }

}

