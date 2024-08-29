package com.findhomes.findhomesbe.DTO;

import com.findhomes.findhomesbe.entity.House;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
public class ManConResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private String result;


}
