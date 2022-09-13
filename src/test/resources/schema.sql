DROP TABLE IF EXISTS employee;
DROP SEQUENCE IF EXISTS seq_employee_id;

CREATE SEQUENCE seq_employee_id START WITH 1 INCREMENT BY 1 NO CYCLE;

CREATE TABLE employee
(
    id          INTEGER PRIMARY KEY DEFAULT nextval('seq_employee_id'),
    name        VARCHAR(32),
    joined_date DATE
);
