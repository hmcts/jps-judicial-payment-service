package uk.gov.hmcts.reform.jps.data.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import uk.gov.hmcts.reform.jps.expection.PendingMigrationScriptException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;

@ExtendWith(MockitoExtension.class)
class FlywayNoOpStrategyTest {

    @Mock
    private Flyway flyway;

    @Mock
    private MigrationInfoService infoService;

    @Mock
    private MigrationInfo info;

    private final FlywayMigrationStrategy strategy = new FlywayNoOpStrategy();

    @AfterEach
    void tearDown() {
        reset(flyway, infoService, info);
    }

    @ParameterizedTest
    @EnumSource(value = MigrationState.class, names = {"BASELINE", "BASELINE_IGNORED", "SUCCESS"})
    void shouldNotThrowExceptionWhenAllMigrationsAreApplied(MigrationState state) {
        MigrationInfo[] infos = { info, info };
        given(flyway.info()).willReturn(infoService);
        given(infoService.all()).willReturn(infos);
        given(info.getState())
            .willReturn(state);

        Throwable exception = catchThrowable(() -> strategy.migrate(flyway));
        assertThat(exception).isNull();
    }

    @Test
    void shouldThrowExceptionWhenOneMigrationIsPending() {
        MigrationInfo[] infos = { info, info };
        given(flyway.info()).willReturn(infoService);
        given(infoService.all()).willReturn(infos);
        given(info.getState()).willReturn(MigrationState.SUCCESS, MigrationState.PENDING);

        assertThatThrownBy(() -> strategy.migrate(flyway))
            .isInstanceOf(PendingMigrationScriptException.class)
            .hasMessageStartingWith("Found migration not yet applied");
    }


}
