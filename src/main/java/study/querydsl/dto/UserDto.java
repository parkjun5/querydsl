package study.querydsl.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class UserDto {

    private String name;
    private int userAge;

    public UserDto(String name, int userAge) {
        this.name = name;
        this.userAge = userAge;
    }
}
