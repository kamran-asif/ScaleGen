package com.genai.inferenceworker.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreaker.class);

    public enum State { CLOSED, OPEN, HALF_OPEN }

    private static class CircuitState {
        State state = State.CLOSED;
        AtomicInteger consecutiveFailures = new AtomicInteger(0);
        long lastStateChangeTime = System.currentTimeMillis();
    }

    private final Map<String, CircuitState> circuits = new ConcurrentHashMap<>();
    private static final int FAILURE_THRESHOLD = 3;
    private static final long COOL_OFF_PERIOD_MS = 15000;

    public boolean allowExecution(String modelId) {
        CircuitState cs = circuits.computeIfAbsent(modelId, k -> new CircuitState());
        synchronized (cs) {
            if (cs.state == State.OPEN) {
                if (System.currentTimeMillis() - cs.lastStateChangeTime > COOL_OFF_PERIOD_MS) {
                    cs.state = State.HALF_OPEN;
                    cs.lastStateChangeTime = System.currentTimeMillis();
                    log.info("[CircuitBreaker] Transitioned model {} to HALF_OPEN", modelId);
                    return true;
                }
                log.warn("[CircuitBreaker] Circuit OPEN for model {}. Tripping to fallback route.", modelId);
                return false;
            }
            return true;
        }
    }

    public void recordSuccess(String modelId) {
        CircuitState cs = circuits.computeIfAbsent(modelId, k -> new CircuitState());
        synchronized (cs) {
            cs.consecutiveFailures.set(0);
            if (cs.state == State.HALF_OPEN) {
                cs.state = State.CLOSED;
                cs.lastStateChangeTime = System.currentTimeMillis();
                log.info("[CircuitBreaker] Model {} recovered. State set to CLOSED.", modelId);
            }
        }
    }

    public void recordFailure(String modelId) {
        CircuitState cs = circuits.computeIfAbsent(modelId, k -> new CircuitState());
        synchronized (cs) {
            int failures = cs.consecutiveFailures.incrementAndGet();
            if (failures >= FAILURE_THRESHOLD && cs.state != State.OPEN) {
                cs.state = State.OPEN;
                cs.lastStateChangeTime = System.currentTimeMillis();
                log.error("[CircuitBreaker] TRIP ALERT! Model {} exceeded {} consecutive failures. State set to OPEN.", modelId, FAILURE_THRESHOLD);
            }
        }
    }

    public String getState(String modelId) {
        CircuitState cs = circuits.get(modelId);
        return cs != null ? cs.state.name() : State.CLOSED.name();
    }
}
