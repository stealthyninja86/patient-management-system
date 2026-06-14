package com.pms.authservice.service.saga;

import com.pms.authservice.exception.SagaCompensationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.LinkedList;

@Component
@Scope("prototype")
public class RegistrationSaga {

    private static final Logger log = LoggerFactory.getLogger(RegistrationSaga.class);
    private final Deque<Runnable> compensations = new LinkedList<>();
    private boolean completed  = false;

    public void addCompensation(Runnable compensation) {
        compensations.push(compensation);
    }

    public void compensate() {
        log.warn("compensating registration - executing {} rollback steps", compensations.size());
        Exception lastException = null;
        while(!compensations.isEmpty()) {
            try{
                compensations.poll().run();
            }
            catch (Exception e){
                log.error("Error while compensating registration", e);
                lastException = e;
            }
        }
        if(lastException != null) {
            throw  new SagaCompensationException("some compensation steps failed", lastException);
        }
    }

    public void complete() {
        this.completed = true;
        compensations.clear();
    }

    public boolean isCompleted() {
        return  completed;
    }
}
