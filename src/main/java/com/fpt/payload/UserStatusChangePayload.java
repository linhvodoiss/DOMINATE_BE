package com.fpt.payload;

import com.fpt.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusChangePayload {
    private Long userId;
    private Boolean isActive;
    private UserStatus status;
}
