package to.charlie.foodplanner.domain.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import to.charlie.foodplanner.domain.entity.TagEntity;


public interface TagRepository extends CrudRepository<TagEntity, UUID>
{
  Optional<TagEntity> findByName(final String name);
}
