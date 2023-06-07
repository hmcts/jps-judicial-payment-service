package uk.gov.hmcts.reform.jps.testutils;

import uk.gov.hmcts.reform.jps.config.PropertiesReader;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DatabaseManager {

    PropertiesReader propertiesReader = new PropertiesReader("src/functionalTest/resources/test-config.properties");
    String dbUrl = propertiesReader.getProperty("datasource.url");


    String dbUsername = propertiesReader.getProperty("datasource.username");
    String dbPassword = propertiesReader.getProperty("datasource.password");

    public void insertSittingRecord(int sittingRecordId, Date sittingDate, String statusId, String regionId,
                                    String epimsId, String hmctsServiceId, String personalCode, int contractTypeId,
                                    String judgeRoleTypeId, boolean am, boolean pm, Timestamp createdDateTime,
                                    String createdByUserId) {

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String query = "INSERT INTO public.sitting_record (sitting_record_id, sitting_date, status_id, region_id, "
                + "epims_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm, "
                + "created_date_time, created_by_user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, sittingRecordId);
            statement.setDate(2, sittingDate);
            statement.setString(3, statusId);
            statement.setString(4, regionId);
            statement.setString(5, epimsId);
            statement.setString(6, hmctsServiceId);
            statement.setString(7, personalCode);
            statement.setInt(8, contractTypeId);
            statement.setString(9, judgeRoleTypeId);
            statement.setBoolean(10, am);
            statement.setBoolean(11, pm);
            statement.setTimestamp(12, createdDateTime);
            statement.setString(13, createdByUserId);
            statement.executeUpdate();

            System.out.println("Data inserted successfully.");
        } catch (SQLException e) {
            System.out.println("this is the URL: " + dbUrl);
            System.out.println("this is the username: " + dbUsername);
            System.out.println("this is the password: " + dbPassword);
            System.out.println("Error inserting data: " + e.getMessage());
        }
    }

    public void deleteData(int sittingRecordId) {
        try {
            Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            String query = "DELETE FROM public.sitting_record WHERE sitting_record_id = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, sittingRecordId);
            statement.executeUpdate();

            System.out.println("Data deleted successfully.");
        } catch (SQLException e) {
            System.out.println("Error deleting data: " + e.getMessage());
        }
    }
}
