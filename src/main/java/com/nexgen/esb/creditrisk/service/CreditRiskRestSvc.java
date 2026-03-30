package com.nexgen.esb.creditrisk.service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import com.nexgen.esb.creditrisk.model.CreditRiskResType;

@Path("/")
@Api(value = "Credit Risk Assessment REST API")
public class CreditRiskRestSvc {

    @GET
    @Path("assess")
    @Produces(MediaType.APPLICATION_XML)
    @ApiOperation(value = "Assess credit risk for an applicant", notes = "Returns credit risk score and details")
    public CreditRiskResType assessCreditRisk(
            @ApiParam(value = "Applicant ID", required = true) @QueryParam("applicantId") String applicantId,
            @ApiParam(value = "First Name", required = true) @QueryParam("firstName") String firstName,
            @ApiParam(value = "Last Name", required = true) @QueryParam("lastName") String lastName,
            @ApiParam(value = "Date of Birth (YYYY-MM-DD)", required = true) @QueryParam("dateOfBirth") String dateOfBirth,
            @ApiParam(value = "Social Insurance Number", required = true) @QueryParam("sin") String sin,
            @ApiParam(value = "Employment Status") @QueryParam("employmentStatus") String employmentStatus,
            @ApiParam(value = "Annual Income") @QueryParam("annualIncome") Double annualIncome,
            @ApiParam(value = "Province Code") @QueryParam("province") String province,
            @ApiParam(value = "Postal Code") @QueryParam("postalCode") String postalCode,
            @ApiParam(value = "Product Type") @QueryParam("productType") String productType,
            @ApiParam(value = "Requested Amount") @QueryParam("requestedAmount") Double requestedAmount) {
        return null;
    }
}
