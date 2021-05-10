package com.webflux.models.documents;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Document(collection = "productos")
@Data
@NoArgsConstructor
public class Producto {

    @Id
    private String id;

    @NotEmpty
    private String nombre;

    @NotNull
    private Double precio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createAt;

    private Categoria categoria;

    private String foto;


    public Producto(String nombre, Double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    public Producto(String nombre, Double precio, Categoria categoria) {
        this(nombre,precio);
        this.categoria = categoria;
    }
}
