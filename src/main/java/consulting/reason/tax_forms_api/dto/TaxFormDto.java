package consulting.reason.tax_forms_api.dto;

import consulting.reason.tax_forms_api.enums.TaxFormStatus;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxFormDto {
    private Integer id;
    private Integer formYear;
    private String formName;
    private TaxFormStatus status;
    private TaxFormDetailsDto taxFormDetailsDto;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
