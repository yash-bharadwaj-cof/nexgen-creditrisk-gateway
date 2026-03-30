package com.nexgen.esb.creditrisk.service;

import javax.jws.WebService;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.CreditRiskResType;

@WebService(endpointInterface = "com.nexgen.esb.creditrisk.service.CreditRiskSoapSvc",
            serviceName = "CreditRiskService",
            targetNamespace = "http://ws.esb.nexgen.com/schema/creditrisk/v1")
public class CreditRiskSoapSvcImpl implements CreditRiskSoapSvc {

    @Override
    public CreditRiskResType assessCreditRisk(CreditRiskReqType request) throws CreditRiskServiceException {
        return null;
    }
}
