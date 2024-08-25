package com.cartservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "products",indexes = @Index(columnList = "unique_code"))
public class Product {

    @Id
    @Column(name = "product_id")
    private int id;
    @Column(unique = true)
    private String title;
    private int price;
    private int count = 1;
    @Column(name = "unique_code",unique = true)
    private String uniqueCode;
    private String photoUrl;

}
