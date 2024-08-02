package com.justlife.repository;

import com.justlife.model.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {
    
    @Transactional
    void deleteByBookingId(Long bookingId);
    List<BookingDetail> findByCleaningProfessionalIdAndBookingStartTimeBetween(Long professionalId, LocalDateTime startTime, LocalDateTime endTime);
}
