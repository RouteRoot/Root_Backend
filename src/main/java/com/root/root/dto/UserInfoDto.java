package com.root.root.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoDto {
    private String loginId;
    private String name;
}
