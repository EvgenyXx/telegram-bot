ALTER TABLE lineup DROP COLUMN IF EXISTS city;
DELETE FROM lineup WHERE id NOT IN (SELECT MIN(id) FROM lineup GROUP BY league, time, date);
ALTER TABLE lineup ADD CONSTRAINT unique_lineup UNIQUE (league, time, date);