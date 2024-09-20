package ru.practicum.category.dto;

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
public class NewCategoryDto {
    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 1, max = 50, message = "Длина названия категории должна составлять не менее 1 символа " +
            "и не более 50 символов соответственно")
    private String name;
}
