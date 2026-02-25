package io.agx.bookmyseat.service;

import io.agx.bookmyseat.entity.Screen;
import io.agx.bookmyseat.entity.Theater;
import io.agx.bookmyseat.repository.ScreenRepository;
import io.agx.bookmyseat.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheaterRepository theaterRepository;

    public List<Screen> getScreensByTheater(Long theaterId) {
        return screenRepository.findByTheaterId(theaterId);
    }

    public Optional<Screen> getScreenById(Long id) {
        return screenRepository.findById(id);
    }

    public Screen createScreen(Long theaterId, Screen screen) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new IllegalArgumentException("Theater not found: " + theaterId));

        if (screenRepository.existsByNameAndTheaterId(screen.getName(), theaterId)) {
            throw new IllegalArgumentException("Screen already exists in this theater: " + screen.getName());
        }

        screen.setTheater(theater);
        return screenRepository.save(screen);
    }

    @Transactional
    public Screen updateScreen(Long id, Screen updated) {
        Screen existing = screenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Screen not found: " + id));

        existing.setName(updated.getName());

        return screenRepository.save(existing);
    }

    @Transactional
    public void deleteScreen(Long id) {
        if (!screenRepository.existsById(id)) {
            throw new IllegalArgumentException("Screen not found: " + id);
        }
        screenRepository.deleteById(id);
    }
}