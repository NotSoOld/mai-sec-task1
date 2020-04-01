package com.notsoold.maisec.forum.dao;

import com.notsoold.maisec.forum.model.ForumEntry;
import com.notsoold.maisec.forum.model.ForumUserCapability;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface ForumEntryDAO extends CrudRepository<ForumEntry, Long> {

    List<ForumEntry> findForumEntriesByMessageIndexEqualsAndAuthorUserCapabilityLessThanEqual(Integer messageIndex, ForumUserCapability userCapability);

    List<ForumEntry> findForumEntriesByMessageIndexEqualsAndAuthorUserCapabilityEquals(Integer messageIndex, ForumUserCapability userCapability);

    List<ForumEntry> findForumEntriesByThreadIdInAndLatestEntryTrueOrderByPublishDateDesc(Collection<Long> threadIds);

    ForumEntry findForumEntryByThreadIdEqualsAndMessageIndexEquals(Long threadId, Integer messageIndex);

    List<ForumEntry> findForumEntriesByThreadIdEqualsOrderByMessageIndex(Long threadId);

    List<ForumEntry> findForumEntriesByTextLikeOrderByPublishDateDesc(String textLike);

    List<ForumEntry> findForumEntriesByAuthorUsernameEquals(String authorName);

}
