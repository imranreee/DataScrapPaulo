package utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GoogleSheetServiceCopy {
    private static GoogleSheetServiceCopy googleSheetServiceCopy;
    private GoogleCredentials googleCredentials;

    private final String SHEET_ID = "1M-krxzQIun6fngp9O0-KERr2_ORdksz87uFar4FjhsM";
    private String SHEET_NAME = "data";
    private List<List<Object>> values;
    private HttpRequestInitializer httpRequestInitializer;
    private Spreadsheets spreadsheets;

    /**
     * <p>Private constructor</p>
     */
    private GoogleSheetServiceCopy() {
        createCredentialCopy();
    }

    public static GoogleSheetServiceCopy createInstance() {
        if (googleSheetServiceCopy == null) {
            googleSheetServiceCopy = new GoogleSheetServiceCopy();
        }
        return googleSheetServiceCopy;
    }

    public void createCredentialCopy() {
        try {
            googleCredentials = GoogleCredentials.fromStream(getClass().getResourceAsStream("/key.json"))
                    .createScoped(Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE));
            httpRequestInitializer = new HttpCredentialsAdapter(googleCredentials);
            getSpreadsheets();
        } catch (IOException e) {
            System.out.println("Authorization failed");
            e.printStackTrace();
        }
    }

    boolean isAuthorized() {
        return googleCredentials != null && httpRequestInitializer != null;
    }

    public void getSpreadsheets() {
        try {
            spreadsheets = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(), httpRequestInitializer).setApplicationName("TestApp_2").build()
                    .spreadsheets();
            getValues();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    private void getValues() {
        try {
            values = spreadsheets.values().get(SHEET_ID, SHEET_NAME)
                    .execute().getValues();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readValuesByRowAndColumn(int rowNumber, int columnNumber) {
        if (!values.isEmpty() && rowNumber > 0 && columnNumber > 0) {
            rowNumber = rowNumber - 1;
            columnNumber = columnNumber - 1;
            return values.get(rowNumber).get(columnNumber).toString();
        }
        return null;
    }

    public void writeDataGoogleSheets(List<Object> data, int rowNumber, String columnName) throws IOException {
        writeSheet(data, SHEET_NAME + columnName + rowNumber);
    }

    private void writeSheet(List<Object> inputData, String sheetAndRange) throws IOException {
        List<List<Object>> values = Arrays.asList(inputData);
        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse result = spreadsheets.values().update(SHEET_ID, sheetAndRange, body).setValueInputOption("USER_ENTERED").execute();
    }

    // New method to copy the sheet and rename it
    public void copySheetWithTimestamp() {
        try {
            // Generate new sheet name with current date and time
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
            String newSheetName = "data_" + timestamp;

            // Get the sheet ID of the original sheet
            Spreadsheet spreadsheet = spreadsheets.get(SHEET_ID).execute();
            List<Sheet> sheets = spreadsheet.getSheets();
            Integer originalSheetId = null;
            for (Sheet sheet : sheets) {
                if (SHEET_NAME.equals(sheet.getProperties().getTitle())) {
                    originalSheetId = sheet.getProperties().getSheetId();
                    break;
                }
            }
            if (originalSheetId == null) {
                System.out.println("Original sheet not found");
                return;
            }

            // Duplicate the sheet
            Request duplicateSheetRequest = new Request()
                    .setDuplicateSheet(new com.google.api.services.sheets.v4.model.DuplicateSheetRequest()
                            .setSourceSheetId(originalSheetId)
                            .setNewSheetName(newSheetName));
            BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                    .setRequests(Collections.singletonList(duplicateSheetRequest));

            spreadsheets.batchUpdate(SHEET_ID, batchUpdateRequest).execute();

            System.out.println("Sheet copied and renamed to: " + newSheetName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
