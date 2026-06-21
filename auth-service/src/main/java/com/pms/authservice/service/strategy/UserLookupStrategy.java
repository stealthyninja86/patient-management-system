package com.pms.authservice.service.strategy;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserLookupStrategy {

    boolean supports(String identifier);

    /**
     * Loads the UserDetails for the given identifier.
     * Called only if supports() returned true.
     */
    UserDetails loadUser(String identifier) throws UsernameNotFoundException;
}