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
    
    static Function<Integer, CompletableFuture<Product>> getProduct(Function<Integer, Product> productProvider,
                                                                    Executor executor) {
        return id -> CompletableFuture.supplyAsync(() -> productProvider.apply(id), executor);
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
    
    String asReport() {
        return "Successfully send: " + packed.toString();
    }
}