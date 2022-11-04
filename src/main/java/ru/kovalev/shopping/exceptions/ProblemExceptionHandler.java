package ru.kovalev.shopping.exceptions;

import java.net.URI;
import java.sql.SQLException;
import java.util.Collection;
import javax.persistence.OptimisticLockException;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
    private static final String DATABASE_ERROR_DETAIL = "Database error";
    private static final String OPTIMISTIC_LOCK_DETAIL = "Optimistic lock error";

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
                        .withDetail(DATABASE_ERROR_DETAIL)
                        .build(),
                nativeWebRequest);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Problem> handleDataIntegrityViolation(Exception exception, NativeWebRequest nativeWebRequest) {
        return create(exception,
                Problem.builder()
                        .withType(Problem.DEFAULT_TYPE)
                        .withTitle(Status.BAD_REQUEST.getReasonPhrase())
                        .withStatus(Status.BAD_REQUEST)
                        .withDetail(DATABASE_ERROR_DETAIL)
                        .build(),
                nativeWebRequest);
    }

    @ExceptionHandler({
            ObjectOptimisticLockingFailureException.class,
            OptimisticLockException.class,
            OptimisticLockingFailureException.class
    })
    protected ResponseEntity<Problem> handleOptimisticLock(Exception exception, NativeWebRequest nativeWebRequest) {
        return create(exception,
                Problem.builder()
                        .withType(Problem.DEFAULT_TYPE)
                        .withTitle(Status.CONFLICT.getReasonPhrase())
                        .withStatus(Status.CONFLICT)
                        .withDetail(OPTIMISTIC_LOCK_DETAIL)
                        .build(),
                nativeWebRequest);
    }

    /**
     * see  {@link org.zalando.problem.spring.web.advice.validation.BaseValidationAdviceTrait}
     *
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

        final var problem = new ConstraintViolationProblemWithDetail(type, status, violations);

        return create(throwable, problem, request);
    }

    @Override
    public URI defaultConstraintViolationType() {
        return Problem.DEFAULT_TYPE;
    }
}
