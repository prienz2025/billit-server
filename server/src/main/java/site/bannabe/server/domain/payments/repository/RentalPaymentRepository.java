package site.bannabe.server.domain.payments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.bannabe.server.domain.payments.entity.RentalPayments;

public interface RentalPaymentRepository extends JpaRepository<RentalPayments, Long> {

}