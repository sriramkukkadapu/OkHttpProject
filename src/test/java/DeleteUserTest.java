import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class DeleteUserTest extends Base {

	@Test
	void deleteUser() throws IOException {
		// Values for the placeholders in templates/createUser.ftl
		Map<String, Object> userData = new HashMap<>();
		userData.put("name", faker.name().fullName());
		userData.put("email", apiUtils.getNewEmail());
		userData.put("gender", "male");
		userData.put("status", "active");

		// Create the user we are going to delete
		response = apiUtils.post("/users", "createUser.ftl", userData);
		assertEquals(201, response.code());

		path = apiUtils.getJsonPath(response);
		int userId = path.read("id");

		// Delete it - GoRest returns 204 No Content with an empty body
		response = apiUtils.delete("/users/" + userId);
		assertEquals(204, response.code());
		assertTrue(apiUtils.getBody(response).isEmpty());

		// The user is really gone
		response = apiUtils.get("/users/" + userId);
		assertEquals(404, response.code());
	}
}
