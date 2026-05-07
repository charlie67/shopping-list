ALTER TABLE recipe_ingredient DROP COLUMN quantity;

ALTER TABLE recipe_ingredient ADD COLUMN size VARCHAR;
ALTER TABLE recipe_ingredient ADD COLUMN preparation VARCHAR;

ALTER TABLE recipe_ingredient ADD COLUMN quantity DECIMAL(10, 2);
ALTER TABLE recipe_ingredient ADD COLUMN quantity_unit VARCHAR;
ALTER TABLE recipe_ingredient ADD COLUMN quantity_text VARCHAR;
