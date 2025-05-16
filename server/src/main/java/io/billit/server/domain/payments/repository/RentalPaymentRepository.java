package io.billit.server.domain.payments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.billit.server.domain.payments.entity.RentalPayments;

public interface RentalPaymentRepository extends JpaRepository<RentalPayments, Long> {

}