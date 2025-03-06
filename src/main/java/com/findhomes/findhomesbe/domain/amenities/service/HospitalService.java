package com.findhomes.findhomesbe.domain.amenities.service;

import com.findhomes.findhomesbe.domain.amenities.domain.HospitalAmenities;
import com.findhomes.findhomesbe.domain.amenities.repository.HospitalAmenitiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalService {

    @Autowired
    private HospitalAmenitiesRepository hospitalAmenitiesRepository;
    public List<HospitalAmenities> findHospitalsByKeyword(String keyword) {
        return hospitalAmenitiesRepository.findByKeyword(keyword);
    }

}
