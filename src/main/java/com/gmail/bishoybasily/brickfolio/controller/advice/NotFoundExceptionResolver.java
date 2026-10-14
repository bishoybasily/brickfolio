package com.gmail.bishoybasily.brickfolio.controller.advice;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.persistence.EntityNotFoundException;

import org.jspecify.annotations.Nullable;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class NotFoundExceptionResolver extends DataFetcherExceptionResolverAdapter {

  @Override
  protected @Nullable GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
    if (ex instanceof EntityNotFoundException)
      return GraphqlErrorBuilder.newError(env)
          .errorType(ErrorType.NOT_FOUND)
          .message(ex.getMessage())
          .build();
    return null;
  }

}
