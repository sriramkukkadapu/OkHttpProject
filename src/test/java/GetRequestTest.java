import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class GetRequestTest extends Base {

	@Test
	void getUsersList() throws IOException {
		response = apiUtils.get("/users");

		assertEquals(200, response.code());
		assertTrue(response.header("Content-Type").contains("application/json"));

		path = apiUtils.getJsonPath(response);
		int count = path.read("$.length()");
		assertTrue(count > 0);
		assertNotNull(path.read("[0].id"));
		assertNotNull(path.read("[0].email"));
	}

	@Test
	void getUserById() throws IOException {
		// GoRest data is shared and changes all the time, so create our own user first
		Map<String, Object> userData = new HashMap<>();
		userData.put("name", faker.name().fullName());
		userData.put("email", apiUtils.getNewEmail());
		userData.put("gender", "female");
		userData.put("status", "active");

		response = apiUtils.post("/users", "createUser.ftl", userData);
		assertEquals(201, response.code());
		path = apiUtils.getJsonPath(response);
		int userId = path.read("id");

		response = apiUtils.get("/users/" + userId);
		assertEquals(200, response.code());

		// Method 1: Jackson - walk the tree with get()
		JsonNode node = mapper.readTree(apiUtils.getBody(response));

		assertEquals(userId, node.get("id").asInt());
		assertEquals(userData.get("name"), node.get("name").asText());
		assertEquals(userData.get("email"), node.get("email").asText());
		assertEquals(userData.get("gender"), node.get("gender").asText());
		assertEquals(userData.get("status"), node.get("status").asText());

		// Method 2: JsonPath - read values with a path
		path = apiUtils.getJsonPath(response);

		assertEquals(userId, (int) path.read("id"));
		assertEquals(userData.get("name"), path.read("name"));
		assertEquals(userData.get("email"), path.read("email"));
		assertEquals(userData.get("gender"), path.read("gender"));
		assertEquals(userData.get("status"), path.read("status"));

		// Clean up: delete the user we created
		response = apiUtils.delete("/users/" + userId);
		assertEquals(204, response.code());
	}
}
