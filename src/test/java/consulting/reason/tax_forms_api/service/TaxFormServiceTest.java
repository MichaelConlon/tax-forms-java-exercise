package consulting.reason.tax_forms_api.service;

import consulting.reason.tax_forms_api.AbstractServiceTest;
import consulting.reason.tax_forms_api.dto.TaxFormDetailsDto;
import consulting.reason.tax_forms_api.dto.TaxFormDto;
import consulting.reason.tax_forms_api.dto.request.TaxFormDetailsRequest;
import consulting.reason.tax_forms_api.entity.TaxForm;
import consulting.reason.tax_forms_api.entity.TaxFormHistory;
import consulting.reason.tax_forms_api.enums.TaxFormHistoryStatus;
import consulting.reason.tax_forms_api.enums.TaxFormStatus;
import consulting.reason.tax_forms_api.exception.TaxFormStatusException;
import consulting.reason.tax_forms_api.repository.TaxFormRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TaxFormServiceTest extends AbstractServiceTest {
    @Autowired
    private TaxFormRepository taxFormRepository;
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
        assertThat(result.get().getHistory().size()).isEqualTo(1);
        assertThat(result.get().getHistory().get(0).getStatus()).isEqualTo(TaxFormHistoryStatus.SUBMITTED);

        TaxForm taxResult = taxFormRepository.findById(taxForm.getId()).get();
        assertThat(taxResult.getStatus()).isEqualTo(TaxFormStatus.SUBMITTED);

        // Check that the history was created
        List<TaxFormHistory> taxHistoryResult = taxResult.getHistory();
        assertThat(taxHistoryResult.size()).isEqualTo(1);
        assertThat(taxHistoryResult.get(0).getStatus()).isEqualTo(TaxFormHistoryStatus.SUBMITTED);
        
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
        assertThat(result.get().getHistory().size()).isEqualTo(1);
        assertThat(result.get().getHistory().get(0).getStatus()).isEqualTo(TaxFormHistoryStatus.RETURNED);

        TaxForm taxResult = taxFormRepository.findById(taxForm.getId()).get();
        assertThat(taxResult.getStatus()).isEqualTo(TaxFormStatus.RETURNED);

        // Check that the history was created
        List<TaxFormHistory> taxHistoryResult = taxResult.getHistory();
        assertThat(taxHistoryResult.size()).isEqualTo(1);
        assertThat(taxHistoryResult.get(0).getStatus()).isEqualTo(TaxFormHistoryStatus.RETURNED);
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
        
        // Check Return values
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TaxFormStatus.ACCEPTED);
        assertThat(result.get().getHistory().size()).isEqualTo(1);
        assertThat(result.get().getHistory().get(0).getStatus()).isEqualTo(TaxFormHistoryStatus.ACCEPTED);

        // Check DB was updated
        TaxForm taxResult = taxFormRepository.findById(taxForm.getId()).get();
        assertThat(taxResult.getStatus()).isEqualTo(TaxFormStatus.ACCEPTED);

        // Check that the history was created
        List<TaxFormHistory> taxHistoryResult = taxResult.getHistory();
        assertThat(taxHistoryResult.size()).isEqualTo(1);
        assertThat(taxHistoryResult.get(0).getStatus()).isEqualTo(TaxFormHistoryStatus.ACCEPTED);
    }

    @Test
    void testSubmitAndReturn() {
        taxForm.setStatus(TaxFormStatus.IN_PROGRESS);
        
        Optional<TaxFormDto> result = taxFormService.submit(taxForm.getId());
        
        // Check Return values
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TaxFormStatus.SUBMITTED);
        assertThat(result.get().getHistory().size()).isEqualTo(1);
        assertThat(result.get().getHistory().get(0).getStatus()).isEqualTo(TaxFormHistoryStatus.SUBMITTED);

        result = taxFormService.returnForm(taxForm.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TaxFormStatus.RETURNED);
        assertThat(result.get().getHistory().size()).isEqualTo(2);
        assertThat(result.get().getHistory().get(1).getStatus()).isEqualTo(TaxFormHistoryStatus.RETURNED);
    }

    @Test
    void testSubmitAndAccept() {
        taxForm.setStatus(TaxFormStatus.IN_PROGRESS);
        
        Optional<TaxFormDto> result = taxFormService.submit(taxForm.getId());
        
        // Check Return values
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TaxFormStatus.SUBMITTED);
        assertThat(result.get().getHistory().size()).isEqualTo(1);
        assertThat(result.get().getHistory().get(0).getStatus()).isEqualTo(TaxFormHistoryStatus.SUBMITTED);

        result = taxFormService.accept(taxForm.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TaxFormStatus.ACCEPTED);
        assertThat(result.get().getHistory().size()).isEqualTo(2);
        assertThat(result.get().getHistory().get(1).getStatus()).isEqualTo(TaxFormHistoryStatus.ACCEPTED);
    }

    @Test
    void testSubmitAndReturnAndAccept() {
        // Set initial status
        taxForm.setStatus(TaxFormStatus.IN_PROGRESS);
        
        // Submit
        Optional<TaxFormDto> result = taxFormService.submit(taxForm.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TaxFormStatus.SUBMITTED);
        assertThat(result.get().getHistory().size()).isEqualTo(1);
        assertThat(result.get().getHistory().get(0).getStatus()).isEqualTo(TaxFormHistoryStatus.SUBMITTED);

        // Return
        result = taxFormService.returnForm(taxForm.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TaxFormStatus.RETURNED);
        assertThat(result.get().getHistory().size()).isEqualTo(2);
        assertThat(result.get().getHistory().get(1).getStatus()).isEqualTo(TaxFormHistoryStatus.RETURNED);

        // Save - Make changes (in theory)
        result = taxFormService.save(taxForm.getId(), taxFormDetailsRequest);
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TaxFormStatus.IN_PROGRESS);
        assertThat(result.get().getHistory().size()).isEqualTo(2); // Save doesn't update history
        assertThat(result.get().getHistory().get(1).getStatus()).isEqualTo(TaxFormHistoryStatus.RETURNED);

        // Submit Again
        result = taxFormService.submit(taxForm.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TaxFormStatus.SUBMITTED);
        assertThat(result.get().getHistory().size()).isEqualTo(3);
        assertThat(result.get().getHistory().get(2).getStatus()).isEqualTo(TaxFormHistoryStatus.SUBMITTED);

        // Accept
        result = taxFormService.accept(taxForm.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TaxFormStatus.ACCEPTED);
        assertThat(result.get().getHistory().size()).isEqualTo(4);
        assertThat(result.get().getHistory().get(3).getStatus()).isEqualTo(TaxFormHistoryStatus.ACCEPTED);
    }

    @Test
    void testAcceptFormNotFound() {
        assertThat(taxFormService.accept(0)).isEmpty();
    }
}
