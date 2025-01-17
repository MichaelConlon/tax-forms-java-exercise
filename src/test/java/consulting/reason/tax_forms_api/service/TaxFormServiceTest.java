package consulting.reason.tax_forms_api.service;

import consulting.reason.tax_forms_api.AbstractServiceTest;
import consulting.reason.tax_forms_api.dto.TaxFormDetailsDto;
import consulting.reason.tax_forms_api.dto.TaxFormDto;
import consulting.reason.tax_forms_api.dto.request.TaxFormDetailsRequest;
import consulting.reason.tax_forms_api.entity.TaxForm;
import consulting.reason.tax_forms_api.enums.TaxFormHistoryStatus;
import consulting.reason.tax_forms_api.enums.TaxFormStatus;
import consulting.reason.tax_forms_api.exception.TaxFormStatusException;
import consulting.reason.tax_forms_api.repository.TaxFormHistoryRepository;
import consulting.reason.tax_forms_api.repository.TaxFormRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TaxFormServiceTest extends AbstractServiceTest {
    @Autowired
    private TaxFormRepository taxFormRepository;
    @Autowired
    private TaxFormHistoryRepository taxFormHistoryRepository;
    private TaxFormService taxFormService;
    private TaxForm taxForm;
    private TaxFormDto taxFormDto;
    private final TaxFormDetailsRequest taxFormDetailsRequest = TaxFormDetailsRequest.builder()
            .ratio(0.5)
            .assessedValue(100)
            .appraisedValue(200L)
            .comments("Testing")
            .build();

    @BeforeEach
    void before() {
        taxFormService = new TaxFormServiceImpl(
                taxFormHistoryRepository,
                taxFormRepository,
                modelMapper
        );

        taxForm = taxFormRepository.save(TaxForm.builder()
                .formName("Test Form 1")
                .formYear(2024)
                .status(TaxFormStatus.NOT_STARTED)
                .build());
        taxFormDto = modelMapper.map(taxForm, TaxFormDto.class);
    }

    @Test
    void testFindAll() {
        assertThat(taxFormService.findAllByYear(2024)).containsExactly(taxFormDto);
        assertThat(taxFormService.findAllByYear(2025)).isEmpty();
    }

    @Test
    void testFindById() {
        assertThat(taxFormService.findById(taxForm.getId())).isEqualTo(Optional.of(taxFormDto));
        assertThat(taxFormService.findById(0)).isEmpty();
    }

    @Test
    void testSave() {
        TaxFormDetailsDto taxFormDetailsDto = TaxFormDetailsDto.builder()
                .ratio(0.5)
                .assessedValue(100)
                .appraisedValue(200L)
                .comments("Testing")
                .build();

        Optional<TaxFormDto> taxFormDto1 = taxFormService.save(taxForm.getId(), taxFormDetailsRequest);
        assertThat(taxFormDto1).isPresent();
        assertThat(taxFormDto1.get().getDetails()).isEqualTo(taxFormDetailsDto);

        assertThat(taxFormService.save(0, taxFormDetailsRequest)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = TaxFormStatus.class, names = {
            "SUBMITTED",
            "ACCEPTED"
    })
    void testSaveHandlesInvalidStatus(TaxFormStatus taxFormStatus) {
        taxForm.setStatus(taxFormStatus);

        TaxFormStatusException taxFormStatusException = new TaxFormStatusException(
                taxForm,
                TaxFormStatus.IN_PROGRESS
        );

        assertThatThrownBy(() -> taxFormService.save(taxForm.getId(), taxFormDetailsRequest))
                .isInstanceOf(TaxFormStatusException.class)
                .hasMessage(taxFormStatusException.getMessage());
    }

    //
    // SUBMIT FROM
    //
    @ParameterizedTest
    @EnumSource(value = TaxFormStatus.class, names = {
            "NOT_STARTED",
            "SUBMITTED",
            "ACCEPTED",
            "RETURNED"
    })
    void testSubmitHandlesInvalidStatus(TaxFormStatus taxFormStatus) {
        taxForm.setStatus(taxFormStatus);

        TaxFormStatusException taxFormStatusException = new TaxFormStatusException(
                taxForm,
                TaxFormStatus.SUBMITTED
        );

        assertThatThrownBy(() -> taxFormService.submit(taxForm.getId()))
                .isInstanceOf(TaxFormStatusException.class)
                .hasMessage(taxFormStatusException.getMessage());
    }

    @Test
    void testSubmitSuccess() {
        taxForm.setStatus(TaxFormStatus.IN_PROGRESS);
        
        Optional<TaxFormDto> result = taxFormService.submit(taxForm.getId());
        
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TaxFormStatus.SUBMITTED);

        // Check that the history was created
        assertThat(taxFormHistoryRepository.findByTaxFormId(taxForm.getId())).isPresent();
        assertThat(taxFormHistoryRepository.findByTaxFormId(taxForm.getId()).get().getStatus()).isEqualTo(TaxFormHistoryStatus.SUBMITTED);
        assertThat(taxFormRepository.findById(taxForm.getId()).get().getStatus())
                .isEqualTo(TaxFormStatus.SUBMITTED);
    }

    @Test
    void testSubmitNotFound() {
        assertThat(taxFormService.submit(0)).isEmpty();
    }

    //
    // RETURN FROM
    //
    @ParameterizedTest
    @EnumSource(value = TaxFormStatus.class, names = {
            "NOT_STARTED",
            "IN_PROGRESS",
            "RETURNED",
            "ACCEPTED"
    })
    void testReturnFormHandlesInvalidStatus(TaxFormStatus taxFormStatus) {
        taxForm.setStatus(taxFormStatus);

        TaxFormStatusException taxFormStatusException = new TaxFormStatusException(
                taxForm,
                TaxFormStatus.RETURNED
        );

        assertThatThrownBy(() -> taxFormService.returnForm(taxForm.getId()))
                .isInstanceOf(TaxFormStatusException.class)
                .hasMessage(taxFormStatusException.getMessage());
    }

    @Test
    void testReturnFormSuccess() {
        taxForm.setStatus(TaxFormStatus.SUBMITTED);
        
        Optional<TaxFormDto> result = taxFormService.returnForm(taxForm.getId());
        
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TaxFormStatus.RETURNED);

        // Check that the history was created
        assertThat(taxFormHistoryRepository.findByTaxFormId(taxForm.getId())).isPresent();
        assertThat(taxFormHistoryRepository.findByTaxFormId(taxForm.getId()).get().getStatus())
                .isEqualTo(TaxFormHistoryStatus.RETURNED);
        assertThat(taxFormRepository.findById(taxForm.getId()).get().getStatus())
                .isEqualTo(TaxFormStatus.RETURNED);
    }

    @Test
    void testReturnFormNotFound() {
        assertThat(taxFormService.returnForm(0)).isEmpty();
    }

    //
    // ACCEPT FORM
    //
    @ParameterizedTest
    @EnumSource(value = TaxFormStatus.class, names = {
            "NOT_STARTED",
            "IN_PROGRESS",
            "RETURNED",
            "ACCEPTED"
    })
    void testAcceptFormHandlesInvalidStatus(TaxFormStatus taxFormStatus) {
        taxForm.setStatus(taxFormStatus);

        TaxFormStatusException taxFormStatusException = new TaxFormStatusException(
                taxForm,
                TaxFormStatus.ACCEPTED
        );

        assertThatThrownBy(() -> taxFormService.accept(taxForm.getId()))
                .isInstanceOf(TaxFormStatusException.class)
                .hasMessage(taxFormStatusException.getMessage());
    }

    @Test
    void testAcceptFormSuccess() {
        taxForm.setStatus(TaxFormStatus.SUBMITTED);
        
        Optional<TaxFormDto> result = taxFormService.accept(taxForm.getId());
        
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TaxFormStatus.ACCEPTED);

        // Check that the history was created
        assertThat(taxFormHistoryRepository.findByTaxFormId(taxForm.getId())).isPresent();
        assertThat(taxFormHistoryRepository.findByTaxFormId(taxForm.getId()).get().getStatus())
                .isEqualTo(TaxFormHistoryStatus.ACCEPTED);
        assertThat(taxFormRepository.findById(taxForm.getId()).get().getStatus())
                .isEqualTo(TaxFormStatus.ACCEPTED);
    }

    @Test
    void testAcceptFormNotFound() {
        assertThat(taxFormService.accept(0)).isEmpty();
    }
}
