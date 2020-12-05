package com.github.kevinarpe.hkpl.args;

import com.beust.jcommander.JCommander;
import com.googlecode.kevinarpe.papaya.annotation.EmptyContainerAllowed;
import com.googlecode.kevinarpe.papaya.annotation.OutputParam;

import javax.annotation.Nullable;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public final class CommandLineArguments {

    private CommandLineArguments() {
        // Empty
    }

    public static void
    parseArgsOrExitOnFailure(String programName,
                             @EmptyContainerAllowed String[] argArr,
                             @OutputParam Args args) {

        final JCommander jCommander = new JCommander(args);
        @Nullable
        Exception nullableException = null;
        try {
            jCommander.parse(argArr);
            args.validate();
        }
        catch (Exception e) {
            nullableException = e;
        }
        if (null != nullableException || args.isHelpRequested()) {

            jCommander.setProgramName(programName);
            jCommander.usage();  // prints to stdout
            if (null != nullableException) {

                System.out.println("-------------------- Exception --------------------");
                nullableException.printStackTrace();
            }
            System.exit(1);  // non-zero
        }
    }
}
