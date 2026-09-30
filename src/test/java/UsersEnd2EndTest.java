import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class UsersEnd2EndTest extends Base {

	@Test
	void createGetDeleteUser() throws IOException {
		// Values for the placeholders in templates/createUser.ftl
		Map<String, Object> userData = new HashMap<>();
		userData.put("name", faker.name().fullName());
		userData.put("email", apiUtils.getNewEmail());
		userData.put("gender", "female");
		userData.put("status", "active");

		// 1. Create
		response = apiUtils.post("/users", "createUser.ftl", userData);
		assertEquals(201, response.code());

		path = apiUtils.getJsonPath(response);
		int userId = path.read("id");

		// 2. Get and verify it was created properly
		response = apiUtils.get("/users/" + userId);
		assertEquals(200, response.code());

		path = apiUtils.getJsonPath(response);
		assertEquals(userId, (int) path.read("id"));
		assertEquals(userData.get("name"), path.read("name"));
		assertEquals(userData.get("email"), path.read("email"));
		assertEquals(userData.get("gender"), path.read("gender"));
		assertEquals(userData.get("status"), path.read("status"));

		// 3. Delete
		response = apiUtils.delete("/users/" + userId);
		assertEquals(204, response.code());

		// 4. Get and verify it was deleted
		response = apiUtils.get("/users/" + userId);
		assertEquals(404, response.code());
	}
}
