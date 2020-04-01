package com.notsoold.maisec.forum.model;

import javax.persistence.*;

@Entity
@Table(name = "maisec_forum_user")
public class ForumUser {

    @Id
    @GeneratedValue
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", nullable = false)
    private String username;

    @Column(name = "user_capab", nullable = false)
    private ForumUserCapability userCapability = ForumUserCapability.CONSUMER;

    public Long getUserId() {
	return userId;
    }

    public String getUsername() {
	return username;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public ForumUserCapability getUserCapability() {
	return userCapability;
    }

    public void setUserCapability(ForumUserCapability userCapability) {
	this.userCapability = userCapability;
    }

}
