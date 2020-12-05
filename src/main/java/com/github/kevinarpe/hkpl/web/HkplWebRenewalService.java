package com.github.kevinarpe.hkpl.web;

import com.google.common.collect.ImmutableList;
import com.googlecode.kevinarpe.papaya.annotation.EmptyContainerAllowed;
import com.googlecode.kevinarpe.papaya.argument.ObjectArgs;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.Chrome;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public interface HkplWebRenewalService {

    Result renew(Chrome chrome,
                 HkplWebCheckedOutService.Result checkedOut,
                 int daysBeforeLastDay)
    throws Exception;

    public static final class Result {

        @EmptyContainerAllowed
        public final ImmutableList<HkplWebCheckedOutService.Result.Row> noMoreRenewalsAllowedRowList;
        @EmptyContainerAllowed
        public final ImmutableList<HkplWebCheckedOutService.Result.Row> renewedRowList;

        public Result(@EmptyContainerAllowed
                      ImmutableList<HkplWebCheckedOutService.Result.Row> noMoreRenewalsAllowedRowList,
                      @EmptyContainerAllowed
                      ImmutableList<HkplWebCheckedOutService.Result.Row> renewedRowList) {

            this.noMoreRenewalsAllowedRowList =
                ObjectArgs.checkNotNull(noMoreRenewalsAllowedRowList, "noMoreRenewalsAllowedRowList");

            this.renewedRowList = ObjectArgs.checkNotNull(renewedRowList, "renewedRowList");
        }
    }
}
