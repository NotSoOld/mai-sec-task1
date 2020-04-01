package com.notsoold.maisec.forum.controller;

import com.notsoold.maisec.forum.DataInit;
import com.notsoold.maisec.forum.dao.ForumEntryDAO;
import com.notsoold.maisec.forum.dao.ForumUserDAO;
import com.notsoold.maisec.forum.model.ForumEntry;
import com.notsoold.maisec.forum.model.ForumUser;
import com.notsoold.maisec.forum.model.ForumUserCapability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ForumUIController {

    private ForumUserDAO forumUserDAO;
    private ForumEntryDAO forumEntryDAO;
    private EntityManager entityManager;

    @Autowired
    public ForumUIController(ForumUserDAO forumUserDAO, ForumEntryDAO forumEntryDAO, EntityManager entityManager) {
        this.forumUserDAO = forumUserDAO;
        this.forumEntryDAO = forumEntryDAO;
        this.entityManager = entityManager;
    }

    @GetMapping("/homepage")
    public String homepage(Model model, Principal principal) {
	model.addAttribute("currentUserName", principal.getName());

	StringBuilder recentThreadLinksBuilder = new StringBuilder();
	Optional<ForumUser> currentUser = forumUserDAO.findByUsername(principal.getName());
	if (currentUser.isPresent()) {
	    int currentUserCapabilityToRead = currentUser.get().getUserCapability().ordinal() + 1;
	    if (currentUserCapabilityToRead > ForumUserCapability.ADMINISTRATOR.ordinal()) {
	        currentUserCapabilityToRead = ForumUserCapability.ADMINISTRATOR.ordinal();
	    }
	    List<ForumEntry> headerForumEntriesCurUserCanRead =
		forumEntryDAO.findForumEntriesByMessageIndexEqualsAndAuthorUserCapabilityLessThanEqual(
				0, ForumUserCapability.values()[currentUserCapabilityToRead]);

	    headerForumEntriesCurUserCanRead.addAll(
	    		forumEntryDAO.findForumEntriesByMessageIndexEqualsAndAuthorUserCapabilityEquals(
	    				0, ForumUserCapability.MANAGER));

	    List<Long> forumEntriesThreadIdsCurUserCanRead =
			    headerForumEntriesCurUserCanRead.stream().map(ForumEntry::getThreadId).collect(Collectors.toList());

	    List<ForumEntry> latestEntries =
		forumEntryDAO.findForumEntriesByThreadIdInAndLatestEntryTrueOrderByPublishDateDesc(forumEntriesThreadIdsCurUserCanRead);

	    for (ForumEntry entry: latestEntries) {
	        ForumEntry headerEntry = forumEntryDAO.findForumEntryByThreadIdEqualsAndMessageIndexEquals(entry.getThreadId(), 0);
		recentThreadLinksBuilder.append("<p><a href=\"threads/").append(entry.getThreadId()).append("\">")
				.append(headerEntry.getText()).append("</a> - <b>").append(entry.getAuthor().getUsername())
				.append("</b> on ").append(DataInit.fmt.format(entry.getPublishDate())).append("</p>");
	    }

	    model.addAttribute("recentThreadLinks", recentThreadLinksBuilder.toString());
	}

	return "homepage";
    }


    @GetMapping("/threads/{threadId}")
    public String getThread(@PathVariable("threadId") Long threadId, Model model, Principal principal) {
	List<ForumEntry> threadEntries = forumEntryDAO.findForumEntriesByThreadIdEqualsOrderByMessageIndex(threadId);
	StringBuilder threadEntriesBuilder = new StringBuilder();
	String threadEntryTemplate = "<p><b>%s</b>    <i>%s</i></p><p>%s</p><br><hr>";
	boolean canReply = false;
	for (ForumEntry entry: threadEntries) {
	    if (entry.getMessageIndex() == 0) {
	        model.addAttribute("threadHeader", entry.getText() + " by " + entry.getAuthor().getUsername());
		Optional<ForumUser> currentUser = forumUserDAO.findByUsername(principal.getName());
		if (entry.getAuthor().getUserCapability() == ForumUserCapability.MANAGER
			|| currentUser.isPresent() && currentUser.get().getUserCapability().compareTo(ForumUserCapability.CONTRIBUTOR) >= 0) {
	            canReply = true;
		}
	    }
	    threadEntriesBuilder.append(String.format(threadEntryTemplate, entry.getAuthor().getUsername(),
			    DataInit.fmt.format(entry.getPublishDate()), entry.getText()));
	}
	model.addAttribute("threadEntries", threadEntriesBuilder.toString());
	model.addAttribute("canReply", canReply);
	model.addAttribute("threadId", threadId);

        return "threads";
    }


    @SuppressWarnings("unchecked")
    @PostMapping("/search")
    public String search(@RequestParam("searchString") String searchString, Model model, Principal principal) {
	Optional<ForumUser> currentUser = forumUserDAO.findByUsername(principal.getName());
	if (currentUser.isPresent() && StringUtils.hasText(searchString)) {
	    @SuppressWarnings("JpaQlInspection")
	    String queryString = "SELECT e FROM ForumEntry e WHERE e.text LIKE '%" + searchString + "%' AND e.threadId IN "
			    + "(SELECT e1.threadId FROM ForumEntry e1 WHERE e1.messageIndex = 0 AND e1.author IN "
			    + "(SELECT u.userId FROM ForumUser u WHERE u.userCapability <= "
			    + (currentUser.get().getUserCapability().ordinal() + 1) + " ) ) ORDER BY e.publishDate DESC";
	    Query query = entityManager.createQuery(queryString);
	    List<ForumEntry> foundEntries = query.getResultList();

	    String threadEntryTemplate = "<p><b>%s</b>    <i>%s</i> in <b>%s</b>:</p><p>%s</p><br><hr>";
	    StringBuilder searchResultsBuilder = new StringBuilder();
	    int resultsCnt = 0;
	    for (ForumEntry foundEntry: foundEntries) {
		ForumEntry headerEntry = forumEntryDAO
				.findForumEntryByThreadIdEqualsAndMessageIndexEquals(foundEntry.getThreadId(), 0);
		searchResultsBuilder.append(String.format(threadEntryTemplate, foundEntry.getAuthor().getUsername(),
				DataInit.fmt.format(foundEntry.getPublishDate()), headerEntry.getText(), foundEntry.getText()));
		resultsCnt++;
	    }

	    model.addAttribute("searchResultsCnt", resultsCnt);
	    model.addAttribute("searchresults", searchResultsBuilder.toString());
	}

        return "searchresults";
    }


    @PostMapping("/addreply")
    @ResponseBody
    public String addReply(@RequestParam("replyText") String replyText, @RequestParam("threadId") Long threadId, Principal principal) {
        List<ForumEntry> currentThreadEntries = forumEntryDAO.findForumEntriesByThreadIdEqualsOrderByMessageIndex(threadId);
        ForumEntry lastEntry = currentThreadEntries.get(currentThreadEntries.size() - 1);
        lastEntry.setLatestEntry(false);
	forumEntryDAO.save(lastEntry);
        Optional<ForumUser> currentUser = forumUserDAO.findByUsername(principal.getName());
        if (!currentUser.isPresent()) {
            return "Error!";
	}
        ForumEntry newEntry = new ForumEntry().setLatestEntry(true).setAuthor(currentUser.get())
			.setMessageIndex(lastEntry.getMessageIndex() + 1)
			.setPublishDate(new Date())
			.setThreadId(lastEntry.getThreadId())
			.setText(replyText);
	forumEntryDAO.save(newEntry);

	return "Reply added!<br><a href=\"/threads/" + threadId + "\">Return</a>";
    }


}
