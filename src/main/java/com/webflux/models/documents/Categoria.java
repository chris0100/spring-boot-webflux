package com.webflux.models.documents;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;

@Document(collection = "categorias")
@Data
@NoArgsConstructor
public class Categoria {

    @Id
    @NotEmpty
    private String id;
    private String nombre;

    public Categoria(String nombre) {
        this.nombre = nombre;
    }
}
