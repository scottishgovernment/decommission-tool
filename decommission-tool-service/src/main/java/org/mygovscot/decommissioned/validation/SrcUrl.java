package org.mygovscot.decommissioned.validation;

import javax.validation.Constraint;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Constraint(validatedBy = SrcUrlValidator.class)
public @interface SrcUrl {

    String message();

    Class[] groups() default {};

    Class[] payload() default {};
}
