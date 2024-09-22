package com.findhomes.findhomesbe.service;


import com.findhomes.findhomesbe.DTO.CompletionRequestDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ChatGPT 서비스 인터페이스
 *
 * @author : lee
 * @fileName : ChatGPTService
 * @since : 12/29/23
 */

@Service
public interface ChatGPTService {


    Map<String, Object> prompt(CompletionRequestDto completionRequestDto, Double temperature);



}
