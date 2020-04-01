package com.notsoold.maisec.forum.model;

import javax.persistence.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

@Entity
@Table(name = "maisec_forum_entry")
public class ForumEntry {

    public final static AtomicLong lastThreadId = new AtomicLong(1000L);

    @Id
    @GeneratedValue
    @Column(name = "uid", nullable = false)
    private Long uid;

    /**
     * Message text or header text (if {@code messageIndex} == 0).
     */
    @Column(name = "text", nullable = false)
    private String text;


    /**
     * Composes all messages with the same threadId into a forum thread.
     */
    @Column(name = "thread_id", nullable = false)
    private Long threadId;

    /**
     * Message #0 is the header (a topic start).
     */
    @Column(name = "message_idx", nullable = false)
    private Integer messageIndex;

    @ManyToOne(targetEntity = ForumUser.class)
    @JoinColumn(name = "author", nullable = false)
    private ForumUser author;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "publish_date", nullable = false)
    private Date publishDate;

    @Column(name = "latest_entry", nullable = false)
    private Boolean latestEntry;

    public Long getUid() {
	return uid;
    }

    public String getText() {
	return text;
    }

    public ForumEntry setText(String text) {
	this.text = text;
	return this;
    }

    public Long getThreadId() {
	return threadId;
    }

    public ForumEntry setThreadId(Long threadId) {
	this.threadId = threadId;
	return this;
    }

    public Integer getMessageIndex() {
	return messageIndex;
    }

    public ForumEntry setMessageIndex(Integer messageIndex) {
	this.messageIndex = messageIndex;
	return this;
    }

    public ForumUser getAuthor() {
	return author;
    }

    public ForumEntry setAuthor(ForumUser author) {
	this.author = author;
	return this;
    }

    public Date getPublishDate() {
	return publishDate;
    }

    public ForumEntry setPublishDate(Date publishDate) {
	this.publishDate = publishDate;
	return this;
    }

    public Boolean getLatestEntry() {
	return latestEntry;
    }

    public ForumEntry setLatestEntry(Boolean latestEntry) {
	this.latestEntry = latestEntry;
	return this;
    }
}
