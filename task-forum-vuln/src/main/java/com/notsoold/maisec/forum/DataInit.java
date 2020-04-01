package com.notsoold.maisec.forum;

import com.notsoold.maisec.forum.dao.ForumEntryDAO;
import com.notsoold.maisec.forum.dao.ForumUserDAO;
import com.notsoold.maisec.forum.model.ForumEntry;
import com.notsoold.maisec.forum.model.ForumUser;
import com.notsoold.maisec.forum.model.ForumUserCapability;
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
import java.util.Date;

@Component
public class DataInit implements ApplicationRunner {

    private ForumUserDAO forumUserDAO;
    private ForumEntryDAO forumEntryDAO;
    private UserDetailsManager userDetailsManager;

    public static DateFormat fmt = new SimpleDateFormat("dd.MM.yyyy hh:mm");

    @Autowired
    public DataInit(ForumUserDAO forumUserDAO, ForumEntryDAO forumEntryDAO, UserDetailsManager userDetailsManager) {
	this.forumUserDAO = forumUserDAO;
	this.forumEntryDAO = forumEntryDAO;
	this.userDetailsManager = userDetailsManager;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!userDetailsManager.userExists("alice")) {
            registerUserAndClient("alice", "{noop}1");
	    registerUserAndClient("bob", "{noop}1", ForumUserCapability.CONTRIBUTOR);
	    registerUserAndClient("manager", "{noop}mngr", ForumUserCapability.MANAGER);
	    registerUserAndClient("admin1", "{noop}superpa$$word1", ForumUserCapability.ADMINISTRATOR);
	    registerUserAndClient("admin2", "{noop}superpa$$word2", ForumUserCapability.ADMINISTRATOR);
	    registerUserAndClient("admin3", "{noop}superpa$$word3", ForumUserCapability.ADMINISTRATOR);
	    registerUserAndClient("hacker", "{noop}Imhack0r");
	    registerUserAndClient("<deleted_user>", "{noop}nopasswordreally");

	    addForumThreads();
	}
    }

    private void registerUserAndClient(String name, String password) {
	ForumUser client = new ForumUser();
	client.setUsername(name);
	client = forumUserDAO.save(client);

	UserDetails user = User.builder()
			.username(name)
			.password(password)
			.authorities(new SimpleGrantedAuthority(client.getUserCapability().toString()))
			.build();
	userDetailsManager.createUser(user);
    }

    private void registerUserAndClient(String name, String password, ForumUserCapability capability) {
	ForumUser client = new ForumUser();
	client.setUsername(name);
	client.setUserCapability(capability);
	client = forumUserDAO.save(client);

	UserDetails user = User.builder()
			.username(name)
			.password(password)
			.authorities(new SimpleGrantedAuthority(client.getUserCapability().toString()))
			.build();
	userDetailsManager.createUser(user);
    }

    private void addForumThreads() {
        try {
	    ForumEntry headerEntry1 = new ForumEntry().setLatestEntry(false).setThreadId(ForumEntry.lastThreadId.incrementAndGet())
			    .setAuthor(forumUserDAO.findByUsername("bob").orElseThrow(Exception::new))
			    .setMessageIndex(0)
			    .setPublishDate(fmt.parse("12.11.2019 15:34"))
			    .setText("Мы растем, ребята!");
	    ForumEntry headerEntry2 = new ForumEntry().setLatestEntry(false).setThreadId(ForumEntry.lastThreadId.incrementAndGet())
			    .setAuthor(forumUserDAO.findByUsername("bob").orElseThrow(Exception::new))
			    .setMessageIndex(0)
			    .setPublishDate(fmt.parse("12.10.2019 15:00"))
			    .setText("Опять эти кактусы");
	    ForumEntry headerEntry3 = new ForumEntry().setLatestEntry(false).setThreadId(ForumEntry.lastThreadId.incrementAndGet())
			    .setAuthor(forumUserDAO.findByUsername("bob").orElseThrow(Exception::new))
			    .setMessageIndex(0)
			    .setPublishDate(fmt.parse("14.04.2019 10:34"))
			    .setText("Никто не желает выпить чаю в переговорной?");
	    ForumEntry headerEntry4 = new ForumEntry().setLatestEntry(false).setThreadId(ForumEntry.lastThreadId.incrementAndGet())
			    .setAuthor(forumUserDAO.findByUsername("admin1").orElseThrow(Exception::new))
			    .setMessageIndex(0)
			    .setPublishDate(fmt.parse("18.12.2019 11:31"))
			    .setText("Дизайн сайта");
	    ForumEntry headerEntry5 = new ForumEntry().setLatestEntry(false).setThreadId(ForumEntry.lastThreadId.incrementAndGet())
			    .setAuthor(forumUserDAO.findByUsername("admin3").orElseThrow(Exception::new))
			    .setMessageIndex(0)
			    .setPublishDate(fmt.parse("22.06.2019 14:34"))
			    .setText("А что насчет безопасности?");
	    ForumEntry headerEntry6 = new ForumEntry().setLatestEntry(false).setThreadId(ForumEntry.lastThreadId.incrementAndGet())
			    .setAuthor(forumUserDAO.findByUsername("manager").orElseThrow(Exception::new))
			    .setMessageIndex(0)
			    .setPublishDate(fmt.parse("22.06.2018 14:34"))
			    .setText("Обратная связь (здесь могут писать даже потребители)");

	    headerEntry1 = forumEntryDAO.save(headerEntry1);
	    headerEntry2 = forumEntryDAO.save(headerEntry2);
	    headerEntry3 = forumEntryDAO.save(headerEntry3);
	    headerEntry4 = forumEntryDAO.save(headerEntry4);
	    headerEntry5 = forumEntryDAO.save(headerEntry5);
	    headerEntry6 = forumEntryDAO.save(headerEntry6);

	    ForumEntry entry11 = new ForumEntry().setLatestEntry(false).setThreadId(headerEntry1.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("bob").orElseThrow(Exception::new))
			    .setMessageIndex(1)
			    .setPublishDate(fmt.parse("12.11.2019 15:42"))
			    .setText("У нас уже три действующих клиента: Боб, Алиса и... какой-то Хакер, но неважно. Как будем праздновать?");
	    ForumEntry entry21 = new ForumEntry().setLatestEntry(false).setThreadId(headerEntry2.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("bob").orElseThrow(Exception::new))
			    .setMessageIndex(1)
			    .setPublishDate(fmt.parse("12.10.2019 15:08"))
			    .setText("Кто-то поставил в мое отсутствие кактус рядом с моим монитором! Это не смешно!!");
	    ForumEntry entry31 = new ForumEntry().setLatestEntry(true).setThreadId(headerEntry3.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("bob").orElseThrow(Exception::new))
			    .setMessageIndex(1)
			    .setPublishDate(fmt.parse("14.04.2019 11:34"))
			    .setText("Что, совсем никто?");
	    ForumEntry entry41 = new ForumEntry().setLatestEntry(false).setThreadId(headerEntry4.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("admin1").orElseThrow(Exception::new))
			    .setMessageIndex(1)
			    .setPublishDate(fmt.parse("18.12.2019 11:54"))
			    .setText("Менеджер сказал, что с этим надо что-то делать.");
	    ForumEntry entry51 = new ForumEntry().setLatestEntry(false).setThreadId(headerEntry5.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("admin3").orElseThrow(Exception::new))
			    .setMessageIndex(1)
			    .setPublishDate(fmt.parse("22.06.2019 14:52"))
			    .setText("Я придумал: этот раздел форума всё равно не видно никому, кроме нас, давайте оставим наши пароли здесь.\nМой - superpa$$word3");
	    ForumEntry entry61 = new ForumEntry().setLatestEntry(false).setThreadId(headerEntry6.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("manager").orElseThrow(Exception::new))
			    .setMessageIndex(1)
			    .setPublishDate(fmt.parse("22.06.2018 14:43"))
			    .setText("Буду первый. Рад здесь всех видеть, пользуйтесь нашим банком на localhost:6080!");

	    forumEntryDAO.save(entry11);
	    forumEntryDAO.save(entry21);
	    forumEntryDAO.save(entry31);
	    forumEntryDAO.save(entry41);
	    forumEntryDAO.save(entry51);
	    forumEntryDAO.save(entry61);

	    ForumEntry entry12 = new ForumEntry().setLatestEntry(true).setThreadId(headerEntry1.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("manager").orElseThrow(Exception::new))
			    .setMessageIndex(2)
			    .setPublishDate(fmt.parse("12.11.2019 15:34"))
			    .setText("Отлично! Твоя зарплата за это теперь увеличивается в 0.9 раз.");
	    ForumEntry entry22 = new ForumEntry().setLatestEntry(true).setThreadId(headerEntry2.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("manager").orElseThrow(Exception::new))
			    .setMessageIndex(2)
			    .setPublishDate(fmt.parse("12.10.2019 15:00"))
			    .setText("*хихикает*");
	    ForumEntry entry42 = new ForumEntry().setLatestEntry(true).setThreadId(headerEntry4.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("admin2").orElseThrow(Exception::new))
			    .setMessageIndex(2)
			    .setPublishDate(fmt.parse("18.12.2019 11:31"))
			    .setText("Не вижу проблем.");
	    ForumEntry entry52 = new ForumEntry().setLatestEntry(false).setThreadId(headerEntry5.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("admin2").orElseThrow(Exception::new))
			    .setMessageIndex(2)
			    .setPublishDate(fmt.parse("22.06.2019 14:34"))
			    .setText("У меня superpa$$word2 :D");
	    ForumEntry entry62 = new ForumEntry().setLatestEntry(true).setThreadId(headerEntry6.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("alice").orElseThrow(Exception::new))
			    .setMessageIndex(2)
			    .setPublishDate(fmt.parse("23.06.2018 11:39"))
			    .setText("Отличный форум");

	    forumEntryDAO.save(entry12);
	    forumEntryDAO.save(entry22);
	    forumEntryDAO.save(entry42);
	    forumEntryDAO.save(entry52);
	    forumEntryDAO.save(entry62);

	    ForumEntry entry53 = new ForumEntry().setLatestEntry(false).setThreadId(headerEntry5.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("admin1").orElseThrow(Exception::new))
			    .setMessageIndex(2)
			    .setPublishDate(fmt.parse("22.06.2019 16:33"))
			    .setText("Коллеги, также напоминаю, что у нас есть <a href=\"/secret_admin_page.html\">страница для администраторов.</a>");
	    ForumEntry entry54 = new ForumEntry().setLatestEntry(false).setThreadId(headerEntry5.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("admin2").orElseThrow(Exception::new))
			    .setMessageIndex(2)
			    .setPublishDate(fmt.parse("22.06.2019 18:33"))
			    .setText("А не опасно вот так оставлять ссылку? Мало ли кто зайдет.");
	    ForumEntry entry55 = new ForumEntry().setLatestEntry(true).setThreadId(headerEntry5.getThreadId())
			    .setAuthor(forumUserDAO.findByUsername("admin1").orElseThrow(Exception::new))
			    .setMessageIndex(2)
			    .setPublishDate(fmt.parse("22.06.2019 20:33"))
			    .setText("Да лан, кто сюда зайдет. Этот тред ж только админам виден :D");

	    forumEntryDAO.save(entry53);
	    forumEntryDAO.save(entry54);
	    forumEntryDAO.save(entry55);

	} catch (Exception e) {
            e.printStackTrace();
	}
    }

}
