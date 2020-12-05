package com.github.kevinarpe.hkpl.web;

import com.googlecode.kevinarpe.papaya.argument.ObjectArgs;
import com.googlecode.kevinarpe.papaya.java_mail.TextMimeSubType;
import com.github.kevinarpe.hkpl.email.HkplSendEmailService;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
// Scope: Global singleton
public final class HkplWebRenewalEmailServiceImp
implements HkplWebRenewalEmailService {

    private final HkplSendEmailService hkplSendEmailService;

    public HkplWebRenewalEmailServiceImp(HkplSendEmailService hkplSendEmailService) {

        this.hkplSendEmailService = ObjectArgs.checkNotNull(hkplSendEmailService, "hkplSendEmailService");
    }

    @Override
    public void
    sendMessage(HkplWebRenewalService.Result renewalResult)
    throws Exception {

        if (renewalResult.noMoreRenewalsAllowedRowList.isEmpty() && renewalResult.renewedRowList.isEmpty()) {
            return;
        }

        final StringBuilder sb = new StringBuilder();

        if (renewalResult.noMoreRenewalsAllowedRowList.size() > 0) {

            sb.append(String.format("No More Renewals Allowed%n"));
            sb.append(String.format("------------------------%n"));

            for (final HkplWebCheckedOutService.Result.Row row : renewalResult.noMoreRenewalsAllowedRowList) {

                sb.append(String.format("Due: %s: %s%n", row.dueLocalDate, row.title));
            }
            sb.append(String.format("%n"));
        }

        if (renewalResult.renewedRowList.size() > 0) {

            sb.append(String.format("Renewals Completed%n"));
            sb.append(String.format("------------------%n"));

            for (final HkplWebCheckedOutService.Result.Row row : renewalResult.renewedRowList) {

                sb.append(String.format("%s%n", row.title));
            }
        }

        final String bodyText = sb.toString();
        hkplSendEmailService.sendMessage("Renewal Results", TextMimeSubType.PLAIN, bodyText);
    }
}
