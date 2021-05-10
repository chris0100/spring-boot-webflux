package com.webflux.services;


import com.webflux.dao.CategoriaRepository;
import com.webflux.dao.ProductoRepository;
import com.webflux.models.documents.Categoria;
import com.webflux.models.documents.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepositoryObj;

    @Autowired
    private CategoriaRepository categoriaRepositoryObj;

    @Override
    public Flux<Producto> findAll() {
        return productoRepositoryObj.findAll();
    }

    @Override
    public Mono<Producto> findById(String id) {
        return productoRepositoryObj.findById(id);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return productoRepositoryObj.save(producto);
    }

    @Override
    public Mono<Void> delete(Producto producto) {
        return productoRepositoryObj.delete(producto);
    }

    @Override
    public Flux<Producto> findAllConNombreUpperCase() {
        return productoRepositoryObj.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                });
    }

    @Override
    public Flux<Producto> findAllConNombreUpperCaseRepeat() {
        return findAllConNombreUpperCase().repeat(5000);
    }


    @Override
    public Flux<Categoria> findAllCategoria() {
        return categoriaRepositoryObj.findAll();
    }

    @Override
    public Mono<Categoria> findCategoriaById(String id) {
        return categoriaRepositoryObj.findById(id);
    }

    @Override
    public Mono<Categoria> saveCategoria(Categoria categoria) {
        return categoriaRepositoryObj.save(categoria);
    }
}
