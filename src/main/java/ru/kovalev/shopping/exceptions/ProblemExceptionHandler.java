package ru.kovalev.shopping.exceptions;

import java.net.URI;
import java.sql.SQLException;
import java.util.Collection;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.validation.ConstraintViolationAdviceTrait;
import org.zalando.problem.violations.Violation;

@ControllerAdvice
public class ProblemExceptionHandler implements
        ProblemHandling, /*SecurityAdviceTrait,*/ ConstraintViolationAdviceTrait {
    private static final String DATABASE_ERROR = "Database error";

    @ExceptionHandler({
            DataAccessException.class,
            TransactionException.class,
            SQLException.class,
            //PSQLException.class, //requires implementation level dependency on postgresql
    })
    protected ResponseEntity<Problem> handleProblem(Exception exception, NativeWebRequest nativeWebRequest) {
        return create(exception,
                Problem.builder()
                        .withType(Problem.DEFAULT_TYPE)
                        .withTitle(Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .withStatus(Status.INTERNAL_SERVER_ERROR)
                        .withDetail(DATABASE_ERROR)
                        .build(),
                nativeWebRequest);
    }

    /**
     * see  {@link org.zalando.problem.spring.web.advice.validation.BaseValidationAdviceTrait}
     * @return problem with Title consistent with other handlers, also returns descriptive details to problem contract
     */
    @Override
    public ResponseEntity<Problem> newConstraintViolationProblem(Throwable throwable,
                                                                 Collection<Violation> stream,
                                                                 NativeWebRequest request) {

        final var type = defaultConstraintViolationType();
        final var status = defaultConstraintViolationStatus();

        final var violations = stream.stream()
                // sorting to make tests deterministic
                .sorted(comparing(Violation::getField).thenComparing(Violation::getMessage))
                .collect(toList());

        final Problem problem = new ConstraintViolationProblemWithDetail(type, status, violations);

        return create(throwable, problem, request);
    }

    @Override
    public URI defaultConstraintViolationType() {
        return Problem.DEFAULT_TYPE;
    }
}
