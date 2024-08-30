package com.mycompany.user.dto.response;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter

public class Response<T> {
    private String code;
    private String message;
    private T data;

    public Response(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public Response(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

}
