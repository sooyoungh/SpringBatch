package com.example.SpringBatch.crawler.CU;

import com.example.SpringBatch.domain.pbproducts.PbProducts;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class CuEventCrawl {

    private static final String CU_EVENT_URL = "https://cu.bgfretail.com/event/plus.do?category=event&depth2=1&sf=N";

    private WebDriver driver;
    private ChromeOptions options;
    private WebElement latestSort;

    public List<PbProducts> cuPBCrawling(){ // CU 단독 운영 혹은 PB 상품 클릭하기

        options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-popup-blocking");       //팝업 안띄움
        // options.addArguments("headless");                       //브라우저 안띄움
        options.addArguments("start-maximized");
        // options.addArguments("--window-size=1400,800");
        options.addArguments("--disable-gpu");			        //gpu 비활성화
        options.addArguments("--blink-settings=imagesEnabled=false"); //이미지 다운 안받음
        driver = new ChromeDriver(options);

        List<PbProducts> pbProducts = new ArrayList<>();

        try {
            driver.get(CU_EVENT_URL);
            Thread.sleep(5000);

            int ulNum= 1;
            int liNum= 1;
            while (true){
                // /html/body/form/div[3]/div[3]/div[2]/div/ul/li[1]
                WebElement element = driver.findElement(By.xpath("/html/body/form/div[3]/div[3]/div[2]/div/ul[" + ulNum + "]/li[" + liNum + "]"));
                WebElement nameElement = element.findElement(By.className("prod_wrap")).findElement(By.className("prod_text")).findElement(By.className("name")).findElement(By.tagName("p"));
                String name = nameElement.getText();
                log.info("상품명은 " + name);
                Thread.sleep(1000);

                // /html/body/form/div[3]/div[3]/div[2]/div/ul/li[1]/a/div[1]/div[1]/img
                String tmpImgUrl = "/html/body/form/div[3]/div[3]/div[2]/div/ul["+ ulNum + "]/li[" + liNum + "]/a/div[1]/div[1]/img";
                String imgUrl = driver.findElement(By.xpath(tmpImgUrl)).getAttribute("src");
                log.info("이미지 주소는 " + imgUrl);
                // Thread.sleep(1000);

                // /html/body/form/div[3]/div[3]/div[2]/div/ul/li[2]/a/div[1]/div[2]/div[2]/strong
                String priceXpath = "/html/body/form/div[3]/div[3]/div[2]/div/ul["+ ulNum +"]/li["+ liNum +"]/a/div[1]/div[2]/div[2]/strong";
                String price = driver.findElement(By.xpath(priceXpath)).getText();
                log.info("가격은 " + price);
                // Thread.sleep(3000);
                // Thread.sleep(1000);

                // 1+1, 2+1
                String eventPath = "/html/body/form/div[3]/div[3]/div[2]/div/ul/li[" + liNum + "]/a/div[2]/span";
                String event = driver.findElement(By.xpath(eventPath)).getText();
                log.info("할인 정보는 " + event);
                // Thread.sleep(1000);


                PbProducts pbProduct = PbProducts.builder()
                        .imgUrl(imgUrl)
                        .name(name)
                        .price(price)
                        .updatedAt(new Date())
                        .event(event)
                        .build();
                pbProducts.add(pbProduct);

                if (liNum == 40) { // 한번에 40개 보여줌, 더보려면 더보기 클릭
                    ulNum++;
                    liNum = 0;
                    WebElement moreBtn = driver.findElement(By.xpath("/html/body/form/div[3]/div[3]/div[2]/div/div/div[1]"));
                    moreBtn.click();
                    Thread.sleep(3000);
                    log.info("더보기 버튼 클릭!");
                }
                liNum++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.close();
        }

        return pbProducts;
    }

}