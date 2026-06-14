package com.pms.authservice.service.strategy;

import com.pms.authservice.model.Role;

public interface RegistrationStrategy<Req, Res> {
    Res register(Req request, String encodedPassword);
    Role supportedRole();
}