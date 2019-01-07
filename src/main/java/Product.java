import lombok.Value;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Created by mtumilowicz on 2019-01-04.
 */
@Value
class Product {
    int id;
    
    static CompletableFuture<Product> getProduct(Integer id) {
        return CompletableFuture.supplyAsync(() -> new Product(id));
    }
}

@Value
class Packed {
    Product product;

    static Function<Product, CompletableFuture<Packed>> pack(Executor executor) {
        return product -> CompletableFuture.supplyAsync(() -> new Packed(product), executor);
    }
}

@Value
class Send {
    Packed packed;
    
    static Function<Packed, CompletableFuture<Send>> send(Executor executor) {
        return packed -> CompletableFuture.supplyAsync(() -> new Send(packed), executor);
    }
}