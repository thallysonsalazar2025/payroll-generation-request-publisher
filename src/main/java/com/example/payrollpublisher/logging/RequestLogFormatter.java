package com.example.payrollpublisher.logging;

import com.example.payrollpublisher.dto.PayrollGenerationRequest;

/**
 * Helpers to build concise, sanitized log messages for requests.
 */
public final class RequestLogFormatter {

    private RequestLogFormatter() {
    }

    public static String summarize(PayrollGenerationRequest request) {
        if (request == null) {
            return "request=<null>";
        }

        return "employeeId=%s companyId=%s requesterId=%s month=%s year=%s"
                .formatted(
                        SensitiveDataSanitizer.mask(request.employeeId()),
                        SensitiveDataSanitizer.mask(request.companyId()),
                        SensitiveDataSanitizer.mask(request.requesterId()),
                        request.month(),
                        request.year());
    }
}
