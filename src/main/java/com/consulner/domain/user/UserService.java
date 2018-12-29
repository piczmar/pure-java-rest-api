package com.consulner.domain.user;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public String create(NewUser user) {
        return userRepository.create(user);
    }

}
