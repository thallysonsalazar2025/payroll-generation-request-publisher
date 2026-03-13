package com.example.payrollpublisher.mappers;

import com.example.payrollpublisher.dto.PayrollGenerationRequest;
import com.example.payrollpublisher.dto.PayrollMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PayrollMapper {
    PayrollMessage toMessage(PayrollGenerationRequest request);
}
