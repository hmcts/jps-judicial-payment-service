package uk.gov.hmcts.reform.jps.expection;

@SuppressWarnings("serial")
public class PendingMigrationScriptException extends RuntimeException {

    public PendingMigrationScriptException(String script) {
        super("Found migration not yet applied " + script);
    }
}
