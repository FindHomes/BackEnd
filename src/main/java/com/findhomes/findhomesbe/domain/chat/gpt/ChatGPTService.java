package com.findhomes.findhomesbe.domain.chat.gpt;


import com.findhomes.findhomesbe.domain.chat.CompletionRequestDto;
import org.springframework.stereotype.Service;

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
