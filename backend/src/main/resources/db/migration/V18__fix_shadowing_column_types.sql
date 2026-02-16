-- Change JSONB to TEXT and TEXT[] to TEXT for JPA compatibility
ALTER TABLE shadowing_scenes ALTER COLUMN lines TYPE TEXT;
ALTER TABLE shadowing_scenes ALTER COLUMN characters TYPE TEXT;
