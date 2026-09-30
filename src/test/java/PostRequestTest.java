import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class PostRequestTest extends Base {

	@Test
	void createUser() throws IOException {
		// Values for the placeholders in templates/createUser.ftl
		Map<String, Object> userData = new HashMap<>();
		userData.put("name", faker.name().fullName());
		userData.put("email", apiUtils.getNewEmail());
		userData.put("gender", "male");
		userData.put("status", "active");

		response = apiUtils.post("/users", "createUser.ftl", userData);

		assertEquals(201, response.code());

		// Response returns the data we sent, plus a new id
		path = apiUtils.getJsonPath(response);
		assertEquals(userData.get("name"), path.read("name"));
		assertEquals(userData.get("email"), path.read("email"));
		assertEquals(userData.get("gender"), path.read("gender"));
		assertEquals(userData.get("status"), path.read("status"));
		assertNotNull(path.read("id"));

		// Clean up: delete the user we created
		int userId = path.read("id");
		response = apiUtils.delete("/users/" + userId);
		assertEquals(204, response.code());
	}

	@Test
	void createPostForUser() throws IOException {
		// A post must belong to an existing user, so create one first
		Map<String, Object> userData = new HashMap<>();
		userData.put("name", faker.name().fullName());
		userData.put("email", apiUtils.getNewEmail());
		userData.put("gender", "female");
		userData.put("status", "active");

		response = apiUtils.post("/users", "createUser.ftl", userData);
		assertEquals(201, response.code());
		path = apiUtils.getJsonPath(response);
		int userId = path.read("id");

		// Values for the placeholders in templates/createPost.ftl
		Map<String, Object> postData = new HashMap<>();
		postData.put("userId", userId);
		postData.put("title", "OkHttp POST example");
		postData.put("body", "Created from PostRequestTest");

		response = apiUtils.post("/posts", "createPost.ftl", postData);

		assertEquals(201, response.code());

		path = apiUtils.getJsonPath(response);
		assertEquals(postData.get("userId"), path.read("user_id"));
		assertEquals(postData.get("title"), path.read("title"));
		assertEquals(postData.get("body"), path.read("body"));
		assertNotNull(path.read("id"));

		// Clean up: delete the post, then the user
		int postId = path.read("id");
		response = apiUtils.delete("/posts/" + postId);
		assertEquals(204, response.code());

		response = apiUtils.delete("/users/" + userId);
		assertEquals(204, response.code());
	}
}
