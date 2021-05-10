package com.webflux;

import com.webflux.models.documents.Categoria;
import com.webflux.models.documents.Producto;
import com.webflux.services.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
@Slf4j
public class SpringBootWebfluxApplication implements CommandLineRunner {

    @Autowired
    private ProductoService productoServiceObj;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebfluxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        mongoTemplate.dropCollection("productos").subscribe();
        mongoTemplate.dropCollection("categorias").subscribe();

        Categoria electronico = new Categoria("Electronico");
        Categoria deporte = new Categoria("Deporte");
        Categoria computacion = new Categoria("Computacion");
        Categoria muebles = new Categoria("Muebles");


        Flux.just(electronico, deporte, computacion, muebles)
                .flatMap(productoServiceObj::saveCategoria)
                .doOnNext(c -> {
                    log.info("Categoria creada: " + c.getNombre() + ", ID: " + c.getId());
                })
                .thenMany( // se incluye otro flujo
                        Flux.just(new Producto("TV Panasonic LCD", 456.89, electronico),
                                new Producto("Radio Stereo", 100.04, deporte),
                                new Producto("Celular LG", 250.30, computacion),
                                new Producto("Audifonos", 12.36, muebles))
                                //con map, se obtiene un mono,
                                // con flatmap se puede acceder a las propiedades
                                .flatMap(producto -> {
                                    producto.setCreateAt(new Date());
                                    return productoServiceObj.save(producto);
                                })
                )
                .subscribe(producto -> log.info("insert: " + producto.getId() + " : " + producto.getNombre()));
    }
}
