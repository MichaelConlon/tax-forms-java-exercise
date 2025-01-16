package consulting.reason.tax_forms_api.dto.request;

import lombok.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class TaxFormDetailsRequest {
    @NotNull(message = "Assessed value is required")
    @Min(value = 0, message = "Assessed value must be positive")
    @Max(value = 100000, message = "Assessed value must not exceed 100,000")
    private Integer assessedValue;

    @Min(value = 0, message = "Appraised value must be positive")
    @Max(value = 100000, message = "Appraised value must not exceed 100,000")
    private Long appraisedValue;

    @NotNull(message = "Ratio is required")
    @DecimalMin(value = "0.0", message = "Ratio must be positive")
    @DecimalMax(value = "1.0", message = "Ratio must not exceed 1.0")
    private Double ratio;

    @Size(max = 500, message = "Comments must not exceed 500 characters")
    private String comments;
}
