package com.codereview.auth.dto;

import com.codereview.auth.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class UserDTO implements Serializable {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Role role;

}
