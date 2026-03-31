package com.nexgen.sb.creditrisk.controller;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.CreditRiskResType;
import com.nexgen.esb.creditrisk.model.RequestHeader;
import com.nexgen.sb.creditrisk.service.CreditRiskOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

/**
 * Spring MVC REST controller for credit risk assessment.
 * Replaces the legacy CXF JAX-RS {@code CreditRiskRestSvc}.
 *
 * <p>Base path (with context-path): {@code /nexgen/api/v1/creditrisk}</p>
 * <p>OpenAPI spec available at: {@code /nexgen/v3/api-docs}</p>
 */
@RestController
@RequestMapping("/api/v1/creditrisk")
@Tag(name = "Credit Risk Assessment", description = "Credit Risk Assessment REST API")
public class CreditRiskController {

    private static final String SOURCE_SYSTEM = "REST";

    private final CreditRiskOrchestrationService orchestrationService;

    public CreditRiskController(CreditRiskOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    /**
     * Assess credit risk for an applicant.
     *
     * <p>Example: {@code GET /nexgen/api/v1/creditrisk/assess?applicantId=APP001&firstName=John
     * &lastName=Doe&dateOfBirth=1990-01-15&sin=123-456-789}</p>
     *
     * @param applicantId          unique applicant identifier (required)
     * @param firstName            applicant first name (required)
     * @param lastName             applicant last name (required)
     * @param dateOfBirth          date of birth in YYYY-MM-DD format (required)
     * @param socialInsuranceNumber Social Insurance Number (required, param name "sin")
     * @param employmentStatus     employment status (optional)
     * @param annualIncome         annual income in CAD (optional)
     * @param province             two-letter province code, e.g. ON, BC (optional)
     * @param postalCode           Canadian postal code (optional)
     * @param productType          product type being applied for (optional)
     * @param requestedAmount      requested loan/credit amount (optional)
     * @return credit risk assessment response (XML or JSON depending on Accept header)
     */
    @Operation(summary = "Assess credit risk for an applicant",
               description = "Returns a credit risk score, category, and detailed breakdown for the given applicant")
    @GetMapping(value = "/assess",
                produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CreditRiskResType> assessCreditRisk(
            @Parameter(description = "Unique applicant identifier", required = true)
            @RequestParam String applicantId,

            @Parameter(description = "Applicant first name", required = true)
            @RequestParam String firstName,

            @Parameter(description = "Applicant last name", required = true)
            @RequestParam String lastName,

            @Parameter(description = "Date of birth (YYYY-MM-DD)", required = true)
            @RequestParam String dateOfBirth,

            @Parameter(description = "Social Insurance Number", required = true)
            @RequestParam("sin") String socialInsuranceNumber,

            @Parameter(description = "Employment status (e.g. EMPLOYED, SELF_EMPLOYED, UNEMPLOYED)")
            @RequestParam(required = false) String employmentStatus,

            @Parameter(description = "Annual income in CAD")
            @RequestParam(required = false) Double annualIncome,

            @Parameter(description = "Two-letter Canadian province code (e.g. ON, BC, AB)")
            @RequestParam(required = false) String province,

            @Parameter(description = "Canadian postal code")
            @RequestParam(required = false) String postalCode,

            @Parameter(description = "Product type (e.g. MORTGAGE, AUTO_LOAN, PERSONAL_LOAN)")
            @RequestParam(required = false) String productType,

            @Parameter(description = "Requested credit/loan amount in CAD")
            @RequestParam(required = false) Double requestedAmount) {

        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setTransactionId(UUID.randomUUID().toString());
        requestHeader.setTimestamp(Instant.now().toString());
        requestHeader.setSourceSystem(SOURCE_SYSTEM);

        CreditRiskReqType request = new CreditRiskReqType();
        request.setRequestHeader(requestHeader);
        request.setApplicantId(applicantId);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setDateOfBirth(dateOfBirth);
        request.setSocialInsuranceNumber(socialInsuranceNumber);
        request.setEmploymentStatus(employmentStatus);
        request.setAnnualIncome(annualIncome);
        request.setProvince(province);
        request.setPostalCode(postalCode);
        request.setProductType(productType);
        request.setRequestedAmount(requestedAmount);
        request.setRequestChannel(SOURCE_SYSTEM);

        CreditRiskResType response = orchestrationService.processRequest(request, SOURCE_SYSTEM);
        return ResponseEntity.ok(response);
    }
}
