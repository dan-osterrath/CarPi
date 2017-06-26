package carpi.log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Class for creating logger instances.
 * 
 * @author osterrath
 *
 */
@ApplicationScoped
public class LoggerFactory {
	/**
	 * Already created loggers.
	 */
	private static final Map<Class<?>, Logger> classLoggers = Collections.synchronizedMap(new HashMap<>());

	/**
	 * Creates a {@link Logger} instance.
	 * 
	 * @return logger
	 */
	@Produces
	public Logger createLogger(InjectionPoint ip) {
		Class<?> clazz = ip.getMember().getDeclaringClass();
		synchronized (classLoggers) {
			if (classLoggers.containsKey(clazz)) {
				return classLoggers.get(clazz);
			}
			Logger log = Logger.getLogger(clazz.getName());
			classLoggers.put(clazz, log);
			return log;
		}
	}
}
