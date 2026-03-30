package com.nexgen.esb.creditrisk.generated;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService(name = "BureauScoreService",
            targetNamespace = "http://ws.esb.nexgen.com/bureau/v1")
public interface BureauScoreService {

    @WebMethod(operationName = "inquire")
    @WebResult(name = "BureauInquiryResponse")
    BureauInquiryResponse inquire(
            @WebParam(name = "BureauInquiryRequest") BureauInquiryRequest request);
}
