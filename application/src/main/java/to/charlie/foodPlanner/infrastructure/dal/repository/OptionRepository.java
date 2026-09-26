package to.charlie.foodPlanner.infrastructure.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import to.charlie.foodPlanner.domain.model.entity.OptionEntity;
import to.charlie.foodPlanner.domain.model.internal.options.Option;

@Repository
public interface OptionRepository extends JpaRepository<OptionEntity, Option> {
}
