package com.github.kevinarpe.hkpl.web;

import com.googlecode.kevinarpe.papaya.argument.StringArgs;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public final class HkplWebUserCredentials {

    public final String username;
    public final String password;

    public HkplWebUserCredentials(String username, String password) {

        this.username = StringArgs.checkNotEmptyOrWhitespace(username, "username");
        this.password = StringArgs.checkNotEmptyOrWhitespace(password, "password");
    }
}
