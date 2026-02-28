package io.agx.bookmyseat.service;

import io.agx.bookmyseat.dto.request.CreateScreenRequest;
import io.agx.bookmyseat.dto.response.ScreenResponse;
import io.agx.bookmyseat.entity.Screen;
import io.agx.bookmyseat.entity.Theater;
import io.agx.bookmyseat.exception.DuplicateResourceException;
import io.agx.bookmyseat.exception.ResourceNotFoundException;
import io.agx.bookmyseat.repository.ScreenRepository;
import io.agx.bookmyseat.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheaterRepository theaterRepository;

    public List<ScreenResponse> getScreensByTheater(Long theaterId) {
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("Theater", theaterId);
        }
        return screenRepository.findByTheaterId(theaterId)
                .stream()
                .map(ScreenResponse::from)
                .toList();
    }

    public ScreenResponse getScreenById(Long id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", id));
        return ScreenResponse.from(screen);
    }


    public ScreenResponse createScreen(Long theaterId, CreateScreenRequest request) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater", theaterId));

        if (screenRepository.existsByNameAndTheaterId(request.getName(), theaterId)) {
            throw new DuplicateResourceException("Screen '" + request.getName() + "' already exists in this theater");
        }

        Screen screen = Screen.builder()
                .name(request.getName())
                .theater(theater)
                .build();
        return ScreenResponse.from(screenRepository.save(screen));
    }

    @Transactional
    public ScreenResponse updateScreen(Long id, CreateScreenRequest request) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", id));
        screen.setName(request.getName());
        return ScreenResponse.from(screenRepository.save(screen));
    }

    @Transactional
    public void deleteScreen(Long id) {
        if (!screenRepository.existsById(id)) {
            throw new ResourceNotFoundException("Screen", id);
        }
        screenRepository.deleteById(id);
    }

    public ScreenResponse getScreenById(Long theaterId, Long screenId) {
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("Theater", theaterId);
        }
        Screen screen = screenRepository.findByIdAndTheaterId(screenId, theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", screenId));
        return ScreenResponse.from(screen);
    }


    @Transactional
    public ScreenResponse updateScreen(Long theaterId, Long screenId, CreateScreenRequest request) {
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("Theater", theaterId);
        }
        Screen screen = screenRepository.findByIdAndTheaterId(screenId, theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", screenId));
        screen.setName(request.getName());
        return ScreenResponse.from(screenRepository.save(screen));
    }

    @Transactional
    public void deleteScreen(Long theaterId, Long screenId) {
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("Theater", theaterId);
        }
        Screen screen = screenRepository.findByIdAndTheaterId(screenId, theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", screenId));
        screenRepository.delete(screen);
    }
}