package com.github.kevinarpe.hkpl.web;

import com.googlecode.kevinarpe.papaya.annotation.Blocking;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.Chrome;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public interface HkplWebLoginService {

    @Blocking
    boolean tryDoLogin(Chrome chrome,
                       HkplWebUserCredentials userCredentials)
    throws Exception;
}
