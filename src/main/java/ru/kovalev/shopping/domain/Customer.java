package ru.kovalev.shopping.domain;

import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Customer extends BaseEntity {
    String name;
}