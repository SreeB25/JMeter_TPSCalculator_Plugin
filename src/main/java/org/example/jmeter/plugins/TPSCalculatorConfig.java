package org.example.jmeter.plugins;

import java.io.Serializable;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core logic of the TPS & Think Time calculator.
 * It runs once when the test starts and publishes
 * the calculated number of users and think time
 * into JMeter properties/variables.
 */
public class TPSCalculatorConfig extends ConfigTestElement implements TestStateListener, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(TPSCalculatorConfig.class);

    private static final String TARGET_TPS = "TPSCalculator.targetTPS";
    private static final String EXPECTED_RESP_MS = "TPSCalculator.expectedResponseMs";
    private static final String DESIRED_THINK_MS = "TPSCalculator.desiredThinkMs";
    private static final String FIXED_USERS = "TPSCalculator.fixedUsers";
    private static final String TRANSACTIONS_PER_ITER = "TPSCalculator.transactionsPerIteration";

    public static final String OUT_USERS_PROP = "calculated.users";
    public static final String OUT_THINK_PROP = "calculated.think";

    public TPSCalculatorConfig() {
        setProperty(TARGET_TPS, "10");
        setProperty(EXPECTED_RESP_MS, "200");
        setProperty(TRANSACTIONS_PER_ITER, "1");
    }

    public void setTargetTPS(String tps) { setProperty(TARGET_TPS, tps); }
    public String getTargetTPS() { return getPropertyAsString(TARGET_TPS, "10"); }
    public void setExpectedResponseMs(String ms) { setProperty(EXPECTED_RESP_MS, ms); }
    public String getExpectedResponseMs() { return getPropertyAsString(EXPECTED_RESP_MS, "200"); }
    public void setDesiredThinkMs(String ms) { setProperty(DESIRED_THINK_MS, ms); }
    public String getDesiredThinkMs() { return getPropertyAsString(DESIRED_THINK_MS, ""); }
    public void setFixedUsers(String threads) { setProperty(FIXED_USERS, threads); }
    public String getFixedUsers() { return getPropertyAsString(FIXED_USERS, ""); }
    public void setTransactionsPerIteration(String t) { setProperty(TRANSACTIONS_PER_ITER, t); }
    public String getTransactionsPerIteration() { return getPropertyAsString(TRANSACTIONS_PER_ITER, "1"); }

    @Override
    public void testStarted() { computeAndPublish(); }
    @Override
    public void testStarted(String host) { computeAndPublish(); }
    @Override
    public void testEnded() {}
    @Override
    public void testEnded(String host) {}

    private void computeAndPublish() {
        double targetTps = parseDoubleSafe(getTargetTPS(), 0.0);
        double respMs = parseDoubleSafe(getExpectedResponseMs(), 0.0);
        double respSec = respMs / 1000.0;
        int txPerIter = (int) parseDoubleSafe(getTransactionsPerIteration(), 1.0);

        String desiredThinkMsStr = getDesiredThinkMs().trim();
        String fixedUsersStr = getFixedUsers().trim();

        Double desiredThinkMs = desiredThinkMsStr.isEmpty() ? null : parseDoubleSafe(desiredThinkMsStr, 0.0);
        Integer fixedUsers = fixedUsersStr.isEmpty() ? null : (int) parseDoubleSafe(fixedUsersStr, 0.0);

        try {
            if (targetTps <= 0) {
                log.warn("Target TPS <=0; skipping calculation");
                return;
            }

            if (desiredThinkMs != null && fixedUsers == null) {
                double Z = desiredThinkMs / 1000.0;
                double users = (targetTps * (respSec + Z)) / txPerIter;
                publish((int) Math.ceil(users), desiredThinkMs);
            } else if (fixedUsers != null && desiredThinkMs == null) {
                double N = fixedUsers;
                double Z = ((N * txPerIter) / targetTps) - respSec;
                if (Z < 0) Z = 0;
                publish(fixedUsers, Z * 1000.0);
            } else if (desiredThinkMs != null && fixedUsers != null) {
                double Z = desiredThinkMs / 1000.0;
                double N = fixedUsers;
                double achievableTps = (N * txPerIter) / (respSec + Z);
                log.info("Achievable TPS = {} for N={}, Zms={}, S={}ms", achievableTps, N, desiredThinkMs, respMs);
                publish(fixedUsers, desiredThinkMs);
            } else {
                double users = (targetTps * respSec) / txPerIter;
                publish((int) Math.ceil(users), 0.0);
            }
        } catch (Exception e) {
            log.error("Error in TPSCalculatorConfig", e);
        }
    }

    private void publish(int users, double thinkMs) {
        try {
            JMeterVariables vars = JMeterContextService.getContext().getVariables();
            if (vars != null) {
                vars.put(OUT_USERS_PROP, String.valueOf(users));
                vars.put(OUT_THINK_PROP, String.valueOf((long) Math.ceil(thinkMs)));
            }
        } catch (Exception ignored) {}
        System.setProperty(OUT_USERS_PROP, String.valueOf(users));
        System.setProperty(OUT_THINK_PROP, String.valueOf((long) Math.ceil(thinkMs)));
    }

    private double parseDoubleSafe(String s, double defaultVal) {
        if (s == null || s.trim().isEmpty()) return defaultVal;
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return defaultVal; }
    }
}
