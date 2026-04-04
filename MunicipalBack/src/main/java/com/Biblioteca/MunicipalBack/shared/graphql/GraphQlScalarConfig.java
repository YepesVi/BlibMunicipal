package com.Biblioteca.MunicipalBack.shared.graphql;

import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.RuntimeWiring;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class GraphQlScalarConfig implements RuntimeWiringConfigurer {

    private static final GraphQLScalarType LONG_SCALAR = GraphQLScalarType.newScalar()
            .name("Long")
            .description("64-bit signed integer (Java Long)")
            .coercing(new Coercing<Long, Long>() {

                @Override
                public Long serialize(Object input) throws CoercingSerializeException {
                    if (input instanceof Long l) return l;
                    if (input instanceof Integer i) return i.longValue();
                    if (input instanceof BigInteger bi) return bi.longValueExact();
                    if (input instanceof String s) {
                        try { return Long.parseLong(s); } catch (NumberFormatException e) {
                            throw new CoercingSerializeException("Cannot coerce to Long: " + s, e);
                        }
                    }
                    throw new CoercingSerializeException("Expected Long but got: " + input.getClass().getName());
                }

                @Override
                public Long parseValue(Object input) throws CoercingParseValueException {
                    if (input instanceof Long l) return l;
                    if (input instanceof Integer i) return i.longValue();
                    if (input instanceof BigInteger bi) return bi.longValueExact();
                    if (input instanceof String s) {
                        try { return Long.parseLong(s); } catch (NumberFormatException e) {
                            throw new CoercingParseValueException("Cannot parse Long: " + s, e);
                        }
                    }
                    throw new CoercingParseValueException("Expected Long input but got: " + input.getClass().getName());
                }

                @Override
                public Long parseLiteral(Object input) throws CoercingParseLiteralException {
                    if (input instanceof IntValue iv) return iv.getValue().longValueExact();
                    if (input instanceof StringValue sv) {
                        try { return Long.parseLong(sv.getValue()); } catch (NumberFormatException e) {
                            throw new CoercingParseLiteralException("Cannot parse Long literal: " + sv.getValue(), e);
                        }
                    }
                    throw new CoercingParseLiteralException("Expected IntValue or StringValue for Long");
                }
            })
            .build();

    private static final GraphQLScalarType DATE_TIME_SCALAR = GraphQLScalarType.newScalar()
            .name("DateTime")
            .description("ISO-8601 date-time scalar. Accepts LocalDateTime (e.g. 2024-01-15T10:30:00) or OffsetDateTime.")
            .coercing(new Coercing<Object, String>() {

                private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

                @Override
                public String serialize(Object input) throws CoercingSerializeException {
                    if (input instanceof LocalDateTime ldt) return FORMATTER.format(ldt);
                    if (input instanceof OffsetDateTime odt) return FORMATTER.format(odt.toLocalDateTime());
                    throw new CoercingSerializeException("Expected LocalDateTime but got: " + input.getClass().getName());
                }

                @Override
                public LocalDateTime parseValue(Object input) throws CoercingParseValueException {
                    try { return LocalDateTime.parse(input.toString(), FORMATTER); }
                    catch (DateTimeParseException e) {
                        throw new CoercingParseValueException("Invalid DateTime: " + input, e);
                    }
                }

                @Override
                public LocalDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
                    if (input instanceof StringValue sv) {
                        try { return LocalDateTime.parse(sv.getValue(), FORMATTER); }
                        catch (DateTimeParseException e) {
                            throw new CoercingParseLiteralException("Invalid DateTime literal: " + sv.getValue(), e);
                        }
                    }
                    throw new CoercingParseLiteralException("Expected StringValue for DateTime");
                }
            })
            .build();

    @Bean
    public MaxQueryDepthInstrumentation maxDepthInstrumentation() {
        return new MaxQueryDepthInstrumentation(15);
    }

    @Override
    public void configure(RuntimeWiring.Builder builder) {
        builder
                .scalar(LONG_SCALAR)
                .scalar(DATE_TIME_SCALAR);
    }
}
