package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.condition.domain.FacilityCategory;
import com.findhomes.findhomesbe.entity.industry.Industry;
import com.findhomes.findhomesbe.repository.industry.IndustryRepository;
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

    public List<Industry> getIndustries(FacilityCategory facilityCategoryEnum, String detailName, ManConRequest.Region region) {
        // FacilityCategory의 빈 이름 가져오기
        String repositoryBeanName = facilityCategoryEnum.getRepositoryBeanName();
        // 빈 이름에 해당하는 repository 빈 객체 가져오기
        FacilityCategoryService facilityCategoryService = applicationContext.getBean(FacilityCategoryService.class);
        JpaRepository repository = facilityCategoryService.getBeanByName(repositoryBeanName, JpaRepository.class);
        List<Industry> result;
        if (repository instanceof IndustryRepository industryRepository) {
            // detailName이 "all"인 경우
            if (detailName.toLowerCase().equals("all")) {
                result = industryRepository.findIndustryWithinBoundary(region.getDistrict(), region.getCity());
                return result;
            }
            // detailName이 all이 아닌 경우 detailName을 포함하는 List<Industry> 가져오기
            result = industryRepository.findByDetailName(detailName);
//      enum을 통한 detailname 탐색 :   result = facilityCategoryEnum.getIndustryListWhenNotAll(repository, detailName);
            return result;

        }
        else {
            long startTime = System.currentTimeMillis();
            result = (List<Industry>) repository.findAll();
            long endTime = System.currentTimeMillis();
            log.info("DB 조회 및 JPA 객체 생성 시간 (공간인덱싱 미사용): " + (endTime - startTime) / 1000.0 + "초");
            return result;
        }
    }


    public <T> T getBeanByName(String beanName, Class<T> beanClass) {
        return applicationContext.getBean(beanName, beanClass);
    }
}
