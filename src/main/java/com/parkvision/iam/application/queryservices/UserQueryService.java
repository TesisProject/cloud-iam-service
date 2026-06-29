package com.parkvision.iam.application.queryservices;

import com.parkvision.iam.domain.model.aggregates.User;
import com.parkvision.iam.domain.queries.GetAllUsersQuery;
import com.parkvision.iam.domain.queries.GetUserQuery;
import com.parkvision.iam.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;

    public Optional<User> handle(GetUserQuery query) {
        return userRepository.findById(query.userId());
    }

    public List<User> handle(GetAllUsersQuery query) {
        return userRepository.findAll();
    }
}
