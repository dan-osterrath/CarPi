/**
 * (C) 2017 by 3m5. Media GmbH. http://www.3m5.de
 */
package carpi.threading;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifier for the health monitor executor service.
 * 
 * @author osterrath
 *
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
public @interface HealthMonitor {
}