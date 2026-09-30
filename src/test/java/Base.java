import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;

import net.datafaker.Faker;

import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Parent class for all API tests. Holds the default objects every test needs,
 * so tests don't create them themselves.
 */
public class Base {

	// Shared by all tests - created once
	protected static final OkHttpClient client = new OkHttpClient();
	protected static final ObjectMapper mapper = new ObjectMapper();
	protected static final APIUtils apiUtils = new APIUtils(client);
	protected static final Faker faker = new Faker();

	// Per test - JUnit creates a new test instance for every @Test, so these start empty each time
	protected Response response;
	protected DocumentContext path;
}
