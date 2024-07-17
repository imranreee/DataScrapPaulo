package master;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.GoogleSheetService;
import utils.GoogleSheetServiceCopy;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectData {
    WebDriver driver;
    String URL = "https://geappliancescustomernet.com/home";
    int ROW_NO = 2;
    GoogleSheetService sheetService = GoogleSheetService.createInstance();
    //utils.GoogleSheetServiceCopy sheetServiceCopy = utils.GoogleSheetServiceCopy.createInstance();

    String numberOfAds = sheetService.readValuesByRowAndColumn(1, 3);

    private Object[][] data = new Object[0][0];
    @DataProvider(name="NumberProvider")
    public Object[][] getNumber() {
        data = new Object[Integer.parseInt(numberOfAds)][1];
        return data;
    }

    @BeforeTest()
    public void startBot() throws InterruptedException {
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<String, Object>();
        Map<String, Object> profile = new HashMap<String, Object>();
        Map<String, Integer> contentSettings = new HashMap<String, Integer>();

        // SET CHROME OPTIONS
        // 0 - Default, 1 - Allow, 2 - Block
        contentSettings.put("notifications", 2);
        contentSettings.put("geolocation", 2);
        profile.put("managed_default_content_settings", contentSettings);
        prefs.put("profile", profile);
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--headless");

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        driver.get(URL);

        System.out.println("Browser successfully up and run with "+URL);
        driver.manage().window().maximize();

        By btnLogin = By.xpath("//input[@value='Login']");
        By btnLoginAgain = By.xpath("//button[@aria-label='Log In']");
        By inputUsername = By.xpath("//input[@name='username']");
        By inputPassword = By.xpath("//input[@name='password']");
        By dropDownSelectAgent = By.xpath("//select[@id='selectPriceSourceD']");
        By valueDropDown = By.xpath("//option[@value='196053']");

        waitForClickAbilityOf(btnLogin);
        driver.findElement(btnLogin).click();

        waitForClickAbilityOf(inputUsername);
        driver.findElement(inputUsername).click();
        driver.findElement(inputUsername).sendKeys("admin@mardeys.com");

        driver.findElement(inputPassword).click();
        driver.findElement(inputPassword).sendKeys("GEsystem123$");

        waitForClickAbilityOf(btnLoginAgain);
        driver.findElement(btnLoginAgain).click();

        waitForClickAbilityOf(dropDownSelectAgent);
        Thread.sleep(1000);
        driver.findElement(dropDownSelectAgent).click();

        waitForClickAbilityOf(valueDropDown);
        Thread.sleep(1000);
        driver.findElement(valueDropDown).click();
    }

    @Test(dataProvider="NumberProvider")
    public void collectData(String args) throws InterruptedException, IOException {
        ROW_NO += 1;
        By inputSearchBox = By.xpath("//input[@name='searchFormSearchBox']");
        By btnSearch = By.xpath("//input[@value='Search']");
        By noResult = By.xpath("//td[@colspan='6']");
        By msrp = By.xpath("//td[@class='priceColumn msrp']");
        By price = By.xpath("//td[@class='priceColumn price']");
        By netPrice = By.xpath("//div[@id='productResultsTableForm']//table//tr//td[6]//div[1]");
        By model = By.xpath("//div[@class='skuDiv']");
        By availableQty = By.xpath("//div[@id='productResultsTableForm']//table//td[4]//div//table//td[1]");
        By nextAvail = By.xpath("//div[@id='productResultsTableForm']//table//td[4]//div//table//tr//td[2]");
        By eta = By.xpath("//div[@id='productResultsTableForm']//table//tr//td[4]//div//table//tr//td[3]");

        /*driver.findElement(inputSearchBox).click();
        driver.findElement(inputSearchBox).clear();
        String searchKey = sheetService.readValuesByRowAndColumn(ROW_NO, 3);
        driver.findElement(inputSearchBox).sendKeys(searchKey);
        Thread.sleep(3000);

        waitForClickAbilityOf(btnSearch);
        driver.findElement(btnSearch).click();*/

        waitForClickAbilityOf(inputSearchBox);

        String searchKey = sheetService.readValuesByRowAndColumn(ROW_NO, 4);
        Thread.sleep(1000);
        String fullURL = "https://geappliancescustomernet.com/search-result/product-number/"+searchKey;
        driver.navigate().to(fullURL);
        Thread.sleep(5000);

        if (!driver.findElements(noResult).isEmpty()){
            sheetService.writeDataGoogleSheets(Collections.singletonList("No Record Found"), ROW_NO,"!O");
        }else {
            if (driver.findElements(model).isEmpty()){
                sheetService.writeDataGoogleSheets(Collections.singletonList("Something went wrong"), ROW_NO,"!O");
            }else {
                sheetService.writeDataGoogleSheets(Collections.singletonList("Record Found"), ROW_NO,"!O");
                Thread.sleep(2000);

                waitForClickAbilityOf(model);
                String modTxt = driver.findElement(model).getText();
                sheetService.writeDataGoogleSheets(Collections.singletonList(modTxt), ROW_NO,"!G");

                String msrpText = driver.findElement(msrp).getText();
                String onlyMsrp = extractPrice(msrpText);
                sheetService.writeDataGoogleSheets(Collections.singletonList(onlyMsrp), ROW_NO,"!H");

                String priceText = driver.findElement(price).getText();
                priceText = removeDollarSign(priceText);
                sheetService.writeDataGoogleSheets(Collections.singletonList(priceText), ROW_NO,"!I");

                String netPriceText = driver.findElement(netPrice).getText();
                netPriceText = removeDollarSign(netPriceText);
                sheetService.writeDataGoogleSheets(Collections.singletonList(netPriceText), ROW_NO,"!J");

                String avlQtyTxt = driver.findElement(availableQty).getText();
                sheetService.writeDataGoogleSheets(Collections.singletonList(avlQtyTxt), ROW_NO,"!K");

                String nextAvailTxt = driver.findElement(nextAvail).getText();
                sheetService.writeDataGoogleSheets(Collections.singletonList(nextAvailTxt), ROW_NO,"!L");

                String etaText = driver.findElement(eta).getText();
                sheetService.writeDataGoogleSheets(Collections.singletonList(etaText), ROW_NO,"!M");

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                sheetService.writeDataGoogleSheets(Collections.singletonList(formatter.format(date)), ROW_NO,"!N");
            }
        }
    }

    @AfterTest()
    public void endTest(){
        GoogleSheetServiceCopy sheetServiceCopy = GoogleSheetServiceCopy.createInstance();
        sheetServiceCopy.copySheetWithTimestamp();
        driver.quit();
        System.out.println("End BOT");
    }

    public void waitForClickAbilityOf(By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static String extractPrice(String input) {
        String regex = "\\$([0-9,]+\\.[0-9]{2})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        }
        return null;
    }

    public static String removeDollarSign(String input) {
        return input.replace("$", "");
    }
}