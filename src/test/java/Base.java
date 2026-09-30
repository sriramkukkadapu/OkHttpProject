import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;

import net.datafaker.Faker;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Parent class for all API tests. Holds the default objects every test needs,
 * so tests don't create them themselves.
 */
public class Base {

	// Shared by all tests - created once
	// Adds "Authorization: Bearer <GO_REST_API_TOKEN>" to every request, so API methods don't have to
	protected static final OkHttpClient client = new OkHttpClient.Builder()
			.addInterceptor(chain -> {
				Request request = chain.request();
				if (Config.token() != null) {
					request = request.newBuilder()
							.header("Authorization", "Bearer " + Config.token())
							.build();
				}
				return chain.proceed(request);
			})
			.build();
	protected static final ObjectMapper mapper = new ObjectMapper();
	protected static final APIUtils apiUtils = new APIUtils(client);
	protected static final Faker faker = new Faker();

	// Per test - JUnit creates a new test instance for every @Test, so these start empty each time
	protected Response response;
	protected DocumentContext path;
}
