package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.condition.domain.FacilityCategory;
import com.findhomes.findhomesbe.entity.Industry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityCategoryService {

    private final ApplicationContext applicationContext;

    public List<Industry> getIndustries(FacilityCategory facilityCategoryEnum, String detailName) {
        // FacilityCategory의 빈 이름 가져오기
        String repositoryBeanName = facilityCategoryEnum.getRepositoryBeanName();
        // 빈 이름에 해당하는 repository 빈 객체 가져오기
        FacilityCategoryService facilityCategoryService = applicationContext.getBean(FacilityCategoryService.class);
        JpaRepository repository = facilityCategoryService.getBeanByName(repositoryBeanName, JpaRepository.class);
        List<Industry> result;
        // all인 경우 List<Industry> 가져오기

        if (detailName.toLowerCase().equals("all")) {
            long startTime = System.currentTimeMillis();
            result = (List<Industry>) repository.findAll();
            long endTime = System.currentTimeMillis();
            log.info("DB 조회 및 JPA 객체 생성 시간: " + (endTime - startTime) / 1000.0 + "초");

            return result;
        }
        // all이 아닌 경우 detailName을 포함하는 List<Industry> 가져오기
        long startTime = System.currentTimeMillis();
        result = facilityCategoryEnum.getIndustryListWhenNotAll(repository, detailName);
        long endTime = System.currentTimeMillis();
        log.info("DB 조회 및 JPA 객체 생성 시간: " + (endTime - startTime) / 1000.0 + "초");
        return result;
    }

    public <T> T getBeanByName(String beanName, Class<T> beanClass) {
        return applicationContext.getBean(beanName, beanClass);
    }
}
