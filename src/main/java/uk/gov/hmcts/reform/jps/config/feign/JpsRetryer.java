package uk.gov.hmcts.reform.jps.config.feign;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("PMD")
public class JpsRetryer extends Retryer.Default {

    int attempt;

    public JpsRetryer(int period, int maxPeriod, int maxAttempts) {
        super(period, maxPeriod, maxAttempts);
    }

    public JpsRetryer() {
        this(500, 2000, 3);
    }

    @Override
    public void continueOrPropagate(RetryableException retryableException) {
        log.warn("Feign retry attempt {} due to {} ", attempt++, retryableException.getCause().getMessage());
        super.continueOrPropagate(retryableException);
    }

    @Override
    public Retryer clone() {
        return new JpsRetryer();
    }
}
