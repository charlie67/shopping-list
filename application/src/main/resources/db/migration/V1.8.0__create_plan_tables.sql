ALTER TABLE recipe
    ADD COLUMN servings INTEGER;

COMMENT ON COLUMN recipe.servings IS
    'recipe_yield read as a number, for the planner''s portion maths. Null when the scraped text held no usable figure.';

--- backfill
UPDATE recipe
SET servings = CAST(substring(recipe_yield FROM '\d+') AS INTEGER)
WHERE recipe_yield ~ '\d'
  AND CAST(substring(recipe_yield FROM '\d+') AS INTEGER) BETWEEN 1 AND 20;

CREATE TABLE cook_batch
(
    id              UUID    NOT NULL,
    recipe_id       UUID    NOT NULL,
    meals_total     INTEGER NOT NULL,
    cooking_for     INTEGER NOT NULL,
    created_at_time TIMESTAMP WITHOUT TIME ZONE,
    updated_at_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_cook_batch PRIMARY KEY (id),
    CONSTRAINT ck_cook_batch_meals_total CHECK (meals_total >= 1),
    CONSTRAINT fk_cook_batch_on_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id) ON DELETE CASCADE
);

CREATE TABLE planned_meal
(
    id              UUID         NOT NULL,
    batch_id        UUID         NOT NULL,
    placement       VARCHAR(255) NOT NULL,
    planned_date    DATE,
    created_at_time TIMESTAMP WITHOUT TIME ZONE,
    updated_at_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_planned_meal PRIMARY KEY (id),
    CONSTRAINT fk_planned_meal_on_batch FOREIGN KEY (batch_id) REFERENCES cook_batch (id) ON DELETE CASCADE,
    -- A frozen meal is deliberately dateless
    CONSTRAINT ck_planned_meal_date CHECK (
        (placement = 'FROZEN' AND planned_date IS NULL)
            OR (placement <> 'FROZEN' AND planned_date IS NOT NULL))
);

CREATE INDEX idx_planned_meal_planned_date ON planned_meal (planned_date);
CREATE INDEX idx_planned_meal_batch_id ON planned_meal (batch_id);

CREATE UNIQUE INDEX uq_planned_meal_one_cook_per_batch
    ON planned_meal (batch_id) WHERE placement = 'COOK';
