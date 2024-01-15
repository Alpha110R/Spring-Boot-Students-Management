package com.alon.springboot.repository;

import com.alon.springboot.model.objects.DBUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DBUserRepository extends CrudRepository<DBUser,Long> {
    Optional<DBUser> findByName(String name);
}
