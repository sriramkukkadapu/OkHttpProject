import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public class Config {

	// Pick the environment with -Denv=qa / -Denv=staging; defaults to "test"
	private static final String ENV = System.getProperty("env", "test");
	private static final Properties props = new Properties();

	static {
		String file = ENV + ".env.properties";
		try (InputStream in = Config.class.getClassLoader().getResourceAsStream(file)) {
			if (in == null) {
				throw new IllegalStateException(file + " not found on classpath");
			}
			props.load(in);
			System.out.println("Loaded environment: " + ENV + " (" + file + ")");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		if (token() == null) {
			System.out.println("WARNING: GO_REST_API_TOKEN is not set - POST/DELETE requests will fail with 401");
		}
	}

	public static String get(String key) {
		return props.getProperty(key);
	}

	public static String baseUrl() {
		return get("base.url");
	}

	/**
	 * API access token, from the GO_REST_API_TOKEN environment variable or -Dapi.token=...
	 * It is never stored in a properties file, so it can't end up in git.
	 */
	public static String token() {
		String token = System.getenv("GO_REST_API_TOKEN");
		if (token == null || token.isBlank()) {
			token = System.getProperty("api.token");
		}
		return (token == null || token.isBlank()) ? null : token;
	}
}
