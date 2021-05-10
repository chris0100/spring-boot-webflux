package com.webflux.controllers;

import com.webflux.models.documents.Producto;
import com.webflux.services.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping("/api/productos")
public class ProductoRestController {

    @Autowired
    private ProductoService productoServiceObj;

    //obtiene un flux
    @GetMapping
    public Flux<Producto> index() {

        Flux<Producto> productos = productoServiceObj.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                })
                .doOnNext(prod -> log.info(prod.getNombre()));

        return productos;
    }


    //Convierte el flux a mono despues de la consulta.
    @GetMapping("/{id}")
    public Mono<Producto> show(@PathVariable("id") String id){

        //Mono<Producto> producto = productoRepositoryObj.findById(id);
        Flux<Producto> productos = productoServiceObj.findAll();

        Mono<Producto> producto = productos.filter(p -> p.getId().equals(id))
                .next()
                .doOnNext(pro -> log.info(pro.getNombre()));

        return producto;
    }
}
