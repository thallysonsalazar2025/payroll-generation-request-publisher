package com.example.payrollpublisher.mappers;

import com.example.payrollpublisher.dto.PayrollGenerationRequest;
import com.example.payrollpublisher.dto.PayrollMessage;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-13T17:30:23-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class PayrollMapperImpl implements PayrollMapper {

    @Override
    public PayrollMessage toMessage(PayrollGenerationRequest request) {
        if ( request == null ) {
            return null;
        }

        String employeeId = null;
        String companyId = null;
        String requesterId = null;
        Integer month = null;
        Integer year = null;

        employeeId = request.employeeId();
        companyId = request.companyId();
        requesterId = request.requesterId();
        month = request.month();
        year = request.year();

        PayrollMessage payrollMessage = new PayrollMessage( employeeId, companyId, requesterId, month, year );

        return payrollMessage;
    }
}
