package com.example.backend.repository;

import com.example.backend.entity.Obligation;
import com.example.backend.enums.Category;
import com.example.backend.enums.Status;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObligationRepository extends JpaRepository<Obligation, UUID> {

  boolean existsByStatusAndTitleIgnoreCase(Status status, String title);

  List<Obligation> findByCategoryAndStatus(Category category, Status status);

  List<Obligation> findByCategory(Category category);

  List<Obligation> findByStatus(Status status);

  List<Obligation> findAll();

  List<Obligation> findByNextPaymentDateBetween(LocalDate start, LocalDate end);
}
