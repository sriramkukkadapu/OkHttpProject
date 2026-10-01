import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

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
		System.out.println("No of Users: " + count);
		assertTrue(count > 0);
		assertNotNull(path.read("[0].id"));
		assertNotNull(path.read("[0].email"));
		int userId = path.read("[0].id");
		response = apiUtils.get("/users/" + userId);
		assertEquals(200, response.code());

		// Method 1: Jackson - walk the tree with get()
		JsonNode node = mapper.readTree(apiUtils.getBody(response));

		assertEquals(userId, node.get("id").asInt());

		// Method 2: JsonPath - read values with a path
		path = apiUtils.getJsonPath(response);

		assertEquals(userId, (int) path.read("id"));
	}
}
