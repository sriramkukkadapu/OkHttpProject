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
	}
}
