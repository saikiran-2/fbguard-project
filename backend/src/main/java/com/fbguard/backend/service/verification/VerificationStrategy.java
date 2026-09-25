package com.fbguard.backend.service.verification;

/**
 * Strategy pattern: every way of scoring "how risky is this app URL" implements
 * this interface. AppVerificationService (the Facade) collects all of them via
 * Spring dependency injection - no switch statements, no manual factory needed
 * to add a new check, just implement this interface and annotate @Component.
 */
public interface VerificationStrategy {

    /** Evaluate a single app URL and return a 0-100 risk contribution + explanation. */
    RiskSignal evaluate(String appUrl);

    /** Display name shown to admins, e.g. "Local Blacklist". */
    String getName();

    /** How much this strategy's score counts toward the final weighted total (0.0-1.0). */
    double getWeight();
}
