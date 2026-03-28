ALTER TABLE customers ADD COLUMN password VARCHAR(255);
-- Para customers existentes, podemos definir uma senha padrão ou deixá-la nula temporariamente
-- Como é um ambiente de desenvolvimento/novo, assumimos que podemos exigir senha
UPDATE customers SET password = 'CHANGE_ME' WHERE password IS NULL;
ALTER TABLE customers ALTER COLUMN password SET NOT NULL;
