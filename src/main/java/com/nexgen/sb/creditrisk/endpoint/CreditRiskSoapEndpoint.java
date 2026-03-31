package com.nexgen.sb.creditrisk.endpoint;

import jakarta.jws.WebService;

import org.springframework.stereotype.Component;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.CreditRiskResType;
import com.nexgen.esb.creditrisk.service.CreditRiskServiceException;
import com.nexgen.sb.creditrisk.service.CreditRiskOrchestrationService;
import com.nexgen.sb.creditrisk.service.CreditRiskSoapSvc;

@Component
@WebService(endpointInterface = "com.nexgen.sb.creditrisk.service.CreditRiskSoapSvc",
            serviceName = "CreditRiskService",
            targetNamespace = "http://ws.esb.nexgen.com/schema/creditrisk/v1",
            wsdlLocation = "classpath:wsdl/CreditRiskService.wsdl")
public class CreditRiskSoapEndpoint implements CreditRiskSoapSvc {

    private final CreditRiskOrchestrationService orchestrationService;

    public CreditRiskSoapEndpoint(CreditRiskOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @Override
    public CreditRiskResType assessCreditRisk(CreditRiskReqType request) throws CreditRiskServiceException {
        return orchestrationService.processRequest(request, "SOAP");
    }
}
