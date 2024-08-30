package com.mycompany.user.dto;

import com.mycompany.user.entity.UserCsvRepresentation;
import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserCsvRepresentationDto {

    @CsvBindByName(column = "email")
    private String email;

    @CsvBindByName(column = "firstname")
    private String fname;

    @CsvBindByName(column = "lastname")
    private String lname;

    @CsvBindByName(column = "password")
    private String password;

    @CsvBindByName(column = "reason")
    private String reason;

    public static UserCsvRepresentationDto fromEntity(UserCsvRepresentation entity) {
        return UserCsvRepresentationDto.builder()
                .email(entity.getEmail())
                .fname(entity.getFname())
                .lname(entity.getLname())
                .password(entity.getPassword())
                .build();
    }
}
