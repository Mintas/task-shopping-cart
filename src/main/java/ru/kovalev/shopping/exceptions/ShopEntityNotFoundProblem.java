package ru.kovalev.shopping.exceptions;

import java.util.function.Supplier;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public class ShopEntityNotFoundProblem extends AbstractThrowableProblem {
    private static final String ERROR_MESSAGE_WITH_ID = "Entity of type '%s' with id '%s' not found";
    private static final String AT_LOCATION = "%s %s";

    public ShopEntityNotFoundProblem(Class<?> clazz, Object entityId) {
        this(clazz, entityId, null);
    }

    public ShopEntityNotFoundProblem(Class<?> clazz, Object entityId, Supplier<String> location) {
        super(Problem.DEFAULT_TYPE, Status.NOT_FOUND.getReasonPhrase(), Status.NOT_FOUND,
                formatMessage(clazz, entityId, location));
    }

    public static String formatMessage(Class<?> clazz, Object entityId, Supplier<String> location) {
        var error = ERROR_MESSAGE_WITH_ID.formatted(clazz.getSimpleName(), entityId);
        return location == null ? error : AT_LOCATION.formatted(error, location.get());
    }
}
