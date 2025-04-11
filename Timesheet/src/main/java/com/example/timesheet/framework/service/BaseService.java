package com.example.timesheet.framework.service;

import com.example.timesheet.Repository.BaseRepository;
import com.example.timesheet.framework.web.utils.PageRequest;
import com.example.timesheet.framework.web.utils.PageWrapper;
import com.example.timesheet.exceptions.InvalidClientDataException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseService extends OrganizationAndTenantAwareService {
    //for creating the boolean expression of the filter
    protected abstract BooleanExpression createBooleanExpression(String property, String operator, String value);
   //if single filter generate 1 boolean expression and handled and if many condition then recursively boolean expression is generated and handled
    private BooleanExpression handleFilter(ArrayNode filter) {
        if (filter.size() == 1) {
            String[] filterParts = filter.get(0).asText().split(":");
            return getBooleanExpression(filterParts[0], filterParts[1], filterParts[2], null, null);
        } else {
            return handleFilterRecursively(filter);
        }
    }
    private BooleanExpression handleFilterRecursively(ArrayNode filter) {
        BooleanExpression expression = null;

        if (filter.size() > 0) {
            String logicalOperator = filter.get(1).asText();

            for (int i = 0; i < filter.size(); i += 2) {
                JsonNode filterToken = filter.get(i);
                if (filterToken.isArray()) {
                    if (expression == null) {
                        expression = handleFilterRecursively((ArrayNode) filterToken);
                    } else {
                        if (logicalOperator.equalsIgnoreCase("and")) {
                            expression = expression.and(handleFilterRecursively((ArrayNode) filterToken));
                        } else {
                            expression = expression.or(handleFilterRecursively((ArrayNode) filterToken));
                        }
                    }
                } else {
                    String[] filterParts = filterToken.asText().split(":");
                    expression = getBooleanExpression(filterParts[0], filterParts[1], filterParts[2], logicalOperator,
                            expression);
                }
            }
        }
        return expression;
    }
  //generate boolean expression for the filter condition
    private BooleanExpression getBooleanExpression(String property, String operator, String value,
                                                   String logicalOperator, BooleanExpression expression) {
        BooleanExpression newBooleanExpression;
        newBooleanExpression = createBooleanExpression(property, operator, value);
        if (expression == null) {
            return newBooleanExpression;
        } else {
            if (logicalOperator.equalsIgnoreCase("and")) {
                return expression.and(newBooleanExpression);
            } else {
                return expression.or(newBooleanExpression);
            }
        }
    }

    protected PageWrapper processPageRequest(PageRequest request, BaseRepository repository) {
        return processPageRequest(request, repository, null);
    }

    protected PageWrapper processPageRequest(PageRequest request, BaseRepository repository,
                                             BooleanExpression additionalExpression) {
        ensurePageRequestIsValid(request);
        BooleanExpression expression = handleFilter(request.getFilter());
        if (additionalExpression != null) {
            if (expression == null) {
                expression = additionalExpression;
            } else {
                expression = expression.and(additionalExpression);
            }

        }
        Pageable pageRequest = getPageable(request);
        Page response = expression == null ? repository.findAll(pageRequest) : repository.findAll(expression, pageRequest);
        return new PageWrapper((PageImpl) response);
    }
    private Pageable getPageable(PageRequest request) {
        int page = request.getPage() != null && request.getPage() - 1 >= 0 ? request.getPage() - 1 : 0;
        Sort sort = getSort(request);
        if (request.getSize() != null) {
            if (request.getSize() < 0) {
                return Pageable.unpaged();
            } else {
                return org.springframework.data.domain.PageRequest.of(page, request.getSize(), sort);
            }
        } else {
            int defaultPageSize = 10;
            return org.springframework.data.domain.PageRequest.of(page, defaultPageSize, sort);
        }
    }
    private Sort getSort(PageRequest request) {
        String[] sort = request.getSort();
        if (sort == null) {
            return Sort.unsorted();
        } else {
            List<Sort.Order> sortOrders = new ArrayList<>();
            for (String sortPart : sort) {
                String sortField = sortPart.split(":")[0].trim();
                String sortDirection = sortPart.split(":")[1].trim();
                Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
                sortOrders.add(new Sort.Order(direction, sortField));
            }
            return Sort.by(sortOrders);
        }
    }

    private void ensurePageRequestIsValid(PageRequest request) {
        String[] sort = request.getSort();
        if (sort != null) {
            for (String sortPart : sort) {
                String[] parts = sortPart.split(":");
                if (parts.length != 2) {
                    throw new InvalidClientDataException("Sort should be in the format ['field:asc|desc']");
                }
                String sortDirection = sortPart.split(":")[1].trim();
                if (!sortDirection.equalsIgnoreCase("ASC") && !sortDirection.equalsIgnoreCase("DESC")) {
                    throw new InvalidClientDataException("Sort direction should be asc or desc");
                }
            }
        }
        ArrayNode filterConditions = request.getFilter();
        if (filterConditions != null && filterConditions.size() > 0 && filterConditions.size() % 2 != 1) {
            throw new InvalidClientDataException("Filter should have odd number of values");
        }
        if (filterConditions != null) {
            validateLogicalOperatorsInFilterCondition(filterConditions);
        }
    }
    private void validateLogicalOperatorsInFilterCondition(ArrayNode filterConditions) {

        for (int i = 0; i < filterConditions.size() - 1; i += 2) {
            String logicalOperator = filterConditions.get(i + 1).asText();
            if (!logicalOperator.equalsIgnoreCase("and") && !(logicalOperator.equalsIgnoreCase("or"))) {
                throw new com.example.timesheet.exceptions.InvalidClientDataException(
                        "Logical operators should be AND or OR case insensitive");
            }
            if (filterConditions.get(i + 2).isArray()) {
                validateLogicalOperatorsInFilterCondition((ArrayNode) filterConditions.get(i + 2));
            }
        }
    }


}
