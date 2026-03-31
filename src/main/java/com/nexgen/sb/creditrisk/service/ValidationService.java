package com.nexgen.sb.creditrisk.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nexgen.esb.creditrisk.model.CreditRiskReqType;
import com.nexgen.esb.creditrisk.model.ProvinceType;
import com.nexgen.sb.creditrisk.exception.ValidationException;

/**
 * Validates incoming credit risk assessment requests.
 * Migrated from legacy {@code CreditRiskRequestValidator} Camel Processor.
 *
 * Business rules enforced:
 * <ul>
 *   <li>CR-001: ApplicantId required</li>
 *   <li>CR-002: FirstName required</li>
 *   <li>CR-003: LastName required</li>
 *   <li>CR-004: DateOfBirth required and in YYYY-MM-DD format</li>
 *   <li>CR-005: SocialInsuranceNumber required and in NNN-NNN-NNN format</li>
 *   <li>CR-006: Province must be a valid Canadian province code</li>
 *   <li>CR-007: PostalCode format validated when present</li>
 * </ul>
 */
@Service
public class ValidationService {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationService.class);

    private static final String SIN_PATTERN = "^\\d{3}-?\\d{3}-?\\d{3}$";
    private static final String DOB_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final String POSTAL_CODE_PATTERN = "^[A-Za-z]\\d[A-Za-z]\\s?\\d[A-Za-z]\\d$";

    /**
     * Validates the given request, throwing {@link ValidationException} on the first
     * rule violation found.
     *
     * @param request the credit risk request to validate
     * @throws ValidationException if any required field is missing or malformed
     */
    public void validate(CreditRiskReqType request) {
        if (request == null) {
            throw new ValidationException("CR-000", "Request body is null or empty");
        }

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
                ProvinceType.fromValue(request.getProvince().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("CR-006", "Invalid province code: " + request.getProvince());
            }
        }

        if (StringUtils.isNotBlank(request.getPostalCode())
                && !request.getPostalCode().matches(POSTAL_CODE_PATTERN)) {
            throw new ValidationException("CR-007", "Invalid postal code format");
        }

        LOG.info("Credit risk request validated successfully for applicant: {}", request.getApplicantId());
    }
}
