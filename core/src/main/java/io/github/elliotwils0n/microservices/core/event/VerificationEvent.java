package io.github.elliotwils0n.microservices.core.event;

import io.github.elliotwils0n.microservices.core.enumeration.VerificationEventType;
import io.github.elliotwils0n.microservices.core.utils.StringFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class VerificationEvent {

    private VerificationEventType type;
    private UUID userId;

    @SneakyThrows
    @Override
    public String toString() {
        return StringFormatter.getObjectAsJson(this);
    }

}
