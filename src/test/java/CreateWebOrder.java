import com.github.javafaker.Faker;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.Test;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Random;



public class CreateWebOrder {

    @Test
    public void webOrder() throws IOException, InterruptedException {
        // 1. Launch Chrome browser.
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*", "ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        // 2. Navigate to http://secure.smartbearsoftware.com/samples/TestComplete12/WebOrders/Login.aspx
        driver.get("http://secure.smartbearsoftware.com/samples/TestComplete12/WebOrders/Login.aspx");
        Assert.assertEquals(driver.getCurrentUrl(), "http://secure.smartbearsoftware.com/samples/TestComplete12/WebOrders/Login.aspx");

        // 3. Login using username Tester and password test
        driver.findElement(By.id("ctl00_MainContent_username")).sendKeys("Tester");
        driver.findElement(By.id("ctl00_MainContent_password")).sendKeys("test", Keys.ENTER);

        // 4. Click on Order link
        driver.findElement(By.xpath("//a[@href='Process.aspx']")).click();

        // Select Product from the drop-down menu
        List<WebElement> chooseOptions = driver.findElements(By.xpath("//select[@id='ctl00_MainContent_fmwOrder_ddlProduct']/option"));
        int productType = (int) (Math.random() * chooseOptions.size());
        chooseOptions.get(productType).click();

        // 5. Enter a random product quantity between 1 and 100
        Faker faker = new Faker();
        int quantity = faker.number().numberBetween(1, 100);
        driver.findElement(By.id("ctl00_MainContent_fmwOrder_txtQuantity")).sendKeys(Keys.BACK_SPACE, String.valueOf(quantity));

        // 6. Click on Calculate and verify that the Total value is correct.
        //   The logic of calculating is as follows:
        //   Price per unit is 100.  The discount of 8 % is applied to quantities of 10+.
        //   So for example, if the quantity is 8, the Total should be 800.
        //   If the quantity is 20, the Total should be 1840.
        //   If the quantity is 77, the Total should be 7084. And so on.
        driver.findElement(By.xpath("//input[@value='Calculate']")).click();
        double pricePerUnit = Double.parseDouble(driver.findElement(By.id("ctl00_MainContent_fmwOrder_txtUnitPrice")).getAttribute("value"));
        int discount = Integer.parseInt(driver.findElement(By.id("ctl00_MainContent_fmwOrder_txtDiscount")).getAttribute("value"));
        int expectedTotal;
        if (quantity >= 10) {
            expectedTotal = (int) Math.round(quantity * pricePerUnit * (1 - discount / 100.0));
        } else {
            expectedTotal = (int) Math.round(quantity * pricePerUnit);

        }
        int actualTotal = Integer.parseInt(driver.findElement(By.id("ctl00_MainContent_fmwOrder_txtTotal")).getAttribute("value"));
        Assert.assertEquals(actualTotal, expectedTotal, "Total was calculated incorrectly");

        //   Using Faker class:
        // 6. Enter random first name and last name.
        // 7. Enter random street address
        // 8. Enter random city
        // 9. Enter random state
        // 10. Enter a random 5-digit zip code
        //    EXTRA: As an extra challenge, for steps 6-10 download 1000 row of corresponding realistic data from mockaroo.com
        //    in a csv format and load it to your program and use the random row of data from there each time.

        //Using Faker class
        /*
        driver.findElement(By.id("ctl00_MainContent_fmwOrder_txtName")).sendKeys(new Faker().name().name() + " " + new Faker().name().lastName());
        driver.findElement(By.id("ctl00_MainContent_fmwOrder_TextBox2")).sendKeys(new Faker().address().streetAddress());
        driver.findElement(By.id("ctl00_MainContent_fmwOrder_TextBox3")).sendKeys(new Faker().address().city());
        driver.findElement(By.id("ctl00_MainContent_fmwOrder_TextBox4")).sendKeys(new Faker().address().state());
        driver.findElement(By.id("ctl00_MainContent_fmwOrder_TextBox5")).sendKeys(new Faker().address().zipCode());
        */

        //Using data from mockaroo.com
        Path reader = Path.of("src/test/java/MOCK_DATA.csv");
        List<String[]> dataRows = Files.readAllLines(reader) //read all lines from file
                .stream()                                   //convert to stream
                .skip(1)                                 //skip the header row
                .map(line -> line.split(","))         //split each line by comma
                .toList();                                  //collect into a list of String arrays

        // Get a random row of data from the list
        int row = (int) (Math.random() * dataRows.size());
        String[] selectedRow = dataRows.get(row);

        //Use the data from the random row to enter customer's information
        driver.findElement(By.id("ctl00_MainContent_fmwOrder_txtName")).sendKeys(selectedRow[0]);
        driver.findElement(By.id("ctl00_MainContent_fmwOrder_TextBox2")).sendKeys(selectedRow[1]);
        driver.findElement(By.id("ctl00_MainContent_fmwOrder_TextBox3")).sendKeys(selectedRow[2]);
        driver.findElement(By.id("ctl00_MainContent_fmwOrder_TextBox4")).sendKeys(selectedRow[3]);
        driver.findElement(By.id("ctl00_MainContent_fmwOrder_TextBox5")).sendKeys(selectedRow[4]);

        // 11. Select the card type randomly. On each run your script should select a random type.
        // (You need to put all checkboxes into a list and retrieve a random element from the list and click it)
        List<WebElement> cardTypes = driver.findElements(By.name("ctl00$MainContent$fmwOrder$cardList"));
        int buttonIndex = (int)(Math.random() * cardTypes.size());
        cardTypes.get(buttonIndex).click();

        // 12. Enter the random card number:
        //      If Visa is selected, the card number should be a visa number that starts with 4.
        //      If MasterCard is selected, card number should be a mastercard number that starts with 5.
        //      If American Express is selected, card number should be an amex number that starts with 3.
        String creditCardNumber = "";
        switch (buttonIndex){
            case 0:  //Visa
                creditCardNumber = "4" + new Faker().number().digits(15);
                break;
            case 1:   //MasterCard
                creditCardNumber = "5" + new Faker().number().digits(15);
                break;
            case 2:   //American Express
                creditCardNumber = "3" + new Faker().number().digits(14);
                break;
        }

        driver.findElement(By.id("ctl00_MainContent_fmwOrder_TextBox6")).sendKeys(creditCardNumber);

        // 13. Enter a valid expiration date (newer than the current date)
        LocalDate currentDate = LocalDate.now();
        LocalDate expirationDate = currentDate.plusYears(new Random().nextInt(5)).plusMonths(new Random().nextInt(12));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        String formattedDate = expirationDate.format(formatter);
        driver.findElement(By.id("ctl00_MainContent_fmwOrder_TextBox1")).sendKeys(formattedDate);

        // 14. Click on Process
        driver.findElement(By.id("ctl00_MainContent_fmwOrder_InsertButton")).click();

        // 15. Verify that “New order has been successfully added” message appeared on the page.
        String expectedText = "New order has been successfully added";
        String pageSource = driver.getPageSource();
        Assert.assertTrue(pageSource.contains(expectedText));

        //16. Click on View All Orders link.
        driver.findElement(By.linkText("View all orders")).click();

        // 17. The placed order details appears on the first row of the orders table. Verify that the entire information
        // contained on the row (Name, Product, Quantity, etc) matches the previously entered information in previous steps.
        driver.findElement(By.xpath("//a[@href='Default.aspx']")).click();
        List <WebElement> columns = driver.findElements(By.xpath("//tbody//tr[position()=2]//td"));
        Assert.assertEquals(columns.get(1).getText(), selectedRow[0], "The names do not match");
        Assert.assertEquals(columns.get(2).getText(), getProductType(productType), "Wrong product type");
        Assert.assertEquals(columns.get(3).getText(), String.valueOf(quantity), "Wrong quantity");
        Assert.assertEquals(columns.get(4).getText(), getTodaysDate(), "Wrong date");
        Assert.assertEquals(columns.get(5).getText(), selectedRow[1], "Wrong Street");
        Assert.assertEquals(columns.get(6).getText(), selectedRow[2], "Wrong city");
        Assert.assertEquals(columns.get(7).getText(), selectedRow[3], "Wrong state");
        Assert.assertEquals(columns.get(8).getText(), selectedRow[4], "Wrong zip code");
        Assert.assertEquals(columns.get(9).getText().toUpperCase(), getCardType(buttonIndex).toUpperCase(), "Invalid card type");
        Assert.assertEquals(columns.get(10).getText(), creditCardNumber, "Wrong card number");
        Assert.assertEquals(columns.get(11).getText(), formattedDate, "Wrong expiration date");

        // 18. Log out of the application
        driver.close();
        driver.quit();

    }

        String getProductType(int productType){
        switch (productType){
            case 0:
                return "MyMoney";

            case 1:
                return "FamilyAlbum";

            case 2:
                return "ScreenSaver";

        }
        return "Invalid product type";
    }

    String getCardType(int cardType){
        switch (cardType){
            case 0:
                return "VISA";

            case 1:
                return "MasterCard";

            case 2:
                return "American Express";

        }
        return "Invalid card type";
    }

    public String getTodaysDate() {
        Date date = new Date();
        SimpleDateFormat formatter2 = new SimpleDateFormat("MM/dd/yyyy");
        return formatter2.format(date);
    }
}


















