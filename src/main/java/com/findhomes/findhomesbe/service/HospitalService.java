package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.entity.industry.HospitalIndustry;
import com.findhomes.findhomesbe.repository.industry.HospitalIndustryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalService {

    @Autowired
    private HospitalIndustryRepository hospitalIndustryRepository;
    public List<HospitalIndustry> findHospitalsByKeyword(String keyword) {
        return hospitalIndustryRepository.findByKeyword(keyword);
    }

}
