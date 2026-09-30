import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class PostsEnd2EndTest extends Base {

	@Test
	void createGetDeletePost() throws IOException {
		// A post must belong to an existing user, so create one first
		Map<String, Object> userData = new HashMap<>();
		userData.put("name", faker.name().fullName());
		userData.put("email", apiUtils.getNewEmail());
		userData.put("gender", "male");
		userData.put("status", "active");

		response = apiUtils.post("/users", "createUser.ftl", userData);
		assertEquals(201, response.code());
		path = apiUtils.getJsonPath(response);
		int userId = path.read("id");

		// Values for the placeholders in templates/createPost.ftl
		Map<String, Object> postData = new HashMap<>();
		postData.put("userId", userId);
		postData.put("title", "E2E post " + System.currentTimeMillis());
		postData.put("body", "Created by PostsEnd2EndTest");

		// 1. Create
		response = apiUtils.post("/posts", "createPost.ftl", postData);
		assertEquals(201, response.code());

		path = apiUtils.getJsonPath(response);
		int postId = path.read("id");

		// 2. Get and verify it was created properly
		response = apiUtils.get("/posts/" + postId);
		assertEquals(200, response.code());

		path = apiUtils.getJsonPath(response);
		assertEquals(postId, (int) path.read("id"));
		assertEquals(postData.get("userId"), path.read("user_id"));
		assertEquals(postData.get("title"), path.read("title"));
		assertEquals(postData.get("body"), path.read("body"));

		// 3. Delete
		response = apiUtils.delete("/posts/" + postId);
		assertEquals(204, response.code());

		// 4. Get and verify it was deleted
		response = apiUtils.get("/posts/" + postId);
		assertEquals(404, response.code());

		// Clean up: delete the user we created
		response = apiUtils.delete("/users/" + userId);
		assertEquals(204, response.code());
	}
}
