package consulting.reason.tax_forms_api.controller;

import consulting.reason.tax_forms_api.AbstractControllerTest;
import consulting.reason.tax_forms_api.dto.TaxFormDetailsDto;
import consulting.reason.tax_forms_api.dto.TaxFormDto;
import consulting.reason.tax_forms_api.dto.request.TaxFormDetailsRequest;
import consulting.reason.tax_forms_api.service.TaxFormService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = TaxFormController.class)
public class TaxFormControllerTest extends AbstractControllerTest {

    @Autowired
    protected MockMvc mockMvc;
    @MockBean
    private TaxFormService taxFormService;

    private final TaxFormDetailsDto taxFormDetailsDto = TaxFormDetailsDto.builder()
            .ratio(0.5)
            .assessedValue(100)
            .appraisedValue(1000L)
            .comments("testing")
            .build();
    private final TaxFormDetailsRequest taxFormDetailsRequest = TaxFormDetailsRequest.builder()
            .ratio(0.5)
            .assessedValue(100)
            .appraisedValue(1000L)
            .comments("testing")
            .build();
    private final TaxFormDto taxFormDto = TaxFormDto.builder()
            .id(1)
            .details(taxFormDetailsDto)
            .formName("Testing form RCC")
            .formYear(2024)
            .createdAt(ZonedDateTime.now())
            .updatedAt(ZonedDateTime.now())
            .build();

    @Test
    void testFindAllByYear() throws Exception {
        given(taxFormService.findAllByYear(2024)).willReturn(List.of(taxFormDto));

        mockMvc.perform(get(Endpoints.FORMS)
                        .param("year", "2024")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(taxFormDto))));
    }

    @Test
    void testFindById() throws Exception {
        given(taxFormService.findById(taxFormDto.getId())).willReturn(Optional.of(taxFormDto));

        mockMvc.perform(get(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taxFormDto)));
    }

    @Test
    void testFindByIdHandlesNotFound() throws Exception {
        mockMvc.perform(get(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSave() throws Exception {
        given(taxFormService.save(taxFormDto.getId(), taxFormDetailsRequest)).willReturn(Optional.of(taxFormDto));

        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .content(objectMapper.writeValueAsString(taxFormDetailsRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taxFormDto)));
    }

    @Test
    void testSaveHandlesNotFound() throws Exception {
        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .content(objectMapper.writeValueAsString(taxFormDetailsRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSubmit() throws Exception {
        given(taxFormService.submit(taxFormDto.getId())).willReturn(Optional.of(taxFormDto));

        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId() + "/submit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taxFormDto)));
    }

    @Test
    void testSubmitHandlesNotFound() throws Exception {
        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId() + "/submit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testReturnForm() throws Exception {
        given(taxFormService.returnForm(taxFormDto.getId())).willReturn(Optional.of(taxFormDto));

        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId() + "/return")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taxFormDto)));
    }

    @Test
    void testReturnFormHandlesNotFound() throws Exception {
        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId() + "/return")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    //
    // REQUEST VALIDATION TESTS
    //

    // VALID REQUESTS
    @Test
    void testSaveWithValidRequest_AllFields() throws Exception {
        String longComment = "a".repeat(500); // Creates a string longer than 500 characters
        TaxFormDetailsRequest validRequest = TaxFormDetailsRequest.builder()
                .assessedValue(100)
                .appraisedValue(1000L)
                .ratio(0.5)
                .comments(longComment)
                .build();

        given(taxFormService.save(taxFormDto.getId(), validRequest)).willReturn(Optional.of(taxFormDto));

        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .content(objectMapper.writeValueAsString(validRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taxFormDto)));
    }

    @Test
    void testSaveWithValidRequest_OptionalFieldsNull() throws Exception {
        TaxFormDetailsRequest validRequest = TaxFormDetailsRequest.builder()
                .assessedValue(100)
                .appraisedValue(null)
                .ratio(0.5)
                .comments(null)
                .build();

        given(taxFormService.save(taxFormDto.getId(), validRequest)).willReturn(Optional.of(taxFormDto));

        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .content(objectMapper.writeValueAsString(validRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(taxFormDto)));
    }

    // ASSESSED VALUE
    @Test
    void testSaveWithTooSmallAssessedValue() throws Exception {
        TaxFormDetailsRequest invalidRequest = TaxFormDetailsRequest.builder()
                .assessedValue(-1)
                .appraisedValue(1000L)
                .ratio(0.5)
                .comments("testing")
                .build();

        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSaveWithTooLargeAssessedValue() throws Exception {
        TaxFormDetailsRequest invalidRequest = TaxFormDetailsRequest.builder()
                .assessedValue(100001)
                .appraisedValue(1000L)
                .ratio(0.5)
                .comments("testing")
                .build();

        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSaveWithNullAssessedValue() throws Exception {
        TaxFormDetailsRequest invalidRequest = TaxFormDetailsRequest.builder()
                .assessedValue(null)
                .appraisedValue(1000L)
                .ratio(null)
                .comments("testing")
                .build();

        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // RATIO
    @Test
    void testSaveWithTooLargeRatio() throws Exception {
        TaxFormDetailsRequest invalidRequest = TaxFormDetailsRequest.builder()
                .assessedValue(100)
                .appraisedValue(1000L)
                .ratio(1.1) // Greater than 1.0
                .comments("testing")
                .build();

        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSaveWithTooSmallRatio() throws Exception {
        TaxFormDetailsRequest invalidRequest = TaxFormDetailsRequest.builder()
                .assessedValue(100)
                .appraisedValue(1000L)
                .ratio(-0.1) // Greater than 1.0
                .comments("testing")
                .build();

        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // APPRAISED VALUE
    @Test
    void testSaveWithNullAppraisedValue() throws Exception {
        TaxFormDetailsRequest invalidRequest = TaxFormDetailsRequest.builder()
                .assessedValue(100)
                .appraisedValue(1000L)
                .ratio(null)
                .comments("testing")
                .build();

        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // COMMENTS
    @Test
    void testSaveWithTooLongComment() throws Exception {
        String longComment = "a".repeat(501); // Creates a string longer than 500 characters
        TaxFormDetailsRequest invalidRequest = TaxFormDetailsRequest.builder()
                .assessedValue(100)
                .appraisedValue(1000L)
                .ratio(0.5)
                .comments(longComment)
                .build();

        mockMvc.perform(patch(Endpoints.FORMS + "/" + taxFormDto.getId())
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
