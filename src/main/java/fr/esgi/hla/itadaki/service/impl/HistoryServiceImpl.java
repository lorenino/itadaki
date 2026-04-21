package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.dto.meal.MealHistoryItemDto;
import fr.esgi.hla.itadaki.mapper.MealMapper;
import fr.esgi.hla.itadaki.repository.MealRepository;
import fr.esgi.hla.itadaki.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/** Retrieves paginated and date-filtered meal history for users. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryServiceImpl implements HistoryService {

    private final MealRepository mealRepository;
    private final MealMapper mealMapper;

    @Override
    public Page<MealHistoryItemDto> getHistory(Long userId, Pageable pageable) {
        return mealRepository.findAllByUserId(userId, pageable)
                .map(mealMapper::toHistoryItemDto);
    }

    @Override
    public List<MealHistoryItemDto> getHistoryByDate(Long userId, LocalDate date) {
        return mealMapper.toHistoryItemDto(
                mealRepository.findByUserIdAndDate(userId, date)
        );
    }
}
