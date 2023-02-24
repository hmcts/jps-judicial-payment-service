package uk.gov.hmcts.reform.hmc.jp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class ContainersBootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainersBootstrap.class);
    private static final String IMAGE = "hmctspublic.azurecr.io/ccd/test-stubs-service:latest";
    protected static final GenericContainer<?> TEST_STUBS_CONTAINER = new GenericContainer<>(IMAGE)
        .withExposedPorts(5555)
        .withLogConsumer(new Slf4jLogConsumer(LOGGER))
        .waitingFor(Wait.forListeningPort());

    @PostConstruct
    public void init() {
        LOGGER.info("==================== Starting TEST_STUBS_CONTAINER ====================");
        if (!TEST_STUBS_CONTAINER.isRunning()) {
            TEST_STUBS_CONTAINER.start();
        }

        createEnvFile();
    }

    private void createEnvFile() {
        final String content = String.format(" # This file is only required when running bootWithCcd "
            + "# WARNING: overriding test stub URL: for use ONLY with `bootWithCcd` "
            + " TEST_STUB_SERVICE_BASE_URL=http://localhost:%d",
            TEST_STUBS_CONTAINER.getFirstMappedPort());
        final String filePath = String.format(".%s%s", File.separator, ".env.test.stub.service.env");
        final Path path = Paths.get(filePath);

        try {
            LOGGER.info("Writing environment variable 'TEST_STUB_SERVICE_BASE_URL' to ==> {}", filePath);
            Files.writeString(path, content);
        } catch (IOException e) {
            LOGGER.error("Something went wrong:::", e);
        }
    }

    @PreDestroy
    public void cleanUp() {
        LOGGER.info("==================== Stopping TEST_STUBS_CONTAINER ====================");
        if (TEST_STUBS_CONTAINER.isRunning()) {
            TEST_STUBS_CONTAINER.stop();
        }
    }
}