package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.entity.Hospital;
import com.findhomes.findhomesbe.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;
    public List<Hospital> findHospitalsByKeyword(String keyword) {
        return hospitalRepository.findByKeyword(keyword);
    }
    public List<double[]> getAllHospitalLocations(String keyword) {
        List<Hospital> hospitals = hospitalRepository.findByKeyword(keyword);

        System.out.println(hospitals);
        List<double[]> locations = new ArrayList<>();

        for (Hospital hospital : hospitals) {
            double[] location = new double[2];
            location[0] = hospital.getX();
            location[1] = hospital.getY();
            locations.add(location);
        }

        return locations;
    }
}
