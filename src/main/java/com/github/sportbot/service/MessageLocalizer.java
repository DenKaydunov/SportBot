package com.github.sportbot.service;

import com.github.sportbot.model.User;

public interface MessageLocalizer {

    String localize(String messageKey, User following);

}
