import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class PostRequestTest extends Base {

	@Test
	void createPost() throws IOException {
		// Values for the placeholders in templates/createPost.ftl
		Map<String, Object> data = new HashMap<>();
		data.put("title", "OkHttp POST example");
		data.put("body", "Created from PostRequestTest");
		data.put("userId", 1);

		response = apiUtils.post("/posts", "createPost.ftl", data);

		assertEquals(201, response.code());

		// Response echoes the data we sent, plus a new id
		path = apiUtils.getJsonPath(response);
		assertEquals("OkHttp POST example", path.read("title"));
		assertEquals("Created from PostRequestTest", path.read("body"));
		assertEquals(1, (int) path.read("userId"));
		assertNotNull(path.read("id"));

		// Clean up: delete the post we created
		int postId = path.read("id");
		response = apiUtils.delete("/posts/" + postId);
		assertEquals(200, response.code());
	}

	@Test
	void createUserWithNestedJson() throws IOException {
		// Unique email on every run
		String email = apiUtils.getNewEmail();

		// One flat map - templates/createUser.ftl places each value in the nested JSON
		Map<String, Object> userData = new HashMap<>();
		userData.put("name", "Test User");
		userData.put("email", email);
		userData.put("street", "221B Baker Street");
		userData.put("city", "London");
		userData.put("zipcode", "NW1 6XE");
		userData.put("lat", "51.5072");
		userData.put("lng", "-0.1276");
		userData.put("companyName", "Anthropic");

		response = apiUtils.post("/users", "createUser.ftl", userData);

		assertEquals(201, response.code());

		// Read the nested values back with dot paths
		path = apiUtils.getJsonPath(response);
		assertEquals("Test User", path.read("name"));
		assertEquals(email, path.read("email"));
		assertEquals("London", path.read("address.city"));
		assertEquals("51.5072", path.read("address.geo.lat"));
		assertEquals("-0.1276", path.read("address.geo.lng"));
		assertEquals("Anthropic", path.read("company.name"));
		assertNotNull(path.read("id"));

		// Clean up: delete the user we created
		int userId = path.read("id");
		response = apiUtils.delete("/users/" + userId);
		assertEquals(200, response.code());
	}
}
