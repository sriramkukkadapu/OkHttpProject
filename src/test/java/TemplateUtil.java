import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class TemplateUtil {

	private static final Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);

	static {
		// Templates are loaded from src/test/resources/templates
		cfg.setClassForTemplateLoading(TemplateUtil.class, "/templates");
		cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
		// Fail fast if a placeholder has no value
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
	}

	/** Fills the placeholders in a template file and returns the result as a String. */
	public static String render(String templateName, Map<String, Object> data) {
		try (StringWriter out = new StringWriter()) {
			cfg.getTemplate(templateName).process(data, out);
			return out.toString();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (TemplateException e) {
			throw new IllegalStateException("Failed to render template " + templateName, e);
		}
	}
}
