package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);
    
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable pageable);
    
    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, LocalDateTime now1, LocalDateTime now2, Pageable pageable);
    
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now, Pageable pageable);
    
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId AND b.status = :status ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId AND b.start < :now AND b.end > :now ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdAndCurrentOrderByStartDesc(Long ownerId, LocalDateTime now, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdAndPastOrderByStartDesc(Long ownerId, LocalDateTime now, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdAndFutureOrderByStartDesc(Long ownerId, LocalDateTime now, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' ORDER BY b.start ASC")
    List<Booking> findApprovedBookingsByItemId(Long itemId);
    
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.booker.id = :bookerId AND b.status = 'APPROVED' AND b.end < :now")
    List<Booking> findPastApprovedBookingsByItemIdAndBookerId(Long itemId, Long bookerId, LocalDateTime now);
}
