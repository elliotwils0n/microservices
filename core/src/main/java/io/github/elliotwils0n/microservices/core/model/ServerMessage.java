package io.github.elliotwils0n.microservices.core.model;

import io.github.elliotwils0n.microservices.core.utils.StringFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ServerMessage {

    private Integer code;
    private String status;
    private String message;

    @SneakyThrows
    @Override
    public String toString() {
        return StringFormatter.getObjectAsJson(this);
    }

}
