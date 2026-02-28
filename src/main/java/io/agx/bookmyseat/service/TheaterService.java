package io.agx.bookmyseat.service;

import io.agx.bookmyseat.dto.request.CreateTheaterRequest;
import io.agx.bookmyseat.dto.response.ScreenResponse;
import io.agx.bookmyseat.dto.response.TheaterResponse;
import io.agx.bookmyseat.entity.Screen;
import io.agx.bookmyseat.entity.Theater;
import io.agx.bookmyseat.exception.DuplicateResourceException;
import io.agx.bookmyseat.exception.ResourceNotFoundException;
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

    public List<TheaterResponse> getAllTheaters() {
        return theaterRepository.findAll()
                .stream()
                .map(TheaterResponse::from)
                .toList();
    }

    public TheaterResponse getTheaterById(Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", id));
        return TheaterResponse.from(theater);
    }

    public List<TheaterResponse> getTheatersByCity(String city) {
        return theaterRepository.findByCityIgnoreCase(city)
                .stream()
                .map(TheaterResponse::from)
                .toList();
    }


    @Transactional
    public TheaterResponse createTheater(CreateTheaterRequest request) {
        if (theaterRepository.existsByNameAndBranch(request.getName(), request.getBranch())) {
            throw new DuplicateResourceException("Theater branch already exists");
        }
        Theater theater = Theater.builder()
                .name(request.getName())
                .branch(request.getBranch())
                .city(request.getCity())
                .address(request.getAddress())
                .build();
        return TheaterResponse.from(theaterRepository.save(theater));
    }

    @Transactional
    public TheaterResponse updateTheater(Long id, CreateTheaterRequest request) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater", id));
        theater.setName(request.getName());
        theater.setBranch(request.getBranch());
        theater.setCity(request.getCity());
        theater.setAddress(request.getAddress());
        return TheaterResponse.from(theaterRepository.save(theater));
    }

    @Transactional
    public void deleteTheater(Long id) {
        if (!theaterRepository.existsById(id)) {
            throw new IllegalArgumentException("Theater not found: " + id);
        }
        theaterRepository.deleteById(id);
    }
}