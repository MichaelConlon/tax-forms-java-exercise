package consulting.reason.tax_forms_api.dto;

import java.time.ZonedDateTime;

import consulting.reason.tax_forms_api.enums.TaxFormHistoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxFormHistoryDto {
    private Integer taxFormId;
    private ZonedDateTime createdAt;
    private TaxFormHistoryStatus type;
}
