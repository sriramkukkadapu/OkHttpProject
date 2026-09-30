import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * All API request methods. URLs are built from base.url of the selected
 * environment.
 */
public class APIUtils {

	private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	private final OkHttpClient client;

	public APIUtils(OkHttpClient client) {
		this.client = client;
	}

	/** GET {base.url}{path} */
	public Response get(String path) throws IOException {
		Request request = new Request.Builder()
				.url(Config.baseUrl() + path)
				.get()
				.build();

		System.out.println(request.method() + " " + request.url());
		Response response = client.newCall(request).execute();
		try {
			// Read the body once, then return a copy that keeps it in memory
			ResponseBody original = response.body();
			String body = original.string();
			System.out.println("Response " + response.code() + ": " + body);
			return response.newBuilder()
					.body(ResponseBody.create(body, original.contentType()))
					.build();
		} finally {
			// Always release the connection
			response.close();
		}
	}

	/** POST {base.url}{path} with a raw JSON string body */
	public Response post(String path, String jsonBody) throws IOException {
		Request request = new Request.Builder()
				.url(Config.baseUrl() + path)
				.post(RequestBody.create(jsonBody, JSON))
				.build();

		System.out.println(request.method() + " " + request.url());
		Response response = client.newCall(request).execute();
		try {
			// Read the body once, then return a copy that keeps it in memory
			ResponseBody original = response.body();
			String body = original.string();
			System.out.println("Response " + response.code() + ": " + body);
			return response.newBuilder()
					.body(ResponseBody.create(body, original.contentType()))
					.build();
		} finally {
			// Always release the connection
			response.close();
		}
	}

	/**
	 * POST {base.url}{path} with a body rendered from
	 * src/test/resources/templates/{template}
	 */
	public Response post(String path, String template, Map<String, Object> data) throws IOException {
		String payload = TemplateUtil.render(template, data);
		System.out.println("Request body: " + payload);
		return post(path, payload);
	}

	/** DELETE {base.url}{path} */
	public Response delete(String path) throws IOException {
		Request request = new Request.Builder()
				.url(Config.baseUrl() + path)
				.delete()
				.build();

		System.out.println(request.method() + " " + request.url());
		Response response = client.newCall(request).execute();
		try {
			// Read the body once, then return a copy that keeps it in memory
			ResponseBody original = response.body();
			String body = original.string();
			System.out.println("Response " + response.code() + ": " + body);
			return response.newBuilder()
					.body(ResponseBody.create(body, original.contentType()))
					.build();
		} finally {
			// Always release the connection
			response.close();
		}
	}

	/** Response body as a String. Can be called any number of times. */
	public String getBody(Response response) {
		try {
			return response.peekBody(Long.MAX_VALUE).string();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * JsonPath: parses the response body so values can be read with dot paths, e.g.
	 * read("address.geo.lat")
	 */
	public DocumentContext getJsonPath(Response response) {
		return JsonPath.parse(getBody(response));
	}

	/** Unique email on every call: random first.last name from Faker + timestamp */
	public String getNewEmail() {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
		return Base.faker.internet().emailAddress(
				(Base.faker.name().firstName() + "." + Base.faker.name().lastName()).toLowerCase() + "." + timestamp);
	}
}
