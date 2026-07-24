-- Persist each ingredient's position within its recipe so the collection is read back in a
-- deterministic order, mirroring recipe_steps.step_count.
ALTER TABLE recipe_ingredient ADD COLUMN ingredient_order INTEGER NOT NULL DEFAULT 0;
