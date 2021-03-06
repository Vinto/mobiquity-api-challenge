import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

import org.apache.commons.validator.routines.EmailValidator;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;

public class PostTest {

    private static RequestSpecification requestSpecification;

    String baseUrl = ReadDataProperties.getInstance().getUrl();

    private static String requestBody = "{\n" +
            "  \"title\": \"Hello World\",\n" +
            "  \"body\": \"Hello\",\n" +
            "  \"userId\": \"1\" \n}";

    @BeforeClass
    public void createRequestSpecification() {
        requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(baseUrl).build();
    }

    /**
     * Return a userId given a valid username
     *
     * @param username
     * @return uId
     */
    private int getUserId(String username) {
        Response response = given()
                .spec(requestSpecification)
                .when()
                .get("users?username=" + username);

        Assert.assertEquals(200, response.statusCode());

        int uId = response.then().extract().path("[0].id");

        return uId;
    }

    /**
     * Fetch posts of a user, then validate email addresses in the
     * comments of the posts
     *
     * @param username
     */
    @Test(dataProvider = "usernameDetails", dataProviderClass = DataProviderTestData.class)
    public void validateEmailAddresses(String username) {
        int userId = getUserId(username);

        /* Fetch posts written by user with userId in response */
        Response response = given()
                .spec(requestSpecification)
                .when()
                .get("posts?userId=" + userId);

        /* Get a list of ids of the posts */
        List postIds = response.jsonPath().getList("id");

        SoftAssert softAssert = new SoftAssert();

        for (Object id : postIds) {
            response = given()
                    .spec(requestSpecification)
                    .when()
                    .get("posts/" + id + "/comments");

            /* Get a list of emails in the comments of the posts */
            List emails = response.jsonPath().getList("email");

            for (Object email : emails) {
                /* Validate email addresses */
                boolean valid = EmailValidator.getInstance().isValid(email.toString());

                /* Continue to the next step even after encountering a failure */
                softAssert.assertTrue(valid, email + " from post " + id + " is invalid");
            }
        }
        softAssert.assertAll();
    }

    /**
     * Fetching a user with an invalid postId
     */
    @Test
    public void invalidPostId() {
        Response response = given()
                .spec(requestSpecification)
                .when()
                .get("posts/x.x.x.x.x");

        Assert.assertEquals(404, response.statusCode());
    }

    /**
     * Post a valid POST request
     */
    @Test
    public void postValidPost() {
        Response response = given()
                .spec(requestSpecification)
                .body(requestBody)
                .when()
                .post("/posts")
                .then().extract().response();

        Assert.assertEquals(201, response.statusCode());
        Assert.assertEquals("101", response.jsonPath().getString("id"));
    }

    /**
     * Send a valid PUT request
     */
    @Test
    public void sendValidPutRequest() {
        Response response = given()
                .spec(requestSpecification)
                .body(requestBody)
                .when()
                .put("/posts/1")
                .then().extract().response();

        Assert.assertEquals(200, response.statusCode());
        Assert.assertEquals("Hello World", response.jsonPath().getString("title"));
        Assert.assertEquals("Hello", response.jsonPath().getString("body"));
        Assert.assertEquals("1", response.jsonPath().getString("userId"));
        Assert.assertEquals("1", response.jsonPath().getString("id"));
    }

    /**
     * Send a valid DELETE request
     */
    @Test
    public void sendValidDeleteRequest() {
        Response response = given()
                .spec(requestSpecification)
                .body(requestBody)
                .when()
                .delete("/posts/1")
                .then().extract().response();

        Assert.assertEquals(200, response.statusCode());
    }
}
