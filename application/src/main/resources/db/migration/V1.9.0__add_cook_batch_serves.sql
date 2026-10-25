-- How many portions the pot was planned to make, when the user scaled the recipe. Null means the
-- recipe's own yield, which is also what every batch planned before this column existed used.
ALTER TABLE cook_batch ADD COLUMN serves INTEGER;
ALTER TABLE cook_batch ADD CONSTRAINT ck_cook_batch_serves CHECK (serves IS NULL OR serves >= 1);
