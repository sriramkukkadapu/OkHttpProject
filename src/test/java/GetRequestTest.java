import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class GetRequestTest extends Base {

	@Test
	void getPostById() throws IOException {
		response = apiUtils.get("/posts/1");

		assertEquals(200, response.code());
		assertTrue(response.header("Content-Type").contains("application/json"));

		path = apiUtils.getJsonPath(response);
		assertEquals(1, (int) path.read("id"));
	}

	@Test
	void getUserWithNestedJson() throws IOException {
		response = apiUtils.get("/users/1");

		assertEquals(200, response.code());

		// Method 1: Jackson - walk the tree one level at a time with get()
		JsonNode node = mapper.readTree(apiUtils.getBody(response));

		assertEquals("Gwenborough", node.get("address").get("city").asText());
		assertEquals("-37.3159", node.get("address").get("geo").get("lat").asText());
		assertEquals("81.1496", node.get("address").get("geo").get("lng").asText());
		assertEquals("Romaguera-Crona", node.get("company").get("name").asText());

		// Method 2: JsonPath - reach nested values with a dot path
		path = apiUtils.getJsonPath(response);

		assertEquals("Gwenborough", path.read("address.city"));
		assertEquals("-37.3159", path.read("address.geo.lat"));
		assertEquals("81.1496", path.read("address.geo.lng"));
		assertEquals("Romaguera-Crona", path.read("company.name"));
	}
}
