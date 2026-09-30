import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetRequestTest {

	private final OkHttpClient client = new OkHttpClient();
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void getPostById() throws IOException {
		Request request = new Request.Builder()
				.url(Config.baseUrl() + "/posts/1")
				.get()
				.build();

		try (Response response = client.newCall(request).execute()) {
			String body = response.body().string();
			System.out.println(body);

			assertEquals(200, response.code());
			assertTrue(response.header("Content-Type").contains("application/json"));

			JsonNode json = mapper.readTree(body);
			int id = json.get("id").asInt();
			assertEquals(1, id);
		}
	}

	@Test
	void getUserWithNestedJson() throws IOException {
		Request request = new Request.Builder()
				.url(Config.baseUrl() + "/users/1")
				.get()
				.build();

		try (Response response = client.newCall(request).execute()) {
			String body = response.body().string();
			System.out.println(response);
			System.out.println(body);

			assertEquals(200, response.code());

			// Method 1: Jackson - walk the tree one level at a time with get()
			JsonNode node = mapper.readTree(body);

			assertEquals("Gwenborough", node.get("address").get("city").asText());
			assertEquals("-37.3159", node.get("address").get("geo").get("lat").asText());
			assertEquals("81.1496", node.get("address").get("geo").get("lng").asText());
			assertEquals("Romaguera-Crona", node.get("company").get("name").asText());

			// Method 2: JsonPath - reach nested values with a dot path
			DocumentContext json = JsonPath.parse(body);

			assertEquals("Gwenborough", json.read("address.city"));
			assertEquals("-37.3159", json.read("address.geo.lat"));
			assertEquals("81.1496", json.read("address.geo.lng"));
			assertEquals("Romaguera-Crona", json.read("company.name"));
		}
	}
}
