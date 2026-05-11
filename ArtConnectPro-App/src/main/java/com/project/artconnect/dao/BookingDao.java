package com.project.artconnect.dao;

import com.project.artconnect.model.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingDao {
    Optional<Booking> findById(Long id);

    List<Booking> findAll();

    List<Booking> findByMemberId(Long memberId);

    List<Booking> findByWorkshopId(Long workshopId);

    void save(Booking booking);

    void update(Booking booking);

    void delete(Long id);
}
