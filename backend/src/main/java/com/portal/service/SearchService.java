package com.portal.service;

import com.portal.dto.SearchDto;
import com.portal.entity.Achievement;
import com.portal.repository.AchievementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SearchService {

    private final AchievementRepository achievementRepository;

    public SearchService(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    // search approved achievements with paging (size 10, newest eventDate first)
    @Transactional(readOnly = true)
    public Page<Achievement> search(SearchDto dto, int page) {
        if (page < 0) {
            page = 0;
        }

        String keyword = cleanString(dto.getKeyword());
        String branch = cleanString(dto.getBranch());
        String level = cleanString(dto.getLevel());

        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "eventDate", "id"));

        return achievementRepository.searchApprovedAchievements(
                keyword,
                branch,
                dto.getYear(),
                dto.getCategoryId(),
                level,
                dto.getFromDate(),
                dto.getToDate(),
                pageable
        );
    }

    // get export list for CSV (capped at 5000 rows, newest eventDate first)
    @Transactional(readOnly = true)
    public List<Achievement> getExportList(SearchDto dto) {
        String keyword = cleanString(dto.getKeyword());
        String branch = cleanString(dto.getBranch());
        String level = cleanString(dto.getLevel());

        Pageable pageable = PageRequest.of(0, 5000, Sort.by(Sort.Direction.DESC, "eventDate", "id"));

        return achievementRepository.exportApprovedAchievements(
                keyword,
                branch,
                dto.getYear(),
                dto.getCategoryId(),
                level,
                dto.getFromDate(),
                dto.getToDate(),
                pageable
        );
    }

    // helper to convert empty or whitespace-only string to null
    private String cleanString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        return input.trim();
    }
}
