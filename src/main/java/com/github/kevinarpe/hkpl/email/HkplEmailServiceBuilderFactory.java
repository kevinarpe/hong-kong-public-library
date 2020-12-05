package com.github.kevinarpe.hkpl.email;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public interface HkplEmailServiceBuilderFactory {

    public static final HkplEmailServiceBuilderFactory INSTANCE = HkplSendEmailServiceBuilderImp::new;

    HkplSendEmailServiceBuilder
    newInstance();
}
