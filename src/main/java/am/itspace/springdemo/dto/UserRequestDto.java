package am.itspace.springdemo.dto;

import am.itspace.springdemo.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequestDto {

    private int id;
    @NotBlank(message = "name is Required")
    private String name;
    @NotBlank(message = "surname is Required")
    private String surname;
    @Email(message = "Email is not valid")
    private String username;
    @Size(min = 6, message = "password length should be at least 6 symbol")
    private String password;
    @Size(min = 6,  message = "confirmPassword length should be at least 6 symbol")
    private String confirmPassword;
    private Role role = Role.USER;

}
