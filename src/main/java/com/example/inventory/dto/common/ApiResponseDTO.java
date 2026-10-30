package com.example.inventory.dto.common;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class ApiResponseDTO<T> {

    private boolean success;
    private String message;

    @JsonIgnore
    private T data;

    @JsonIgnore
    @Builder.Default
    private String dataKey = "data";

    public ApiResponseDTO(boolean success, String message, T data) {
        this(success, message, data, "data");
    }

    public ApiResponseDTO(boolean success, String message, T data, String dataKey) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.dataKey = dataKey;
    }

    @JsonAnyGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public java.util.Map<String, T> getResponseData() {
        return data == null ? java.util.Map.of() : java.util.Map.of(dataKey, data);
    }
}
