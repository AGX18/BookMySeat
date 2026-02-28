package io.agx.bookmyseat.controller;

import io.agx.bookmyseat.dto.request.CreateScreenRequest;
import io.agx.bookmyseat.dto.request.CreateTheaterRequest;
import io.agx.bookmyseat.dto.response.ScreenResponse;
import io.agx.bookmyseat.dto.response.TheaterResponse;
import io.agx.bookmyseat.service.ScreenService;
import io.agx.bookmyseat.service.TheaterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
public class TheaterController {
    private final TheaterService theaterService;
    private final ScreenService screenService;


    @PostMapping
    public ResponseEntity<TheaterResponse> createTheater(@Valid @RequestBody CreateTheaterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(theaterService.createTheater(request));
    }

    @GetMapping
    public ResponseEntity<List<TheaterResponse>> getAllTheaters() {
        return ResponseEntity.ok(theaterService.getAllTheaters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TheaterResponse> getTheaterById(@PathVariable Long id) {
        return ResponseEntity.ok(theaterService.getTheaterById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TheaterResponse> updateTheater(@PathVariable Long id, @Valid @RequestBody CreateTheaterRequest request) {
        return ResponseEntity.ok(theaterService.updateTheater(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheater(@PathVariable Long id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/{id}/screens")
    public ResponseEntity<ScreenResponse> addScreen(@PathVariable Long id, @Valid @RequestBody CreateScreenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(screenService.createScreen(id, request));
    }

    @GetMapping("/{id}/screens")
    public ResponseEntity<List<ScreenResponse>> getScreens(@PathVariable Long id) {
        return ResponseEntity.ok(screenService.getScreensByTheater(id));
    }

    @GetMapping("/{theaterId}/screens/{screenId}")
    public ResponseEntity<ScreenResponse> getScreenById(@PathVariable Long theaterId, @PathVariable Long screenId) {
        return ResponseEntity.ok(screenService.getScreenById(theaterId, screenId));
    }

    @PutMapping("/{theaterId}/screens/{screenId}")
    public ResponseEntity<ScreenResponse> updateScreen(@PathVariable Long theaterId, @PathVariable Long screenId, @Valid @RequestBody CreateScreenRequest request) {
        return ResponseEntity.ok(screenService.updateScreen(theaterId, screenId, request));
    }

    @DeleteMapping("/{theaterId}/screens/{screenId}")
    public ResponseEntity<Void> deleteScreen(@PathVariable Long theaterId, @PathVariable Long screenId) {
        screenService.deleteScreen(theaterId, screenId);
        return ResponseEntity.noContent().build();
    }

}
