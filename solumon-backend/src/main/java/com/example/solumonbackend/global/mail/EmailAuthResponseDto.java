package com.example.solumonbackend.global.mail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
public class EmailAuthResponseDto {

  String email;
  String code;

}
