package com.notsoold.maisec.bank;

import com.notsoold.maisec.bank.dao.BankClientDao;
import com.notsoold.maisec.bank.model.BankClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Component
public class DataInit implements ApplicationRunner {

    private BankClientDao bankClientDao;
    private UserDetailsManager userDetailsManager;

    public static DateFormat fmt = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

    @Autowired
    public DataInit(BankClientDao bankClientDao, UserDetailsManager userDetailsManager) {
	this.bankClientDao = bankClientDao;
	this.userDetailsManager = userDetailsManager;
    }

    @Override
    public void run(ApplicationArguments args) {
	if (!userDetailsManager.userExists("alice")) {
	    registerUserAndClient("alice", "{noop}1");
	    registerUserAndClient("bob", "{noop}1");
	    registerUserAndClient("hacker", "{noop}hack0r");
	}
    }

    private void registerUserAndClient(String name, String password) {
	BankClient client = new BankClient();
	client.setClientName(name);
	client.setMoneyAmount(1000L);
	bankClientDao.save(client);

	UserDetails user = User.builder()
			.username(name)
			.password(password)
			.authorities(new SimpleGrantedAuthority("ROLE_USER"))
			.build();
	userDetailsManager.createUser(user);
    }

}
