package com.hr.employee.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponseDto(
    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("refresh_token")
    String refreshToken
) { }
