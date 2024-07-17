package utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;




public class GoogleSheetService {
    private static GoogleSheetService googleSheetService;
    private GoogleCredentials googleCredentials;

    private final String SHEET_ID = "1M-krxzQIun6fngp9O0-KERr2_ORdksz87uFar4FjhsM";

    private String SHEET_NAME = "data";
    private List<List<Object>> values;
    private HttpRequestInitializer httpRequestInitializer;
    private Spreadsheets spreadsheets;

    /**
     * <p>Private constructor</p>
     */
    GoogleSheetService() {
        createCredential();
    }

    public static GoogleSheetService createInstance() {
        if (googleSheetService == null) {
            googleSheetService = new GoogleSheetService();
        }
        return googleSheetService;
    }

    public void createCredential() {
        try {
            googleCredentials = GoogleCredentials.fromStream(getClass().getResourceAsStream("/key.json"))
                    .createScoped(Arrays.asList(SheetsScopes.SPREADSHEETS,SheetsScopes.DRIVE));
            httpRequestInitializer = new HttpCredentialsAdapter(googleCredentials.fromStream(getClass().getResourceAsStream("/key.json"))
                    .createScoped(Arrays.asList(SheetsScopes.SPREADSHEETS,SheetsScopes.DRIVE)));
            getSpreadsheets();
        } catch (IOException e) {
            System.out.println("Authorized failed");
            e.printStackTrace();
        }

    }

    boolean isAuthorized() {
        if (googleCredentials != null && httpRequestInitializer != null) {
            return true;
        }
        return false;
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
}