-- V11__add_plan_to_events.sql
ALTER TABLE events 
ADD COLUMN plan VARCHAR(20) NOT NULL DEFAULT 'FREE';

-- Migra plano do customer para seus eventos existentes
UPDATE events e
SET plan = c.plan
FROM customers c
WHERE e.customer_id = c.id;
