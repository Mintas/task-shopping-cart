package ru.kovalev.shopping.exceptions;

import java.sql.SQLException;
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
}
