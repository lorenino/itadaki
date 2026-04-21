package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.business.Meal;
import fr.esgi.hla.itadaki.business.MealAnalysis;
import fr.esgi.hla.itadaki.business.User;
import fr.esgi.hla.itadaki.business.enums.UserRole;
import fr.esgi.hla.itadaki.dto.admin.AdminMealResponseDto;
import fr.esgi.hla.itadaki.dto.admin.AdminStatsDto;
import fr.esgi.hla.itadaki.dto.admin.AdminUserResponseDto;
import fr.esgi.hla.itadaki.exception.ConflictException;
import fr.esgi.hla.itadaki.exception.ResourceNotFoundException;
import fr.esgi.hla.itadaki.repository.MealAnalysisRepository;
import fr.esgi.hla.itadaki.repository.MealRepository;
import fr.esgi.hla.itadaki.repository.UserRepository;
import fr.esgi.hla.itadaki.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Admin service; readOnly by default to allow LAZY loads (meals.size, analysis, user on Meal). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final MealRepository mealRepository;
    private final MealAnalysisRepository mealAnalysisRepository;

    @Override
    public Page<AdminUserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserDto);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if (user.getRole() == UserRole.ADMIN && userRepository.countByRole(UserRole.ADMIN) <= 1) {
            throw new ConflictException("Cannot delete the last admin user");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public AdminUserResponseDto updateUserRole(Long id, UserRole role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if (user.getRole() == UserRole.ADMIN && role != UserRole.ADMIN
                && userRepository.countByRole(UserRole.ADMIN) <= 1) {
            throw new ConflictException("Cannot demote the last admin user");
        }
        user.setRole(role);
        return toUserDto(userRepository.save(user));
    }

    @Override
    public Page<AdminMealResponseDto> getAllMeals(Pageable pageable) {
        return mealRepository.findAll(pageable).map(this::toMealDto);
    }

    @Override
    @Transactional
    public void deleteMeal(Long id) {
        if (!mealRepository.existsById(id)) {
            throw new ResourceNotFoundException("Meal not found with id: " + id);
        }
        mealRepository.deleteById(id);
    }

    @Override
    public AdminStatsDto getStats() {
        long totalUsers = userRepository.count();
        long totalMeals = mealRepository.count();
        long totalAnalyses = mealAnalysisRepository.count();
        Double avg = mealAnalysisRepository.averageEstimatedCalories();
        return new AdminStatsDto(totalUsers, totalMeals, totalAnalyses, avg);
    }

    private AdminUserResponseDto toUserDto(User user) {
        long mealCount = mealRepository.countByUserId(user.getId());
        return new AdminUserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                mealCount
        );
    }

    private AdminMealResponseDto toMealDto(Meal meal) {
        MealAnalysis analysis = meal.getAnalysis();
        Double calories = analysis != null ? analysis.getEstimatedTotalCalories() : null;
        String userName = meal.getUser() != null ? meal.getUser().getUsername() : null;
        return new AdminMealResponseDto(
                meal.getId(),
                meal.getStatus(),
                meal.getMealType(),
                meal.getUploadedAt(),
                userName,
                calories
        );
    }
}
