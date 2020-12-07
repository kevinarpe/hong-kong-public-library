package com.github.kevinarpe.hkpl.web;

import com.googlecode.kevinarpe.papaya.annotation.Blocking;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.Chrome;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public interface HkplWebLoginService {

    @Blocking
    void doLogin(Chrome chrome, HkplWebUserCredentials userCredentials)
    throws Exception;
}
