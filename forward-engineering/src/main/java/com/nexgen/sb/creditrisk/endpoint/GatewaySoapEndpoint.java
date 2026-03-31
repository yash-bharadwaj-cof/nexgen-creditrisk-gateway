package com.nexgen.sb.creditrisk.endpoint;

import jakarta.jws.WebService;

import org.springframework.stereotype.Component;

import com.nexgen.sb.creditrisk.model.CreditRiskReqType;
import com.nexgen.sb.creditrisk.model.CreditRiskResType;
import com.nexgen.sb.creditrisk.service.CreditRiskServiceException;
import com.nexgen.sb.creditrisk.service.CreditRiskOrchestrationService;
import com.nexgen.sb.creditrisk.service.CreditRiskSoapSvc;
import com.nexgen.sb.creditrisk.service.GatewayPreProcessService;

@Component
@WebService(endpointInterface = "com.nexgen.sb.creditrisk.service.CreditRiskSoapSvc",
            serviceName = "CreditRiskService",
            targetNamespace = "http://ws.esb.nexgen.com/schema/creditrisk/v1",
            wsdlLocation = "classpath:wsdl/CreditRiskService.wsdl")
public class GatewaySoapEndpoint implements CreditRiskSoapSvc {

    private final CreditRiskOrchestrationService orchestrationService;
    private final GatewayPreProcessService gatewayPreProcessService;

    public GatewaySoapEndpoint(CreditRiskOrchestrationService orchestrationService,
                               GatewayPreProcessService gatewayPreProcessService) {
        this.orchestrationService = orchestrationService;
        this.gatewayPreProcessService = gatewayPreProcessService;
    }

    @Override
    public CreditRiskResType assessCreditRisk(CreditRiskReqType request) throws CreditRiskServiceException {
        CreditRiskReqType processedRequest = gatewayPreProcessService.preProcess(request);
        return orchestrationService.processRequest(processedRequest, "SOAP");
    }
}
