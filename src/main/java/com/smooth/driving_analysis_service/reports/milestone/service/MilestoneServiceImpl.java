package com.smooth.driving_analysis_service.reports.milestone.service;

import com.smooth.driving_analysis_service.reports.milestone.dto.response.MilestoneReportResponse;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneReportRepository repo;

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneReportResponse> listByUser(long userId) {
        return repo.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(MilestoneReportResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneReportResponse getStamp(long id) {
        var r = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 마일스톤을 찾을 수 없습니다. id=" + id));
        return MilestoneReportResponse.builder()
                .id(r.getId())
                .numberOfDriving(r.getNumberOfDriving())
                .build();
    }

    @Override
    public void updateRead(long id, boolean read) {
        var r = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 마일스톤을 찾을 수 없습니다. id=" + id));
        r.setRead(read);
        repo.save(r);
    }
}
