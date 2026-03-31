package com.nexgen.sb.creditrisk.generated;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

@WebService(name = "BureauScoreService",
            targetNamespace = "http://ws.esb.nexgen.com/bureau/v1")
public interface BureauScoreService {

    @WebMethod(operationName = "inquire")
    @WebResult(name = "BureauInquiryResponse")
    BureauInquiryResponse inquire(
            @WebParam(name = "BureauInquiryRequest") BureauInquiryRequest request);
}
