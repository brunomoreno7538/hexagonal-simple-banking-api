package moreno.corebanking_natixis.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumValueValidator implements ConstraintValidator<ValidEnumValue, Enum<?>> {

    private Set<String> allowedValues;
    private boolean ignoreCase;

    @Override
    public void initialize(ValidEnumValue constraintAnnotation) {
        this.ignoreCase = constraintAnnotation.ignoreCase();
        if (this.ignoreCase) {
            this.allowedValues = Arrays.stream(constraintAnnotation.allowedValues())
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());
        } else {
            this.allowedValues = new HashSet<>(Arrays.asList(constraintAnnotation.allowedValues()));
        }
    }

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        String enumValueName = value.name();

        if (this.ignoreCase) {
            return this.allowedValues.contains(enumValueName.toUpperCase());
        } else {
            return this.allowedValues.contains(enumValueName);
        }
    }
}