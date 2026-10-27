package com.caseflow.operator;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatorRepository extends JpaRepository<Operator, Long> {
    List<Operator> findAllByOrderByDisplayNameAsc();
}
