package com.nexgen.esb.creditrisk.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.CreditRiskResType;

@WebService(name = "CreditRiskSoapSvc",
            targetNamespace = "http://ws.esb.nexgen.com/schema/creditrisk/v1")
public interface CreditRiskSoapSvc {

    @WebMethod(operationName = "assessCreditRisk")
    @WebResult(name = "CreditRiskResponse")
    CreditRiskResType assessCreditRisk(
            @WebParam(name = "CreditRiskRequest") CreditRiskReqType request
    ) throws CreditRiskServiceException;
}
