package com.example.common.utils;

import com.example.common.dto.FilterRequest;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class FilterSpecificationBuilder<T> {
    public Specification<T> build(List<FilterRequest> filters) {
        return (root, query, criteriaBuilder) -> {
            if (filters == null || filters.isEmpty()) {
                return null;
            }

            List<Predicate> predicates = new ArrayList<>();

            for (FilterRequest filter : filters) {
                Path<String> path = root.get(filter.getField());
                String value = filter.getValue();

                switch (filter.getOperator()) {
                    case "eq":
                        predicates.add(criteriaBuilder.equal(path, value));
                        break;
                    case "like":
                        predicates.add(criteriaBuilder.like(path, "%" + value + "%"));
                        break;
                    case "gt":
                        predicates.add(criteriaBuilder.greaterThan(path, value));
                        break;
                    case "lt":
                        predicates.add(criteriaBuilder.lessThan(path, value));
                        break;
                    // Extend more operators as needed
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

