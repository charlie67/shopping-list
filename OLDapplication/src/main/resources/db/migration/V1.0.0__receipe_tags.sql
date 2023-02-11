CREATE TABLE ingredient
(
    id         UUID NOT NULL,
    ingredient TEXT UNIQUE,
    CONSTRAINT pk_ingredient PRIMARY KEY (id)
);

CREATE TABLE tag
(
    id   UUID NOT NULL,
    name VARCHAR(255) UNIQUE,
    CONSTRAINT pk_tag PRIMARY KEY (id)
);

CREATE TABLE recipe
(
    id           UUID NOT NULL,
    title        TEXT,
    instructions TEXT,
    url          TEXT,
    CONSTRAINT pk_recipe PRIMARY KEY (id)
);

CREATE TABLE recipe_ingredients
(
    ingredients_id UUID NOT NULL,
    recipe_id      UUID NOT NULL,
    CONSTRAINT pk_recipe_ingredients PRIMARY KEY (ingredients_id, recipe_id)
);

CREATE TABLE recipe_tags
(
    recipe_id UUID NOT NULL,
    tags_id   UUID NOT NULL,
    CONSTRAINT pk_recipe_tags PRIMARY KEY (recipe_id, tags_id)
);

ALTER TABLE recipe_ingredients
    ADD CONSTRAINT fk_recing_on_ingredient FOREIGN KEY (ingredients_id) REFERENCES ingredient (id);

ALTER TABLE recipe_ingredients
    ADD CONSTRAINT fk_recing_on_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id);

ALTER TABLE recipe_tags
    ADD CONSTRAINT fk_rectag_on_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id);

ALTER TABLE recipe_tags
    ADD CONSTRAINT fk_rectag_on_tag FOREIGN KEY (tags_id) REFERENCES tag (id);

CREATE TABLE planner
(
    id  UUID NOT NULL,
    day DATE,
    CONSTRAINT pk_planner PRIMARY KEY (id)
);

CREATE TABLE planner_recipes
(
    day_planner_entity_id UUID NOT NULL,
    recipes_id            UUID NOT NULL,
    CONSTRAINT pk_planner_recipes PRIMARY KEY (day_planner_entity_id, recipes_id)
);

ALTER TABLE planner_recipes
    ADD CONSTRAINT fk_plarec_on_day_planner_entity FOREIGN KEY (day_planner_entity_id) REFERENCES planner (id);

ALTER TABLE planner_recipes
    ADD CONSTRAINT fk_plarec_on_recipe_entity FOREIGN KEY (recipes_id) REFERENCES recipe (id);