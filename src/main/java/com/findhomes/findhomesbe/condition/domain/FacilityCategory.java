package com.findhomes.findhomesbe.condition.domain;

import com.findhomes.findhomesbe.entity.Industry;
import com.findhomes.findhomesbe.repository.industry.*;
import lombok.Getter;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Getter
public enum FacilityCategory {

    동물병원("animalHospitalIndustryRepository",
            (applicationContext, detailName) -> {
                AnimalHospitalIndustryRepository repository = applicationContext.getBean("animalHospitalIndustryRepository", AnimalHospitalIndustryRepository.class);
                return (List<Industry>) repository.findByDetailName(detailName);
            }
    ),
    베이커리("bakeryIndustryRepository",
            (applicationContext, detailName) -> {
                BakeryIndustryRepository repository = applicationContext.getBean("bakeryIndustryRepository", BakeryIndustryRepository.class);
                return (List<Industry>) repository.findByDetailName(detailName);
            }
    ),
    목욕탕("bathhouseIndustryRepository",
            (applicationContext, detailName) -> {
                BathhouseIndustryRepository repository = applicationContext.getBean("bathhouseIndustryRepository", BathhouseIndustryRepository.class);
                return (List<Industry>) repository.findByDetailName(detailName);
            }
    ),
    미용실("beautyIndustryRepository",
            (applicationContext, detailName) -> {
                BeautyIndustryRepository repository = applicationContext.getBean("beautyIndustryRepository", BeautyIndustryRepository.class);
                return (List<Industry>) repository.findByDetailName(detailName);
            }
    ),
    영화관("cinemaIndustryRepository",
            (applicationContext, detailName) -> {
                CinemaIndustryRepository repository = applicationContext.getBean("cinemaIndustryRepository", CinemaIndustryRepository.class);
                return (List<Industry>) repository.findByDetailName(detailName);
            }
    ),
    병원("clinicIndustryRepository",
            (applicationContext, detailName) -> {
                ClinicIndustryRepository repository = applicationContext.getBean("clinicIndustryRepository", ClinicIndustryRepository.class);
                return (List<Industry>) repository.findByDetailName(detailName);
            }
    ),
    공연장("concertHallIndustryRepository",
            (applicationContext, detailName) -> {
                ConcertHallIndustryRepository repository = applicationContext.getBean("concertHallIndustryRepository", ConcertHallIndustryRepository.class);
                return (List<Industry>) repository.findByDetailName(detailName);
            }
    ),
    피시방("gameIndustryRepository",
            (applicationContext, detailName) -> {
                GameIndustryRepository repository = applicationContext.getBean("gameIndustryRepository", GameIndustryRepository.class);
                return (List<Industry>) repository.findByDetailName(detailName);
            }
    ),
    헬스장("gymIndustryRepository",
            (applicationContext, detailName) -> {
                GymIndustryRepository repository = applicationContext.getBean("gymIndustryRepository", GymIndustryRepository.class);
                return (List<Industry>) repository.findByDetailName(detailName);
            }
    ),
    종합병원("hospitalIndustryRepository",
            (applicationContext, detailName) -> {
                HospitalIndustryRepository repository = applicationContext.getBean("hospitalIndustryRepository", HospitalIndustryRepository.class);
                return (List<Industry>) repository.findByDetailName(detailName);
            }
    ),
    노래방("karaokeIndustryRepository",
            (applicationContext, detailName) -> {
                KaraokeIndustryRepository repository = applicationContext.getBean("karaokeIndustryRepository", KaraokeIndustryRepository.class);
                return (List<Industry>) repository.findByDetailName(detailName);
            }
    ),
    약국("pharmacyIndustryRepository",
            (applicationContext, detailName) -> {
                PharmacyIndustryRepository repository = applicationContext.getBean("pharmacyIndustryRepository", PharmacyIndustryRepository.class);
                return (List<Industry>) repository.findByDetailName(detailName);
            }
    ),
    음식점("restaurantIndustryRepository",
            (applicationContext, detailName) -> {
                RestaurantIndustryRepository repository = applicationContext.getBean("restaurantIndustryRepository", RestaurantIndustryRepository.class);
                return (List<Industry>) repository.findByDetailName(detailName);
            }
    );

    private final String repositoryBeanName;
    private final BiFunction<ApplicationContext, String, List<Industry>> getIndustryListWhenNotAllFunction;

    FacilityCategory(String repositoryBeanName,
                     BiFunction<ApplicationContext, String, List<Industry>> getIndustryListWhenNotAllFunction) {
        this.repositoryBeanName = repositoryBeanName;
        this.getIndustryListWhenNotAllFunction = getIndustryListWhenNotAllFunction;
    }

    public List<Industry> getIndustryListWhenNotAll(ApplicationContext applicationContext, String detailName) {
        return getIndustryListWhenNotAllFunction.apply(applicationContext, detailName);
    }

    public static String getAllData() {
        return Arrays.stream(FacilityCategory.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}
