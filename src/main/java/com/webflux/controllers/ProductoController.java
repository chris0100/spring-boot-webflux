package com.webflux.controllers;

import com.webflux.models.documents.Categoria;
import com.webflux.models.documents.Producto;
import com.webflux.services.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Controller
@Slf4j
@SessionAttributes("producto")
public class ProductoController {

    @Autowired
    private ProductoService productoServiceObj;

    @Value("${config.uplads.path}")
    private String path;


    //llamamos el model atribute para inyectarlo en form.html
    @ModelAttribute("categorias")
    public Flux<Categoria> categoriaFlux() {
        return productoServiceObj.findAllCategoria();
    }


    //Retorna un flux de datos, los pasa por un map para filtrar y suscribe mostrando en logs
    @GetMapping({"/listar", "/"})
    public Mono<String> listar(Model model) {

        Flux<Producto> productos = productoServiceObj.findAllConNombreUpperCase();

        productos.subscribe(prod -> log.info(prod.getNombre()));

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return Mono.just("listar");
    }

    @GetMapping("/form")
    public Mono<String> crear(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "Formulario de producto");
        model.addAttribute("boton", "Crear");
        return Mono.just("form");
    }


    @PostMapping("/form")
    public Mono<String> guardar(Producto producto, SessionStatus status, @RequestPart(name = "file") FilePart filePart) {
        status.setComplete();


        Mono<Categoria> categoriaMono = productoServiceObj.findCategoriaById(producto.getCategoria().getId());

        return categoriaMono.flatMap(c -> {

            if (producto.getCreateAt() == null) {
                producto.setCreateAt(new Date());
            }

            if (!filePart.filename().isEmpty()) {
                producto.setFoto(UUID.randomUUID().toString() + "-" + filePart.filename()
                        .replace(" ", "")
                        .replace(":", "")
                        .replace("\\", ""));
            }

            producto.setCategoria(c);
            return productoServiceObj.save(producto);
        })
                .doOnNext(p -> {
                    log.info("Producto almacenado: " + p.getNombre() + " : " + p.getId() + " : " + p.getCategoria().getNombre());
                })
                .flatMap(p -> {
                    if (!filePart.filename().isEmpty()) {
                        return filePart.transferTo(new File(path + p.getFoto()));
                    }
                    return Mono.empty();
                })
                .thenReturn("redirect:/listar");
    }


    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable("id") String id, Model model) {
        Mono<Producto> productoMono = productoServiceObj.findById(id)
                .doOnNext(p -> log.info("Producto: " + p.getNombre()))
                .defaultIfEmpty(new Producto());

        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("producto", productoMono);
        model.addAttribute("boton", "Editar");
        return Mono.just("form");
    }

    //forma 2 para editar, ya que permite manejar errores
    @GetMapping("/form-v2/{id}")
    public Mono<String> editarV2(@PathVariable("id") String id, Model model) {
        return productoServiceObj.findById(id)
                .doOnNext(p -> {
                    log.info("Producto: " + p.getNombre());
                    model.addAttribute("titulo", "Editar Producto");
                    model.addAttribute("producto", p);
                    model.addAttribute("boton", "Editar");

                })
                .defaultIfEmpty(new Producto())
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto"));
                    }
                    return Mono.just(p);
                })
                .then(Mono.just("form"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
    }


    @GetMapping("/eliminar/{id}")
    public Mono<String> eliminar(@PathVariable String id) {
        return productoServiceObj.findById(id)
                .defaultIfEmpty(new Producto())
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto a eliminar"));
                    }
                    return Mono.just(p);
                })
                .flatMap(p -> {
                    log.info("producto a eliminar: " + p.getNombre());
                    return productoServiceObj.delete(p);
                })
                .then(Mono.just("redirect:/listar?success=producto+eliminado"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
    }


    @GetMapping("/ver/{id}")
    public Mono<String> ver(Model model, @PathVariable String id) {
        return productoServiceObj.findById(id)
                .doOnNext(p -> {
                    model.addAttribute("producto", p);
                    model.addAttribute("titulo", "Detalle Producto");
                }).switchIfEmpty(Mono.just(new Producto()))
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto"));
                    }
                    return Mono.just(p);
                })
                .then(Mono.just("ver"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
    }


    @GetMapping("/uploads/img/{nombreFoto:.+}")
    public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String nombreFoto) throws MalformedURLException {
        Path ruta = Paths.get(path).resolve(nombreFoto).toAbsolutePath();

        Resource imagen = new UrlResource(ruta.toUri());

        return Mono.just(
                ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                                + imagen.getFilename() + "\"")
                        .body(imagen)
        );
    }


    //Sirve para manejar la contrapresion
    @GetMapping("/listar-datadriver")
    public String listarDataDriver(Model model) {

        Flux<Producto> productos = productoServiceObj.findAllConNombreUpperCase()
                .delayElements(Duration.ofSeconds(1)); // cada segundo va retornando

        productos.subscribe(prod -> log.info(prod.getNombre()));

        // va añadiendo al thymeleaf de 2 en 2
        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 2));
        model.addAttribute("titulo", "Listado de productos");
        return "listar";
    }


    @GetMapping("/listar-full")
    public String listarFull(Model model) {

        Flux<Producto> productos = productoServiceObj.findAllConNombreUpperCaseRepeat();


        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return "listar";
    }


    @GetMapping("/listar-chunked")
    public String listarChunked(Model model) {

        Flux<Producto> productos = productoServiceObj.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                })
                .repeat(5000);


        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return "listar-chunked";
    }


}
