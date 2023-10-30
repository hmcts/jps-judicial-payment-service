package uk.gov.hmcts.reform.jps.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.LinkedList;

import static java.lang.String.format;

@Builder
@Data
public class FileInfos {
    public static final String DRAFT_FILE_NAME_FORMAT = "%s_%d_of";
    public static final String FILE_NAME_FORMAT = "%s_%d_%s_%d";

    private final LinkedList<FileInfo> fileInfos = new LinkedList<>();

    public void addFileInfo(FileInfo fileInfo) {
        fileInfos.add(fileInfo);
    }

    public int getFileCount() {
        return fileInfos.size();
    }

    public FileInfo getLatestFileInfo() {
        return fileInfos.getLast();
    }

    public void setFileNames() {
        LocalDate now = LocalDate.now();
        int totalFiles = getFileCount();
        char[] charArray = now.getMonth().toString().toLowerCase().toCharArray();
        charArray[0] = Character.toUpperCase(charArray[0]);
        String month = new String(charArray);
        fileInfos.forEach(fileInfo ->
                              fileInfo.setFileName(
                                  format(
                                      FILE_NAME_FORMAT,
                                      fileInfo.getFileName(),
                                      totalFiles,
                                      month,
                                      now.getYear()
                                  )
                              )
        );
    }

    public FileInfo createFileInfo(FileInfos fileInfos,
                                   String serviceName,
                                   String createdById,
                                   String createdByName) {
        FileInfo fileInfo = FileInfo.builder()
            .fileName(format(
                DRAFT_FILE_NAME_FORMAT,
                serviceName,
                fileInfos.getFileCount() + 1
            ))
            .fileCreatedById(createdById)
            .fileCreatedByName(createdByName)
            .recordCount(1)
            .build();

        fileInfos.addFileInfo(fileInfo);
        return fileInfo;
    }
}
