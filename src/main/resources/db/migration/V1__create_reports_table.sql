CREATE SCHEMA reports_schema;
CREATE TABLE reports_schema.reports
(
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  report_name varchar (50) NOT NULL,
  report_time_hour integer ,
  report_time_minuets integer
);