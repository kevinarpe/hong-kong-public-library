package com.github.kevinarpe.hkpl.web;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public interface HkplWebRenewalEmailService {

    void sendMessage(HkplWebRenewalService.Result renewalResult)
    throws Exception;
}
