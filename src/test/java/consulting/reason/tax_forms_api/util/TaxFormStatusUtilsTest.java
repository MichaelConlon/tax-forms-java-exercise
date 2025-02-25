package consulting.reason.tax_forms_api.util;

import consulting.reason.tax_forms_api.entity.TaxForm;
import consulting.reason.tax_forms_api.enums.TaxFormStatus;
import consulting.reason.tax_forms_api.exception.TaxFormStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TaxFormStatusUtilsTest {
    private TaxForm taxForm;

    @BeforeEach
    void before() {
        taxForm = TaxForm.builder()
                .id(1)
                .formName("Test Tax Form")
                .formYear(2024)
                .status(TaxFormStatus.NOT_STARTED)
                .build();
    }

    @ParameterizedTest
    @EnumSource(value = TaxFormStatus.class, names = {
            "NOT_STARTED",
            "IN_PROGRESS"
    })
    void testSavePermitted(TaxFormStatus taxFormStatus) {
        taxForm.setStatus(taxFormStatus);
        TaxFormStatusUtils.save(taxForm);
        assertThat(taxForm.getStatus()).isEqualTo(TaxFormStatus.IN_PROGRESS);
    }

    @ParameterizedTest
    @EnumSource(value = TaxFormStatus.class, names = {
            "SUBMITTED",
            "ACCEPTED"
    })
    void testSaveNotPermitted(TaxFormStatus taxFormStatus) {
        taxForm.setStatus(taxFormStatus);
        TaxFormStatusException taxFormStatusException = new TaxFormStatusException(
                taxForm,
                TaxFormStatus.IN_PROGRESS
        );

        assertThatThrownBy(() -> TaxFormStatusUtils.save(taxForm))
                .isInstanceOf(TaxFormStatusException.class)
                .hasMessage(taxFormStatusException.getMessage());
    }

    @Test
    void testSubmitPermitted() {
        taxForm.setStatus(TaxFormStatus.IN_PROGRESS);
        TaxFormStatusUtils.submit(taxForm);
        assertThat(taxForm.getStatus()).isEqualTo(TaxFormStatus.SUBMITTED);
    }

    @ParameterizedTest
    @EnumSource(value = TaxFormStatus.class, names = {
            "NOT_STARTED",
            "SUBMITTED",
            "ACCEPTED",
            "RETURNED"
    })
    void testSubmitNotPermitted(TaxFormStatus taxFormStatus) {
        taxForm.setStatus(taxFormStatus);
        TaxFormStatusException taxFormStatusException = new TaxFormStatusException(
                taxForm,
                TaxFormStatus.SUBMITTED
        );

        assertThatThrownBy(() -> TaxFormStatusUtils.submit(taxForm))
                .isInstanceOf(TaxFormStatusException.class)
                .hasMessage(taxFormStatusException.getMessage());
    }

    @Test
    void testReturnFormPermitted() {
        taxForm.setStatus(TaxFormStatus.SUBMITTED);
        TaxFormStatusUtils.returnForm(taxForm);
        assertThat(taxForm.getStatus()).isEqualTo(TaxFormStatus.RETURNED);
    }

    @ParameterizedTest
    @EnumSource(value = TaxFormStatus.class, names = {
            "NOT_STARTED",
            "IN_PROGRESS",
            "ACCEPTED",
            "RETURNED"
    })
    void testReturnFormNotPermitted(TaxFormStatus taxFormStatus) {
        taxForm.setStatus(taxFormStatus);
        TaxFormStatusException taxFormStatusException = new TaxFormStatusException(
                taxForm,
                TaxFormStatus.RETURNED
        );

        assertThatThrownBy(() -> TaxFormStatusUtils.returnForm(taxForm))
                .isInstanceOf(TaxFormStatusException.class)
                .hasMessage(taxFormStatusException.getMessage());
    }

    @Test
    void testAcceptPermitted() {
        taxForm.setStatus(TaxFormStatus.SUBMITTED);
        TaxFormStatusUtils.accept(taxForm);
        assertThat(taxForm.getStatus()).isEqualTo(TaxFormStatus.ACCEPTED);
    }

    @ParameterizedTest
    @EnumSource(value = TaxFormStatus.class, names = {
            "NOT_STARTED",
            "IN_PROGRESS",
            "ACCEPTED",
            "RETURNED"
    })
    void testAcceptNotPermitted(TaxFormStatus taxFormStatus) {
        taxForm.setStatus(taxFormStatus);
        TaxFormStatusException taxFormStatusException = new TaxFormStatusException(
                taxForm,
                TaxFormStatus.ACCEPTED
        );

        assertThatThrownBy(() -> TaxFormStatusUtils.accept(taxForm))
                .isInstanceOf(TaxFormStatusException.class)
                .hasMessage(taxFormStatusException.getMessage());
    }
}
