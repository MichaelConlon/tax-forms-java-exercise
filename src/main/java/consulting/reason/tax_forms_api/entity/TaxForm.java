package consulting.reason.tax_forms_api.entity;

import consulting.reason.tax_forms_api.dto.TaxFormDetailsDto;
import consulting.reason.tax_forms_api.enums.TaxFormStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tax_forms")
@Entity
public class TaxForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Integer id;
    @Column(nullable = false)

    private Integer formYear;
    
    @Column(nullable = false)
    private String formName;

    @Type(JsonType.class)
    @Column(name = "details", columnDefinition = "VARCHAR2")
    private TaxFormDetailsDto details;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private TaxFormStatus status;

    @OneToMany(mappedBy = "taxForm", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaxFormHistory> history;

    @CreationTimestamp
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    private ZonedDateTime updatedAt;

    public void addHistory(TaxFormHistory history) {
        if (this.history == null) {
            this.history = new ArrayList<>();
        }
        this.history.add(history);
        history.setTaxForm(this);
    }
}
