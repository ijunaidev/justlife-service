package com.justlife.repository;

import com.justlife.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(LocalDateTime endTime, LocalDateTime startTime);
}
