package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.condition.domain.FacilityCategory;
import com.findhomes.findhomesbe.entity.Industry;
import com.findhomes.findhomesbe.repository.industry.IndustryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityCategoryService {

    private final ApplicationContext applicationContext;

    public List<? extends Industry> getIndustries(FacilityCategory facilityCategoryEnum, String detailName) {
        // FacilityCategory의 빈 이름 가져오기
        String repositoryBeanName = facilityCategoryEnum.getRepositoryBeanName();
        // 빈 이름에 해당하는 repository 빈 객체 가져오기
        IndustryRepository<? extends Industry> repository = applicationContext.getBean(repositoryBeanName, IndustryRepository.class);
        List<? extends Industry> result;

        long startTime = System.currentTimeMillis();

        // 모든 데이터 가져오기 (detailName이 "all"인 경우)
        if (detailName.toLowerCase().equals("all")) {
            result = repository.findAll();
        } else {
            // detailName이 포함된 데이터 가져오기
            result = repository.findByDetailName(detailName);
        }

        long endTime = System.currentTimeMillis();
        log.info("DB 조회 및 JPA 객체 생성 시간: " + (endTime - startTime) / 1000.0 + "초");

        return result;
    }

    // Bean 이름과 클래스를 통해 Bean 객체를 가져오는 유틸리티 메서드
    public <T> T getBeanByName(String beanName, Class<T> beanClass) {
        return applicationContext.getBean(beanName, beanClass);
    }
}
