package com.tomato.repository;

import com.tomato.model.DeliveryAssignment;
import com.tomato.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {

    List<DeliveryAssignment> findByOrderId(Long orderId);

    Optional<DeliveryAssignment> findByOrderIdAndStatus(Long orderId, DeliveryStatus status);

    Optional<DeliveryAssignment> findByOrderIdAndDeliveryPartnerId(Long orderId, Long deliveryPartnerId);

    boolean existsByOrderIdAndStatus(Long orderId, DeliveryStatus status);
}
