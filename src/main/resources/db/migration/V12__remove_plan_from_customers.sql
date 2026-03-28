-- V12__remove_plan_from_customers.sql
ALTER TABLE customers 
DROP COLUMN plan;
