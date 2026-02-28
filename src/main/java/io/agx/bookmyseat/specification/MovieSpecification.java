package io.agx.bookmyseat.specification;

import io.agx.bookmyseat.entity.Movie;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovieSpecification {

    public static Specification<Movie> filter(String genre, String title, LocalDate releasedAfter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (genre != null) {
                predicates.add(cb.equal(root.get("genre"), genre));
            }
            if (title != null) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (releasedAfter != null) {
                predicates.add(cb.greaterThan(root.get("releaseDate"), releasedAfter));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}