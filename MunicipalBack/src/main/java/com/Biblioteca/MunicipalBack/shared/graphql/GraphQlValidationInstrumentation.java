package com.Biblioteca.MunicipalBack.shared.graphql;

import graphql.GraphQLError;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.schema.DataFetcher;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GraphQlValidationInstrumentation implements Instrumentation {

    private final Validator validator;

    @Override
    public DataFetcher<?> instrumentDataFetcher(DataFetcher<?> dataFetcher,
                                                 InstrumentationFieldFetchParameters parameters,
                                                 InstrumentationState state) {
        return environment -> {
            Map<String, Object> arguments = environment.getArguments();
            for (Object value : arguments.values()) {
                if (value != null && isInputRecord(value)) {
                    validate(value, environment);
                }
            }
            return dataFetcher.get(environment);
        };
    }

    private boolean isInputRecord(Object value) {
        return value.getClass().isRecord();
    }

    private <T> void validate(T input, graphql.schema.DataFetchingEnvironment env) {
        Set<ConstraintViolation<T>> violations = validator.validate(input);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));

            throw new GraphQlValidationException(message);
        }
    }

    public static class GraphQlValidationException extends RuntimeException {
        public GraphQlValidationException(String message) {
            super(message);
        }
    }
}
