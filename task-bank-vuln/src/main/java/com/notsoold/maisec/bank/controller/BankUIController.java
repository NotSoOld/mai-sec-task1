package com.notsoold.maisec.bank.controller;

import com.notsoold.maisec.bank.DataInit;
import com.notsoold.maisec.bank.dao.BankClientDao;
import com.notsoold.maisec.bank.dao.TransactionLogDao;
import com.notsoold.maisec.bank.model.BankClient;
import com.notsoold.maisec.bank.model.TransactionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
public class BankUIController {

    private BankClientDao bankClientDao;
    private TransactionLogDao transactionLogDao;

    @Autowired
    public BankUIController(BankClientDao bankClientDao, TransactionLogDao transactionLogDao) {
        this.bankClientDao = bankClientDao;
        this.transactionLogDao = transactionLogDao;
    }

    @GetMapping("/homepage")
    public String homepage(Model model, Principal principal) {
	model.addAttribute("currentUserName", principal.getName());
	Optional<BankClient> clientOpt = bankClientDao.findByClientName(principal.getName());
	clientOpt.ifPresent(bankClient -> model.addAttribute("myMoneyAmount", bankClient.getMoneyAmount()));
	StringBuilder transactionLogBuilder = new StringBuilder();
	Iterable<TransactionLog> logEntries = transactionLogDao.findAll();
	for (TransactionLog entry: logEntries) {
	    Optional<BankClient> clientWhoMadeTransaction = bankClientDao.findById(entry.getRelatedClientId());
	    if (!clientWhoMadeTransaction.isPresent()) {
	        continue;
	    }
	    if (principal.getName().equals(clientWhoMadeTransaction.get().getClientName())) {
		transactionLogBuilder.append("<p><b>").append(DataInit.fmt.format(entry.getCreationDate()))
				.append("</b>   ").append(entry.getMessage()).append("</p>");
	    }
	}
	model.addAttribute("transactionLog", transactionLogBuilder.toString());

	return "homepage";
    }

}
