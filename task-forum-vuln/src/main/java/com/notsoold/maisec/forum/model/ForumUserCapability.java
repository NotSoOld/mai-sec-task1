package com.notsoold.maisec.forum.model;

public enum ForumUserCapability {
    BANNED,
    CONSUMER,
    CONTRIBUTOR,
    MANAGER,
    spacer, // we don't want managers to see administrators' threads
    ADMINISTRATOR
}
