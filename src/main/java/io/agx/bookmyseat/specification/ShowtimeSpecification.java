package io.agx.bookmyseat.specification;

import io.agx.bookmyseat.entity.Showtime;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShowtimeSpecification {

    public static Specification<Showtime> filter(Long movieId, Long screenId, Long theaterId, LocalDate date) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (movieId != null) {
                predicates.add(cb.equal(root.get("movie").get("id"), movieId));
            }
            if (screenId != null) {
                predicates.add(cb.equal(root.get("screen").get("id"), screenId));
            }
            if (theaterId != null) {
                predicates.add(cb.equal(root.get("screen").get("theater").get("id"), theaterId));
            }
            if (date != null) {
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.plusDays(1).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), start));
                predicates.add(cb.lessThan(root.get("startTime"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}