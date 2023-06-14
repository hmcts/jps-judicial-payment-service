package uk.gov.hmcts.reform.jps.data.migration;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import uk.gov.hmcts.reform.jps.expection.PendingMigrationScriptException;

import java.util.stream.Stream;

import static org.flywaydb.core.api.MigrationState.BASELINE;
import static org.flywaydb.core.api.MigrationState.BASELINE_IGNORED;

public class FlywayNoOpStrategy implements FlywayMigrationStrategy {

    @Override
    public void migrate(Flyway flyway) {
        Stream.of(flyway.info().all())
            .filter(info -> !BASELINE.equals(info.getState())
                && !BASELINE_IGNORED.equals(info.getState())
                && !info.getState().isApplied())
            .findFirst()
            .ifPresent(info -> {
                throw new PendingMigrationScriptException(info.getScript());
            });
    }
}
