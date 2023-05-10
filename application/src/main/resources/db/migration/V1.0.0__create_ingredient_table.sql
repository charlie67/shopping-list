CREATE TABLE ingredient
(
    id   UUID         NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_ingredient PRIMARY KEY (id)
);

CREATE TABLE recipe
(
    id           UUID NOT NULL,
    title        VARCHAR(255),
    url          VARCHAR(255),
    instructions TEXT,
    CONSTRAINT pk_recipe PRIMARY KEY (id)
);

CREATE TABLE recipe_ingredient
(
    id            UUID             NOT NULL,
    quantity      DOUBLE PRECISION NOT NULL,
    unit          VARCHAR(255),
    ingredient_id UUID             NOT NULL,
    recipe_id     UUID             NOT NULL,
    CONSTRAINT pk_recipe_ingredient PRIMARY KEY (id)
);

CREATE TABLE shopping_list_item
(
    id              BIGINT  NOT NULL,
    title           VARCHAR(255),
    completed       BOOLEAN NOT NULL,
    quantity        INTEGER NOT NULL,
    created_at_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_shopping_list_item PRIMARY KEY (id)
);

CREATE TABLE tag
(
    id   UUID NOT NULL,
    name VARCHAR(255),
    CONSTRAINT pk_tag PRIMARY KEY (id)
);

ALTER TABLE ingredient
    ADD CONSTRAINT uc_ingredient_name UNIQUE (name);

ALTER TABLE recipe_ingredient
    ADD CONSTRAINT FK_RECIPE_INGREDIENT_ON_INGREDIENT FOREIGN KEY (ingredient_id) REFERENCES ingredient (id);

ALTER TABLE recipe_ingredient
    ADD CONSTRAINT FK_RECIPE_INGREDIENT_ON_RECIPE FOREIGN KEY (recipe_id) REFERENCES recipe (id);