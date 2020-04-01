package com.notsoold.maisec.forum.controller;

import com.notsoold.maisec.forum.dao.ForumEntryDAO;
import com.notsoold.maisec.forum.dao.ForumUserDAO;
import com.notsoold.maisec.forum.model.ForumUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
public class AdminPageController {

    private ForumUserDAO forumUserDAO;
    private ForumEntryDAO forumEntryDAO;

    @Autowired
    public AdminPageController(ForumUserDAO forumUserDAO, ForumEntryDAO forumEntryDAO) {
	this.forumUserDAO = forumUserDAO;
	this.forumEntryDAO = forumEntryDAO;
    }

    @PostMapping("/admin/deleteuser")
    @ResponseBody
    public String adminDeleteUser(@RequestParam("username") String username) {
	Optional<ForumUser> userToDelete = forumUserDAO.findByUsername(username);
	Optional<ForumUser> substitutionUser = forumUserDAO.findByUsername("<deleted_user>");

        userToDelete.ifPresent(user -> {
		forumEntryDAO.findForumEntriesByAuthorUsernameEquals(username).forEach(forumEntry -> {
		    try {
		    	forumEntry.setAuthor(substitutionUser.orElseThrow(Exception::new));

		    } catch (Exception e) {
			e.printStackTrace();
		    }
		});

		forumUserDAO.delete(user);
	});

        return "<p>Success!</p><a href=\"/secret_admin_page.html\">Return to admin page</a>";
    }

}
