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
		assertEquals(data.get("title"), path.read("title"));
		assertEquals(data.get("body"), path.read("body"));
		assertEquals(data.get("userId"), path.read("userId"));
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
		assertEquals(userData.get("name"), path.read("name"));
		assertEquals(userData.get("email"), path.read("email"));
		assertEquals(userData.get("street"), path.read("address.street"));
		assertEquals(userData.get("city"), path.read("address.city"));
		assertEquals(userData.get("zipcode"), path.read("address.zipcode"));
		assertEquals(userData.get("lat"), path.read("address.geo.lat"));
		assertEquals(userData.get("lng"), path.read("address.geo.lng"));
		assertEquals(userData.get("companyName"), path.read("company.name"));
		assertNotNull(path.read("id"));

		// Clean up: delete the user we created
		int userId = path.read("id");
		response = apiUtils.delete("/users/" + userId);
		assertEquals(200, response.code());
	}
}
