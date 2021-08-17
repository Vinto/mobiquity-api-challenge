import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PostTest {

    private static RequestSpecification requestSpecification;
    private static ResponseSpecification responseSpecification;

    @BeforeClass
    public void createRequestSpecification() {
        requestSpecification = new RequestSpecBuilder()
                .setBaseUri("https://jsonplaceholder.typicode.com").build();
    }

    @BeforeClass
    public void createResponseSpecification() {
        responseSpecification = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .build();
    }

    @DataProvider()
    public static Object[][] usernameDetails() {
        return new Object[][]{
                {"Delphine"},
        };
    }

    @Test(dataProvider = "usernameDetails")
    public void validateEmailAddresses(String username) {
        given()
                .spec(requestSpecification)
                .log().all()
                .when()
                .get("users?username=" + username)
                .then()
                .spec(responseSpecification)
                .log().body();
    }
}
