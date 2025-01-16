package consulting.reason.tax_forms_api.repository;

import consulting.reason.tax_forms_api.entity.TaxFormHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository 
public interface TaxFormHistoryRepository extends JpaRepository<TaxFormHistory, Integer> {}
