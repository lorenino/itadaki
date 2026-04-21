package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.business.enums.UserRole;
import fr.esgi.hla.itadaki.dto.admin.AdminMealResponseDto;
import fr.esgi.hla.itadaki.dto.admin.AdminStatsDto;
import fr.esgi.hla.itadaki.dto.admin.AdminUserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service admin : operations privilegiees (gestion users, gestion repas, stats globales).
 * Les controllers exposant ces methodes doivent porter @PreAuthorize("hasRole('ADMIN')").
 */
public interface AdminService {

    Page<AdminUserResponseDto> getAllUsers(Pageable pageable);

    void deleteUser(Long id);

    AdminUserResponseDto updateUserRole(Long id, UserRole role);

    Page<AdminMealResponseDto> getAllMeals(Pageable pageable);

    void deleteMeal(Long id);

    AdminStatsDto getStats();
}
