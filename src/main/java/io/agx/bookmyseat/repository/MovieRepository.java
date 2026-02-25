package io.agx.bookmyseat.repository;

import io.agx.bookmyseat.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByGenre(String genre);

    List<Movie> findByTitleContainingIgnoreCase(String title);

    List<Movie> findByReleaseDateAfter(LocalDate date);

    List<Movie> findByGenreAndReleaseDateAfter(String genre, LocalDate date);
}