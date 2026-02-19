package com.codereview.platform.dto;

import com.codereview.platform.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Role role;

}
