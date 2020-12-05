package com.github.kevinarpe.hkpl.args;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public interface Args {

    boolean isHelpRequested();

    void validate();
}
