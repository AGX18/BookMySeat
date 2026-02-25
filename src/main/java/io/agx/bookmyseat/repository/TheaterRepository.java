package io.agx.bookmyseat.repository;

import io.agx.bookmyseat.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {

    List<Theater> findByCity(String city);

    List<Theater> findByCityIgnoreCase(String city);

    boolean existsByNameAndCity(String name, String city);
}