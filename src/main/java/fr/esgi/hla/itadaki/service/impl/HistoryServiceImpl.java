package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.service.HistoryService;
import org.springframework.stereotype.Service;

/**
 * TODO: Implements HistoryService.
 *       - getHistory: paginated query of meals by userId, map to MealHistoryItemDto list
 *       - getHistoryByDate: filter meals by userId and specific date, map to list
 *
 *       Inject: MealRepository, MealMapper
 */
@Service
public class HistoryServiceImpl implements HistoryService {

    // TODO: Inject MealRepository
    // TODO: Inject MealMapper

    // TODO: Override getHistory(Long userId, Pageable pageable) → Page<MealHistoryItemDto>
    // TODO: Override getHistoryByDate(Long userId, LocalDate date) → List<MealHistoryItemDto>
}
