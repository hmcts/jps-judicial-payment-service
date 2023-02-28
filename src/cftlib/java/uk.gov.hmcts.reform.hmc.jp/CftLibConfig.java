package uk.gov.hmcts.reform.hmc.jp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

@Component
public class CftLibConfig extends ContainersBootstrap implements CFTLibConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CftLibConfig.class);

    private static final String ROLE_PUBLISHER = "jp-publisher";
    private static final String ROLE_SUBMITTER = "jp-submitter";
    private static final String ROLE_RECORDER = "jp-recorder";
    private static final String ROLE_CCD_IMPORT = "ccd-import";

    @Override
    public void configure(CFTLib lib) {
        createIdamUsers(lib);
    }

    private void createIdamUsers(CFTLib lib) {
        LOGGER.info("About to create Idam users......................");
        lib.createIdamUser("ccd.docker.default@hmcts.net", ROLE_CCD_IMPORT);
        lib.createIdamUser("auto.test.cnp@gmail.com", ROLE_SUBMITTER);
        lib.createIdamUser("next.hearing.date.admin@gmail.com", ROLE_PUBLISHER);
        lib.createIdamUser("master.caseworker@gmail.com", ROLE_RECORDER);
        LOGGER.info("Finished creating Idam users......................");
    }
}