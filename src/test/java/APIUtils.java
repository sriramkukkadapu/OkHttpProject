import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/** All API request methods. URLs are built from base.url of the selected environment. */
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
		return send(request);
	}

	/** POST {base.url}{path} with a raw JSON string body */
	public Response post(String path, String jsonBody) throws IOException {
		Request request = new Request.Builder()
				.url(Config.baseUrl() + path)
				.post(RequestBody.create(jsonBody, JSON))
				.build();
		return send(request);
	}

	/** POST {base.url}{path} with a body rendered from src/test/resources/templates/{template} */
	public Response post(String path, String template, Map<String, Object> data) throws IOException {
		String payload = TemplateUtil.render(template, data);
		System.out.println("Request body: " + payload);
		return post(path, payload);
	}

	/** Response body as a String. Can be called any number of times. */
	public String getBody(Response response) {
		try {
			return response.peekBody(Long.MAX_VALUE).string();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** JsonPath: parses the response body so values can be read with dot paths, e.g. read("address.geo.lat") */
	public DocumentContext getJsonPath(Response response) {
		return JsonPath.parse(getBody(response));
	}

	/**
	 * Sends the request, reads the body and closes the real response so the connection
	 * is released. Returns a copy of the response with the body held in memory.
	 */
	private Response send(Request request) throws IOException {
		System.out.println(request.method() + " " + request.url());
		Response response = client.newCall(request).execute();
		try {
			ResponseBody original = response.body();
			String body = original.string();
			System.out.println("Response " + response.code() + ": " + body);
			return response.newBuilder()
					.body(ResponseBody.create(body, original.contentType()))
					.build();
		} finally {
			response.close();
		}
	}
}
