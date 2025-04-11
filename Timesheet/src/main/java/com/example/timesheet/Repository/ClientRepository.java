package com.example.timesheet.Repository;

import com.example.timesheet.models.Client;
import com.example.timesheet.models.Employee;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ClientRepository extends BaseRepository<Client, String>, PagingAndSortingRepository<Client, String>, QuerydslPredicateExecutor<Client> {
}
