package com.nexgen.sb.creditrisk.service;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

import com.nexgen.sb.creditrisk.model.CreditRiskReqType;
import com.nexgen.sb.creditrisk.model.CreditRiskResType;
import com.nexgen.sb.creditrisk.service.CreditRiskServiceException;

@WebService(name = "CreditRiskSoapSvc",
            targetNamespace = "http://ws.esb.nexgen.com/schema/creditrisk/v1")
public interface CreditRiskSoapSvc {

    @WebMethod(operationName = "assessCreditRisk")
    @WebResult(name = "CreditRiskResponse")
    CreditRiskResType assessCreditRisk(
            @WebParam(name = "CreditRiskRequest") CreditRiskReqType request
    ) throws CreditRiskServiceException;
}
