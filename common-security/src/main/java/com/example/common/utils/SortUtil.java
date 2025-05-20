package com.example.common.utils;

import com.example.common.dto.SortRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

public class SortUtil {
    public static Sort getSort(List<SortRequest> sortRequests) {
        if (sortRequests == null || sortRequests.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = sortRequests.stream()
                .map(req -> new Sort.Order(
                        "desc".equalsIgnoreCase(req.getDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                        req.getField()))
                .collect(Collectors.toList());

        return Sort.by(orders);
    }
}

