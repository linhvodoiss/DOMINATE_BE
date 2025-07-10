package com.fpt.form;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangePasswordForm {
    private String oldPassword;
    private String newPassword;

}
