package com.eatsfine.eatsfine.domain.term.repository;

import com.eatsfine.eatsfine.domain.term.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {

}
