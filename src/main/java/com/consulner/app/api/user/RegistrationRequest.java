package com.consulner.app.api.user;

import lombok.Value;

@Value
class RegistrationRequest {

    String login;
    String password;
}
