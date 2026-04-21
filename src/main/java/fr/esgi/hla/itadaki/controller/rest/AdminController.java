package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.dto.admin.AdminMealResponseDto;
import fr.esgi.hla.itadaki.dto.admin.AdminStatsDto;
import fr.esgi.hla.itadaki.dto.admin.AdminUserResponseDto;
import fr.esgi.hla.itadaki.dto.admin.UpdateRoleRequestDto;
import fr.esgi.hla.itadaki.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller pour les operations privilegiees reservees aux ADMIN :
 * liste / suppression / promotion d'utilisateurs, moderation de repas, stats globales.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administration endpoints (ADMIN role required)")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all users (paginated)")
    public Page<AdminUserResponseDto> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return adminService.getAllUsers(pageable);
    }

    @DeleteMapping("/users/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a user (cascades to meals, photos, analyses, corrections)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/role")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Promote or demote a user (USER <-> ADMIN)")
    public AdminUserResponseDto updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequestDto request) {
        return adminService.updateUserRole(id, request.role());
    }

    @GetMapping("/meals")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all meals across all users (paginated)")
    public Page<AdminMealResponseDto> getAllMeals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        return adminService.getAllMeals(pageable);
    }

    @DeleteMapping("/meals/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a meal")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        adminService.deleteMeal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Global application statistics")
    public AdminStatsDto getStats() {
        return adminService.getStats();
    }
}
