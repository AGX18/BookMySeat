package io.agx.bookmyseat.service;

import io.agx.bookmyseat.entity.Theater;
import io.agx.bookmyseat.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;

    public List<Theater> getAllTheaters() {
        return theaterRepository.findAll();
    }

    public Optional<Theater> getTheaterById(Long id) {
        return theaterRepository.findById(id);
    }

    public List<Theater> getTheatersByCity(String city) {
        return theaterRepository.findByCityIgnoreCase(city);
    }

    public Theater createTheater(Theater theater) {
        if (theaterRepository.existsByNameAndCity(theater.getName(), theater.getCity())) {
            throw new IllegalArgumentException("Theater already exists in this city: " + theater.getName());
        }
        return theaterRepository.save(theater);
    }

    @Transactional
    public Theater updateTheater(Long id, Theater updated) {
        Theater existing = theaterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Theater not found: " + id));

        existing.setName(updated.getName());
        existing.setCity(updated.getCity());
        existing.setAddress(updated.getAddress());

        return theaterRepository.save(existing);
    }

    @Transactional
    public void deleteTheater(Long id) {
        if (!theaterRepository.existsById(id)) {
            throw new IllegalArgumentException("Theater not found: " + id);
        }
        theaterRepository.deleteById(id);
    }
}