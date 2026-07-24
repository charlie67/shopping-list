-- extraction_method is now persisted as the ExtractionMethod enum name (EnumType.STRING)
-- rather than the human-readable label, so migrate existing rows to the enum constant names.
UPDATE recipe SET extraction_method = 'MICRODATA' WHERE extraction_method = 'microdata';
UPDATE recipe SET extraction_method = 'JSON_LD' WHERE extraction_method = 'JSON-LD';
UPDATE recipe SET extraction_method = 'JUST_THE_RECIPE' WHERE extraction_method = 'JustTheRecipe';
