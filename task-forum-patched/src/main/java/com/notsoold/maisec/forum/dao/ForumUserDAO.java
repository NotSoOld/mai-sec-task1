package com.notsoold.maisec.forum.dao;

import com.notsoold.maisec.forum.model.ForumUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ForumUserDAO extends CrudRepository<ForumUser, Long> {

    Optional<ForumUser> findByUsername(String username);

}
