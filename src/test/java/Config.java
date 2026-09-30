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
	}

	public static String get(String key) {
		return props.getProperty(key);
	}

	public static String baseUrl() {
		return get("base.url");
	}
}
