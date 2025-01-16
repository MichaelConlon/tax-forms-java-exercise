package consulting.reason.tax_forms_api.service;

import consulting.reason.tax_forms_api.dto.TaxFormDetailsDto;
import consulting.reason.tax_forms_api.dto.TaxFormDto;
import consulting.reason.tax_forms_api.dto.request.TaxFormDetailsRequest;
import consulting.reason.tax_forms_api.entity.TaxFormHistory;
import consulting.reason.tax_forms_api.enums.TaxFormHistoryStatus;
import consulting.reason.tax_forms_api.enums.TaxFormStatus;
import consulting.reason.tax_forms_api.exception.TaxFormStatusException;
import consulting.reason.tax_forms_api.repository.TaxFormHistoryRepository;
import consulting.reason.tax_forms_api.repository.TaxFormRepository;
import consulting.reason.tax_forms_api.util.TaxFormStatusUtils;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaxFormServiceImpl implements TaxFormService {
    private final TaxFormHistoryRepository taxFormHistoryRepository;
    private final TaxFormRepository taxFormRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TaxFormDto> findAllByYear(Integer year) {
        return taxFormRepository.findAllByFormYear(year).stream()
                .map(taxForm -> modelMapper.map(taxForm, TaxFormDto.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TaxFormDto> findById(Integer id) {
        return taxFormRepository.findById(id)
                .map(taxForm -> modelMapper.map(taxForm, TaxFormDto.class));
    }

    @Override
    @Transactional
    public Optional<TaxFormDto> save(Integer id, TaxFormDetailsRequest taxFormDetailsRequest) {
        return taxFormRepository.findById(id)
                .map(taxForm -> {
                    TaxFormStatusUtils.save(taxForm);
                    taxForm.setDetails(modelMapper.map(taxFormDetailsRequest, TaxFormDetailsDto.class));

                    taxFormRepository.save(taxForm);

                    return modelMapper.map(taxForm, TaxFormDto.class);
                });
    }

    @Override
    @Transactional
    public Optional<TaxFormDto> submit(Integer id) {
        return taxFormRepository.findById(id)
                .map(taxForm -> {
                    TaxFormStatusUtils.submit(taxForm);

                    // Updated status and create history
                    TaxFormHistory taxFormHistory = TaxFormHistory.builder()
                            .taxForm(taxForm)
                            .status(TaxFormHistoryStatus.SUBMITTED)
                            .build();

                    // Save 
                    taxFormHistoryRepository.save(taxFormHistory);
                    taxFormRepository.save(taxForm);

                    return modelMapper.map(taxForm, TaxFormDto.class);
                });
    }

}
