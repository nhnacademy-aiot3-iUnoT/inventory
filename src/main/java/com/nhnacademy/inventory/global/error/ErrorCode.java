package com.nhnacademy.inventory.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


public interface ErrorCode {

    HttpStatus getStatus();
    String getCode();
    String getMessage();
    String getName();





}
