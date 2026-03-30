package com.nexgen.esb.creditrisk.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.logging.LoggerConstants;

/**
 * Validates incoming credit risk assessment requests.
 * Business Rules:
 * - CR-001: ApplicantId required
 * - CR-002: FirstName required
 * - CR-003: LastName required
 * - CR-004: DateOfBirth required and format validated
 * - CR-005: SocialInsuranceNumber required and format validated
 * - CR-006: Province must be valid Canadian province code
 */
public class CreditRiskRequestValidator implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(CreditRiskRequestValidator.class);

    private static final String SIN_PATTERN = "^\\d{3}-?\\d{3}-?\\d{3}$";
    private static final String DOB_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final String POSTAL_CODE_PATTERN = "^[A-Za-z]\\d[A-Za-z]\\s?\\d[A-Za-z]\\d$";

    @Override
    public void process(Exchange exchange) throws Exception {
        Message inMessage = exchange.getIn();
        CreditRiskReqType request = inMessage.getBody(CreditRiskReqType.class);

        if (request == null) {
            throw new ValidationException("CR-000", "Request body is null or empty");
        }

        populateLoggingProperties(exchange, request);

        if (StringUtils.isBlank(request.getApplicantId())) {
            throw new ValidationException("CR-001", "Applicant ID field is blank");
        }

        if (StringUtils.isBlank(request.getFirstName())) {
            throw new ValidationException("CR-002", "First Name field is blank");
        }

        if (StringUtils.isBlank(request.getLastName())) {
            throw new ValidationException("CR-003", "Last Name field is blank");
        }

        if (StringUtils.isBlank(request.getDateOfBirth())) {
            throw new ValidationException("CR-004", "Date of Birth field is blank");
        }

        if (!request.getDateOfBirth().matches(DOB_PATTERN)) {
            throw new ValidationException("CR-004", "Date of Birth must be in YYYY-MM-DD format");
        }

        if (StringUtils.isBlank(request.getSocialInsuranceNumber())) {
            throw new ValidationException("CR-005", "Social Insurance Number field is blank");
        }

        if (!request.getSocialInsuranceNumber().matches(SIN_PATTERN)) {
            throw new ValidationException("CR-005", "Social Insurance Number must be in NNN-NNN-NNN format");
        }

        if (StringUtils.isNotBlank(request.getProvince())) {
            try {
                com.nexgen.esb.creditrisk.model.ProvinceType.fromValue(request.getProvince().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("CR-006", "Invalid province code: " + request.getProvince());
            }
        }

        if (StringUtils.isNotBlank(request.getPostalCode()) &&
                !request.getPostalCode().matches(POSTAL_CODE_PATTERN)) {
            throw new ValidationException("CR-007", "Invalid postal code format");
        }

        LOG.info("Credit risk request validated successfully for applicant: {}", request.getApplicantId());
        exchange.setProperty("VALIDATED_REQUEST", request);
    }

    private void populateLoggingProperties(Exchange exchange, CreditRiskReqType request) {
        exchange.setProperty(LoggerConstants.LOG_APPLICANT_ID, request.getApplicantId());
        exchange.setProperty(LoggerConstants.LOG_PROVINCE, request.getProvince());
        exchange.setProperty(LoggerConstants.LOG_PRODUCT_TYPE, request.getProductType());
        exchange.setProperty(LoggerConstants.LOG_REQUEST_CHANNEL, request.getRequestChannel());
        if (request.getRequestHeader() != null) {
            exchange.setProperty(LoggerConstants.LOG_TRANSACTION_ID, request.getRequestHeader().getTransactionId());
            exchange.setProperty(LoggerConstants.LOG_SOURCE_SYSTEM, request.getRequestHeader().getSourceSystem());
        }
    }

    public static class ValidationException extends Exception {
        private static final long serialVersionUID = 1L;
        private final String errorCode;

        public ValidationException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() { return errorCode; }
    }
}
