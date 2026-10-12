CREATE USER hline_user WITH PASSWORD 'hline_password';
CREATE DATABASE hline_db OWNER hline_user;
GRANT ALL PRIVILEGES ON DATABASE hline_db TO hline_user;
