package com.example.timesheet.framework.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;

@Getter
@Setter
@Service
public abstract class OrganizationAndTenantAwareService {

    @Autowired
    private EntityManager entityManager;
}

