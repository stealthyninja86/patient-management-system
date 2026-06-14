package com.pms.authservice.service.strategy;

import com.pms.authservice.model.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RegistrationStrategyProvider {
    private final Map<Role, RegistrationStrategy<?, ?>> strategyMap;

    public RegistrationStrategyProvider(List<RegistrationStrategy<?, ?>> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        RegistrationStrategy::supportedRole,
                        Function.identity()
                ));
    }

    @SuppressWarnings("unchecked")
    public <Req, Res> RegistrationStrategy<Req, Res> getStrategy(Role role) {
        RegistrationStrategy<?, ?> strategy = strategyMap.get(role);
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy not found for role: " + role);
        }
        return (RegistrationStrategy<Req, Res>) strategy;
    }
}
