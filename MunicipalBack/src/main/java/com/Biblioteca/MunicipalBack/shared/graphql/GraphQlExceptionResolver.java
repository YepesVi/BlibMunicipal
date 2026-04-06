package com.Biblioteca.MunicipalBack.shared.graphql;

import com.Biblioteca.MunicipalBack.shared.exceptions.BusinessException;
import com.Biblioteca.MunicipalBack.shared.exceptions.ConflictException;
import com.Biblioteca.MunicipalBack.shared.exceptions.InvalidCredentialsException;
import com.Biblioteca.MunicipalBack.shared.exceptions.InvalidTokenException;
import com.Biblioteca.MunicipalBack.shared.exceptions.ResourceNotFoundException;
import com.Biblioteca.MunicipalBack.shared.exceptions.TokenRefreshException;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof ResourceNotFoundException e) {
            return GraphqlErrorBuilder.newError(env)
                    .message(e.getMessage())
                    .errorType(ErrorType.DataFetchingException)
                    .extensions(java.util.Map.of("classification", "NOT_FOUND"))
                    .build();
        }
        if (ex instanceof ConflictException e) {
            return GraphqlErrorBuilder.newError(env)
                    .message(e.getMessage())
                    .errorType(ErrorType.ValidationError)
                    .extensions(java.util.Map.of("classification", "CONFLICT"))
                    .build();
        }
        if (ex instanceof BusinessException e) {
            return GraphqlErrorBuilder.newError(env)
                    .message(e.getMessage())
                    .errorType(ErrorType.ValidationError)
                    .extensions(java.util.Map.of("classification", "BAD_REQUEST"))
                    .build();
        }
        if (ex instanceof InvalidCredentialsException e) {
            return GraphqlErrorBuilder.newError(env)
                    .message(e.getMessage())
                    .errorType(ErrorType.ValidationError)
                    .extensions(java.util.Map.of("classification", "UNAUTHORIZED"))
                    .build();
        }
        if (ex instanceof InvalidTokenException e) {
            return GraphqlErrorBuilder.newError(env)
                    .message(e.getMessage())
                    .errorType(ErrorType.ValidationError)
                    .extensions(java.util.Map.of("classification", "UNAUTHORIZED"))
                    .build();
        }
        if (ex instanceof TokenRefreshException e) {
            return GraphqlErrorBuilder.newError(env)
                    .message(e.getMessage())
                    .errorType(ErrorType.ValidationError)
                    .extensions(java.util.Map.of("classification", "UNAUTHORIZED"))
                    .build();
        }
        if (ex instanceof AccessDeniedException) {
            return GraphqlErrorBuilder.newError(env)
                    .message("Access denied")
                    .errorType(ErrorType.ValidationError)
                    .extensions(java.util.Map.of("classification", "FORBIDDEN"))
                    .build();
        }
        if (ex instanceof AuthenticationException) {
            return GraphqlErrorBuilder.newError(env)
                    .message("Authentication required")
                    .errorType(ErrorType.ValidationError)
                    .extensions(java.util.Map.of("classification", "UNAUTHORIZED"))
                    .build();
        }
        if (ex instanceof GraphQlValidationInstrumentation.GraphQlValidationException e) {
            return GraphqlErrorBuilder.newError(env)
                    .message(e.getMessage())
                    .errorType(ErrorType.ValidationError)
                    .extensions(java.util.Map.of("classification", "BAD_REQUEST"))
                    .build();
        }
        return null;
    }
}
