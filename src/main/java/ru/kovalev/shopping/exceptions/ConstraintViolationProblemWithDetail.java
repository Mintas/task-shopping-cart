package ru.kovalev.shopping.exceptions;

import java.net.URI;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.StatusType;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

@JsonPropertyOrder({"title", "status", "detail", "violations"})
public class ConstraintViolationProblemWithDetail extends ConstraintViolationProblem {
    public static final String DETAIL_MESSAGE = "Constraint Violation";

    public ConstraintViolationProblemWithDetail(List<Violation> violations) {
        super(Problem.DEFAULT_TYPE, Status.BAD_REQUEST, violations);
    }

    public ConstraintViolationProblemWithDetail(URI type, StatusType status, List<Violation> violations) {
        super(type, status, violations);
    }

    @Override
    public String getTitle() {
        return getStatus().getReasonPhrase();
    }

    @Override
    public String getDetail() {
        return DETAIL_MESSAGE;
    }
}
