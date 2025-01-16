package consulting.reason.tax_forms_api.entity;

import java.time.ZonedDateTime;

import org.hibernate.annotations.CreationTimestamp;

import consulting.reason.tax_forms_api.enums.TaxFormHistoryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tax_form_histories")
@Entity
public class TaxFormHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tax_form_id", nullable = false)
    private TaxForm taxForm;

    @Column(nullable = false)
    private TaxFormHistoryStatus status;

    @Column(nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;
}
