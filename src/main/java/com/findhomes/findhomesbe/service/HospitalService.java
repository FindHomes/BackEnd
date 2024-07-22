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

}
