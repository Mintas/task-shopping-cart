--liquibase formatted sql

--changeset dkovalev:001.1
--comment create customer, product, cart and item tables
CREATE TABLE customer
(
    id         UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    deleted    BOOLEAN NOT NULL,
    version    INTEGER NOT NULL,
    name       VARCHAR(255) NOT NULL,
    CONSTRAINT pk_customer PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uq_customer_name_not_deleted ON customer (name) WHERE deleted IS NOT TRUE;


CREATE TABLE product
(
    id          UUID NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    deleted     BOOLEAN NOT NULL,
    version     INTEGER NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    stored      INTEGER NOT NULL,
    reserved    INTEGER NOT NULL,
    CONSTRAINT pk_product PRIMARY KEY (id)
);

CREATE TABLE cart
(
    id            UUID NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE,
    updated_at  TIMESTAMP WITH TIME ZONE,
    deleted    BOOLEAN NOT NULL,
    version    INTEGER NOT NULL,
    customer_id   UUID NOT NULL,
    cart_state    TEXT NOT NULL,
    CONSTRAINT pk_cart PRIMARY KEY (id)
);
ALTER TABLE cart
    ADD CONSTRAINT fk_cart_on_customer FOREIGN KEY (customer_id) REFERENCES customer (id) ON DELETE RESTRICT;
CREATE UNIQUE INDEX uq_cart_on_customer_not_deleted ON cart (customer_id) WHERE deleted IS NOT TRUE;

CREATE TABLE item
(
    id         UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    deleted    BOOLEAN NOT NULL,
    version    INTEGER NOT NULL,
    cart_id    UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity   INTEGER NOT NULL,
    CONSTRAINT pk_item PRIMARY KEY (id)
);
ALTER TABLE item
    ADD CONSTRAINT fk_item_on_cart FOREIGN KEY (cart_id) REFERENCES cart (id) ON DELETE RESTRICT;
ALTER TABLE item
    ADD CONSTRAINT fk_item_on_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE RESTRICT;

