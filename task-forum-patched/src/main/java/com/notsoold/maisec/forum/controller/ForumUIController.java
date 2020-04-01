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
import org.springframework.web.util.HtmlUtils;
import org.unbescape.html.HtmlEscape;

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
	    List<ForumEntry> headerForumEntriesCurUserCanRead =
		forumEntryDAO.findForumEntriesByMessageIndexEqualsAndAuthorUserCapabilityLessThanEqual(
				0, getCapabilityToReadThreads(currentUser.get()));

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
				.append(HtmlUtils.htmlEscape(headerEntry.getText())).append("</a> - <b>").append(entry.getAuthor().getUsername())
				.append("</b> on ").append(DataInit.fmt.format(entry.getPublishDate())).append("</p>");
	    }

	    model.addAttribute("recentThreadLinks", recentThreadLinksBuilder.toString());
	}

	return "homepage";
    }


    @GetMapping("/threads/{threadId}")
    public String getThread(@PathVariable("threadId") Long threadId, Model model, Principal principal) throws Exception {
	List<ForumEntry> threadEntries = forumEntryDAO.findForumEntriesByThreadIdEqualsOrderByMessageIndex(threadId);
	StringBuilder threadEntriesBuilder = new StringBuilder();
	String threadEntryTemplate = "<p><b>%s</b>    <i>%s</i></p><p>%s</p><br><hr>";
	boolean canReply = false;
	for (ForumEntry entry: threadEntries) {
	    if (entry.getMessageIndex() == 0) {
		Optional<ForumUser> currentUser = forumUserDAO.findByUsername(principal.getName());
	        /* We should include a check whether current user can see this thread because of a possibility of IDOR attack. */
		int currentUserCapabilityToRead = currentUser.orElseThrow(Exception::new).getUserCapability().ordinal() + 1;
		if (currentUserCapabilityToRead < entry.getAuthor().getUserCapability().ordinal()
				&& entry.getAuthor().getUserCapability() != ForumUserCapability.MANAGER) {
		    model.addAttribute("threadHeader", "Thread unavailable");
		    threadEntriesBuilder.append("You don't have permissions to access this thread.");
		    break;
		}

	        model.addAttribute("threadHeader",
				HtmlUtils.htmlEscape(entry.getText()) + " by " + entry.getAuthor().getUsername());

		if (entry.getAuthor().getUserCapability() == ForumUserCapability.MANAGER
			|| currentUser.get().getUserCapability().compareTo(ForumUserCapability.CONTRIBUTOR) >= 0) {
	            canReply = true;
		}
	    }
	    threadEntriesBuilder.append(String.format(threadEntryTemplate, entry.getAuthor().getUsername(),
			    DataInit.fmt.format(entry.getPublishDate()), HtmlUtils.htmlEscape(entry.getText())));
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
	    /* Use parameters to prevent SQL injection attack. */
	    String queryString = "SELECT e FROM ForumEntry e WHERE e.text LIKE ?1 AND e.threadId IN "
			    + "(SELECT e1.threadId FROM ForumEntry e1 WHERE e1.messageIndex = 0 AND e1.author IN "
			    + "(SELECT u.userId FROM ForumUser u WHERE u.userCapability <= ?2 ) ) ORDER BY e.publishDate DESC";
	    Query query = entityManager.createQuery(queryString);
	    query.setParameter(1, "%" + searchString + "%");
	    query.setParameter(2, getCapabilityToReadThreads(currentUser.get()));
	    List<ForumEntry> foundEntries = query.getResultList();

	    String threadEntryTemplate = "<p><b>%s</b>    <i>%s</i> in <b>%s</b>:</p><p>%s</p><br><hr>";
	    StringBuilder searchResultsBuilder = new StringBuilder();
	    int resultsCnt = 0;
	    for (ForumEntry foundEntry: foundEntries) {
		ForumEntry headerEntry = forumEntryDAO
				.findForumEntryByThreadIdEqualsAndMessageIndexEquals(foundEntry.getThreadId(), 0);
		searchResultsBuilder.append(String.format(threadEntryTemplate, foundEntry.getAuthor().getUsername(),
				DataInit.fmt.format(foundEntry.getPublishDate()), HtmlUtils.htmlEscape(headerEntry.getText()),
				HtmlUtils.htmlEscape(foundEntry.getText())));
		resultsCnt++;
	    }

	    model.addAttribute("searchResultsCnt", resultsCnt);
	    model.addAttribute("searchresults", searchResultsBuilder.toString());
	}

        return "searchresults";
    }

    /* Or just don't use raw SQL at all:

    @PostMapping("/search")
    public String search(@RequestParam("searchString") String searchString, Model model, Principal principal) {
	Optional<ForumUser> currentUser = forumUserDAO.findByUsername(principal.getName());
	if (currentUser.isPresent()) {
	    List<ForumEntry> foundEntries = forumEntryDAO.findForumEntriesByTextLikeOrderByPublishDateDesc("%" + searchString + "%");
	    String threadEntryTemplate = "<p><b>%s</b>    <i>%s</i> in <b>%s</b>:</p><p>%s</p><br><hr>";
	    StringBuilder searchResultsBuilder = new StringBuilder();
	    Set<Long> prohibitedThreadIds = new HashSet<>();
	    int resultsCnt = 0;
	    for (ForumEntry foundEntry: foundEntries) {
	        if (currentUser.get().getUserCapability() != ForumUserCapability.ADMINISTRATOR) {
		    if (prohibitedThreadIds.contains(foundEntry.getThreadId())) {
			// Already checked that thread, and there are no permissions to see it.
			continue;
		    }
		    if (foundEntry.getMessageIndex() == 0 &&
				    currentUser.get().getUserCapability().compareTo(foundEntry.getAuthor().getUserCapability()) < 0) {
			// No permissions to see this thread.
			prohibitedThreadIds.add(foundEntry.getThreadId());
			continue;
		    }
		    // We need to check if this entry from thread is visible to current user.
		    // This can be done by checking the header entry of this thread.
		    ForumEntry headerEntry = forumEntryDAO
				    .findForumEntryByThreadIdEqualsAndMessageIndexEquals(foundEntry.getThreadId(), 0);
		    if (currentUser.get().getUserCapability().compareTo(headerEntry.getAuthor().getUserCapability()) < 0) {
			prohibitedThreadIds.add(foundEntry.getThreadId());
			continue;
		    }

		    searchResultsBuilder.append(String.format(threadEntryTemplate, foundEntry.getAuthor().getUsername(),
				    DataInit.fmt.format(foundEntry.getPublishDate()), headerEntry.getText(), foundEntry.getText()));

		} else {
		    ForumEntry headerEntry = forumEntryDAO
				    .findForumEntryByThreadIdEqualsAndMessageIndexEquals(foundEntry.getThreadId(), 0);
		    searchResultsBuilder.append(String.format(threadEntryTemplate, foundEntry.getAuthor().getUsername(),
				    DataInit.fmt.format(foundEntry.getPublishDate()), headerEntry.getText(), foundEntry.getText()));
		}

		resultsCnt++;
	    }

	    model.addAttribute("searchResultsCnt", resultsCnt);
	    model.addAttribute("searchresults", searchResultsBuilder.toString());

	}

        return "searchresults";
    }


     */


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
			.setText(HtmlUtils.htmlEscape(replyText));
	forumEntryDAO.save(newEntry);

	return "Reply added!<br><a href=\"/threads/" + threadId + "\">Return</a>";
    }


    private ForumUserCapability getCapabilityToReadThreads(ForumUser currentUser) {
	int currentUserCapabilityToRead = currentUser.getUserCapability().ordinal() + 1;
	if (currentUserCapabilityToRead > ForumUserCapability.ADMINISTRATOR.ordinal()) {
	    currentUserCapabilityToRead = ForumUserCapability.ADMINISTRATOR.ordinal();
	}
	return ForumUserCapability.values()[currentUserCapabilityToRead];
    }


}
