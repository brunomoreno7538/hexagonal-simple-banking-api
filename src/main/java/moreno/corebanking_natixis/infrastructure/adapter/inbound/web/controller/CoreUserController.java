package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.in.CreateCoreUserUseCase;
import moreno.corebanking_natixis.application.port.in.DeleteCoreUserUseCase;
import moreno.corebanking_natixis.application.port.in.GetCoreUserUseCase;
import moreno.corebanking_natixis.application.port.in.UpdateCoreUserUseCase;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.CoreUser;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper.CoreUserWebMapper;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.CreateCoreUserRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.UpdateCoreUserRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.CoreUserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/core-users")
@RequiredArgsConstructor
public class CoreUserController {

    private final CreateCoreUserUseCase createCoreUserUseCase;
    private final GetCoreUserUseCase getCoreUserUseCase;
    private final UpdateCoreUserUseCase updateCoreUserUseCase;
    private final DeleteCoreUserUseCase deleteCoreUserUseCase;
    private final CoreUserWebMapper coreUserWebMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CoreUserResponse> createCoreUser(@Valid @RequestBody CreateCoreUserRequest request) {
        CoreUser coreUserToCreate = coreUserWebMapper.toDomain(request);
        CoreUser createdCoreUser = createCoreUserUseCase.create(coreUserToCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(coreUserWebMapper.toResponse(createdCoreUser));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CoreUserResponse> getCoreUserById(@PathVariable UUID userId) {
        return getCoreUserUseCase.findById(userId)
                .map(coreUserWebMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("CoreUser not found with id: " + userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CoreUserResponse>> getAllCoreUsers() {
        List<CoreUserResponse> responses = getCoreUserUseCase.findAll().stream()
                .map(coreUserWebMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CoreUserResponse> updateCoreUser(@PathVariable UUID userId, @Valid @RequestBody UpdateCoreUserRequest request) {
        CoreUser coreUserToUpdate = coreUserWebMapper.toDomain(request);
        CoreUser updatedCoreUser = updateCoreUserUseCase.update(userId, coreUserToUpdate);
        return ResponseEntity.ok(coreUserWebMapper.toResponse(updatedCoreUser));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCoreUser(@PathVariable UUID userId) {
        deleteCoreUserUseCase.deleteById(userId);
        return ResponseEntity.noContent().build();
    }
}