package uk.gov.hmcts.reform.jps.components.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.jps.model.FileInfo;
import uk.gov.hmcts.reform.jps.model.FileInfos;

import java.time.LocalDate;
import java.time.Month;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.CALLS_REAL_METHODS;

class FileInfosTest {

    public static final String BBA_3 = "BBA3";
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    private FileInfos fileInfos;

    @BeforeEach
    void setUp() {
        fileInfos = FileInfos.builder().build();
    }

    @Test
    void shouldCreateFileInfoWhenNewRequested() {
        FileInfo fileInfo = getFileInfo();

        assertThat(fileInfo.getFileName())
            .isEqualTo(String.join(
                           "_",
                           BBA_3,
                           String.valueOf(1),
                           "of"
                       )
            );
    }

    @Test
    void shouldUpdateWithFullFileNameWhenUpdateRequested() {
        LocalDate localDate = LocalDate.of(2023, Month.OCTOBER, 30);
        try (MockedStatic<LocalDate> localDateMockedStatic = Mockito.mockStatic(
            LocalDate.class,
            CALLS_REAL_METHODS
        )) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(localDate);
            IntStream.range(0, 3).forEach(counter -> getFileInfo());
            fileInfos.setFileNames();
            assertThat(fileInfos.getFileInfos())
                .map(FileInfo::getFileCreationDate,
                     FileInfo::getFileCreatedById,
                     FileInfo::getFileCreatedByName,
                     FileInfo::getRecordCount,
                     FileInfo::getFileName)
                .containsExactly(tuple(localDate, USER_ID, USER_NAME, 1, "BBA3_1_of_3_October_2023"),
                                 tuple(localDate, USER_ID, USER_NAME, 1, "BBA3_2_of_3_October_2023"),
                                 tuple(localDate, USER_ID, USER_NAME, 1, "BBA3_3_of_3_October_2023")
                );
        }
    }

    @Test
    void shouldLastFileInfoWhenPresent() {
        IntStream.range(0, 3).forEach(counter -> getFileInfo());

        FileInfo latestFileInfo = fileInfos.getLatestFileInfo();
        assertThat(latestFileInfo.getFileName())
            .isEqualTo("BBA3_3_of");
    }

    private FileInfo getFileInfo() {
        return fileInfos.createFileInfo(
            fileInfos,
            BBA_3,
            USER_ID,
            USER_NAME
        );
    }
}
