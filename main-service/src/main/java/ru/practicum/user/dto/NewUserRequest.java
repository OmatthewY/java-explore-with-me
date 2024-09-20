package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {
    @Email(message = "Электронная почта должен быть действительной")
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Size(min = 6, max = 254, message = "Длина электронной почты пользователя должна составлять не менее 6 " +
            "и не более 254 символов")
    private String email;

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 250, message = "Длина имени пользователя должна составлять не менее 2 и не более 250 символов")
    private String name;
}
