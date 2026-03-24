-- Flyway V1: Initial schema migration
-- Created to match existing JPA entity definitions

-- users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    default_list_id BIGINT
);

-- guests table
CREATE TABLE IF NOT EXISTS guests (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(50) NOT NULL DEFAULT '#cccccc'
);

-- products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    price DOUBLE PRECISION,
    category_id BIGINT REFERENCES categories(id)
);

-- shopping_lists table
CREATE TABLE IF NOT EXISTS shopping_lists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_id BIGINT REFERENCES users(id)
);

-- shopping_list_shares table (many-to-many join table)
CREATE TABLE IF NOT EXISTS shopping_list_shares (
    list_id BIGINT NOT NULL REFERENCES shopping_lists(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    PRIMARY KEY (list_id, user_id)
);

-- shopping_list_items table
CREATE TABLE IF NOT EXISTS shopping_list_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    count DOUBLE PRECISION,
    unit_price DOUBLE PRECISION,
    checked BOOLEAN NOT NULL DEFAULT FALSE,
    category_id BIGINT REFERENCES categories(id),
    added_by_id BIGINT REFERENCES users(id),
    list_id BIGINT REFERENCES shopping_lists(id)
);

-- password_reset_tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    expires_at TIMESTAMP NOT NULL
);
