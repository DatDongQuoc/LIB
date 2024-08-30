package com.mycompany.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycompany.user.entity.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {

}
