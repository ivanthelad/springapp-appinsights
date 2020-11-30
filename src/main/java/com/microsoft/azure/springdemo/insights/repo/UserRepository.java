package com.microsoft.azure.springdemo.insights.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public  interface UserRepository extends CrudRepository<User, Long> {
    Iterable<User> findAll(Sort sort);

    Page<User> findAll(Pageable pageable);
}
