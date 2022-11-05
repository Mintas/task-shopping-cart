package ru.kovalev.shopping.exceptions;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import ru.kovalev.shopping.domain.Product;

public class InsufficientQuantityProblem extends AbstractThrowableProblem {
    private static final String ERROR_MESSAGE_WITH_ID =
            "Insufficient product quantity '%d' for '%s'. Stored: %d, Reserved: %d! Available: %d";

    public InsufficientQuantityProblem(Product product, Integer quantity) {
        super(Problem.DEFAULT_TYPE, Status.BAD_REQUEST.getReasonPhrase(), Status.BAD_REQUEST,
                formatMessage(product, quantity));
    }

    public static String formatMessage(Product product, Integer quantity) {
        return ERROR_MESSAGE_WITH_ID.formatted(quantity,
                product.getId(), product.getStored(), product.getReserved(), product.getAvailableQuantity());
    }
}
