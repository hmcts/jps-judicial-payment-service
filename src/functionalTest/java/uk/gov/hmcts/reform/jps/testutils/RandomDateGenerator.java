package uk.gov.hmcts.reform.jps.testutils;

import java.time.LocalDate;
import java.util.Random;

public class RandomDateGenerator {
    private RandomDateGenerator() {
        throw new AssertionError();
    }

    public static LocalDate generateRandomDate() {
        Random random = new Random();
        LocalDate currentDate = LocalDate.now();
        int daysToSubtract = random.nextInt(3650); //up to 10 years less of current date

        return currentDate.minusDays(daysToSubtract);
    }
}
