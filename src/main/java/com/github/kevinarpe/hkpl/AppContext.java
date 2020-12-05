package com.github.kevinarpe.hkpl;

import com.googlecode.kevinarpe.papaya.container.ImmutableFullEnumMap;
import com.googlecode.kevinarpe.papaya.function.retry.RetryStrategyFactory;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeLauncherService;
import com.github.kevinarpe.hkpl.web.HkplWebCheckedOutService;
import com.github.kevinarpe.hkpl.web.HkplWebLoginService;
import com.github.kevinarpe.hkpl.web.HkplWebRenewalEmailService;
import com.github.kevinarpe.hkpl.web.HkplWebRenewalService;
import com.github.kevinarpe.hkpl.web.RetryStrategyType;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public interface AppContext {

    ChromeLauncherService
    getChromeLauncherService();

    HkplWebLoginService
    getHkplWebLoginService();

    ImmutableFullEnumMap<RetryStrategyType, RetryStrategyFactory>
    getRetryStrategyMap();

    HkplWebCheckedOutService
    getHkplWebCheckedOutService();

    HkplWebRenewalService
    getHkplWebRenewalService();

    HkplWebRenewalEmailService
    getHkplWebRenewalEmailService();
}
