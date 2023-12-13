DROP TABLE recipe_ingredient;
DROP TABLE ingredient;
DROP TABLE recipe;

CREATE TABLE recipe
(
    id                    UUID NOT NULL,
    description           TEXT,
    name                  TEXT,
    url                   TEXT,
    date_modified         VARCHAR(255),
    date_published        VARCHAR(255),
    keywords              TEXT,
    cook_time             VARCHAR(255),
    prep_time             VARCHAR(255),
    total_time            VARCHAR(255),
    recipe_category       VARCHAR(255),
    recipe_yield          VARCHAR(255),
    calories              VARCHAR(255),
    fat_content           VARCHAR(255),
    saturated_fat_content VARCHAR(255),
    carbohydrate_content  VARCHAR(255),
    sugar_content         VARCHAR(255),
    fiber_content         VARCHAR(255),
    protein_content       VARCHAR(255),
    sodium_content        VARCHAR(255),
    extraction_method     VARCHAR(255),
    created_at_time       TIMESTAMP WITHOUT TIME ZONE,
    updated_at_time       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_recipe PRIMARY KEY (id)
);

CREATE TABLE ingredient
(
    id   UUID NOT NULL,
    name TEXT NOT NULL,
    CONSTRAINT pk_ingredient PRIMARY KEY (id)
);

CREATE TABLE recipe_steps
(
    id         UUID    NOT NULL,
    text       TEXT    NOT NULL,
    type       VARCHAR(255),
    step_count INTEGER NOT NULL,
    recipe_id  UUID,
    CONSTRAINT pk_recipe_steps PRIMARY KEY (id)
);

CREATE TABLE recipe_ingredient
(
    id            UUID NOT NULL,
    quantity      DOUBLE PRECISION,
    unit          VARCHAR(255),
    ingredient_id UUID NOT NULL,
    recipe_id     UUID NOT NULL,
    CONSTRAINT pk_recipe_ingredient PRIMARY KEY (id)
);

ALTER TABLE recipe_steps
    ADD CONSTRAINT FK_RECIPE_STEPS_ON_RECIPE FOREIGN KEY (recipe_id) REFERENCES recipe (id);

ALTER TABLE recipe_ingredient
    ADD CONSTRAINT FK_RECIPE_INGREDIENT_ON_INGREDIENT FOREIGN KEY (ingredient_id) REFERENCES ingredient (id);

ALTER TABLE recipe_ingredient
    ADD CONSTRAINT FK_RECIPE_INGREDIENT_ON_RECIPE FOREIGN KEY (recipe_id) REFERENCES recipe (id);

ALTER TABLE recipe_steps
    ADD CONSTRAINT FK_RECIPE_STEPS_ON_RECIPE_ENTITY FOREIGN KEY (recipe_id) REFERENCES recipe (id);