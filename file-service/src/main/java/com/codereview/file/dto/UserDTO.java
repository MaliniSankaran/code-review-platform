package com.codereview.file.dto;

import com.codereview.file.entity.Role;
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
