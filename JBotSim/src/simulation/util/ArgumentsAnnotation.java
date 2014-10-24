package simulation.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

public @interface ArgumentsAnnotation {
	public String name();
	public String help() default "";
	public String[] values() default {};
	public String defaultValue() default "";
}
