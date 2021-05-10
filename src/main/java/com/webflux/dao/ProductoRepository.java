package com.webflux.dao;

import com.webflux.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;


public interface ProductoRepository extends ReactiveMongoRepository<Producto,String> {

}
