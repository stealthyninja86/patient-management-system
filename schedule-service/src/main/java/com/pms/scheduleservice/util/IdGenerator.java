package com.pms.scheduleservice.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IdGenerator {

    private static final Logger log = LoggerFactory.getLogger(IdGenerator.class);

    @PersistenceContext
    private EntityManager entityManager;

    public String nextId(String prefix, String columnName) {
        log.debug("Generating next id with prefix: {}", prefix);
        Number nextVal = (Number) entityManager
                .createNativeQuery("SELECT nextval('" + columnName + "')")
                .getSingleResult();
        return prefix + String.format("%03d", nextVal.intValue());
    }
}
