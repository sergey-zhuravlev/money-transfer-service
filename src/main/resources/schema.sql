CREATE SCHEMA IF NOT EXISTS test;

CREATE TABLE IF NOT EXISTS accounts (
  id varchar(64) primary key,
  balance bigint check (balance >= 0)
  );

CREATE TABLE IF NOT EXISTS transfers (
  imdepontence_id varchar(64) primary key,
  src varchar(64),
  dst varchar(64),
  ammount bigint
);
