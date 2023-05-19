package uk.gov.hmcts.reform.jps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

@Component
public class CftLibConfig extends ContainersBootstrap implements CFTLibConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CftLibConfig.class);

    private static final String ROLE_PUBLISHER = "jps-publisher";
    private static final String ROLE_SUBMITTER = "jps-submitter";
    private static final String ROLE_RECORDER = "jps-recorder";
    private static final String ROLE_ADMIN = "jps-admin";
    private static final String ROLE_CCD_IMPORT = "ccd-import";

    @Override
    public void configure(CFTLib lib) {
        createIdamUsers(lib);
    }

    private void createIdamUsers(CFTLib lib) {
        LOGGER.info("About to create Idam users......................");
        lib.createIdamUser("jps-invalid-role@gmail.com", ROLE_CCD_IMPORT);
        lib.createIdamUser("jps-submitter-role@gmail.com", ROLE_SUBMITTER);
        lib.createIdamUser("jps-publisher-role@gmail.com", ROLE_PUBLISHER);
        lib.createIdamUser("jps-recorder-role@gmail.com", ROLE_RECORDER);
        lib.createIdamUser("jps-admin-role@gmail.com", ROLE_ADMIN);
        LOGGER.info("Finished creating Idam users......................");
    }
}
