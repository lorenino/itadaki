package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.meal.MealHistoryItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/** Retrieves paginated and date-filtered meal history for a user. */
public interface HistoryService {

    Page<MealHistoryItemDto> getHistory(Long userId, Pageable pageable);

    List<MealHistoryItemDto> getHistoryByDate(Long userId, LocalDate date);
}
