package com.nexgen.esb.creditrisk.service;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.nexgen.esb.creditrisk.model.CreditRiskResType;

@Path("/")
public class CreditRiskRestSvc {

    @GET
    @Path("assess")
    @Produces(MediaType.APPLICATION_XML)
    public CreditRiskResType assessCreditRisk(
            @QueryParam("applicantId") String applicantId,
            @QueryParam("firstName") String firstName,
            @QueryParam("lastName") String lastName,
            @QueryParam("dateOfBirth") String dateOfBirth,
            @QueryParam("sin") String sin,
            @QueryParam("employmentStatus") String employmentStatus,
            @QueryParam("annualIncome") Double annualIncome,
            @QueryParam("province") String province,
            @QueryParam("postalCode") String postalCode,
            @QueryParam("productType") String productType,
            @QueryParam("requestedAmount") Double requestedAmount) {
        return null;
    }
}
