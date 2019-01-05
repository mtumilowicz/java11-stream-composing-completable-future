import lombok.Value;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Created by mtumilowicz on 2019-01-04.
 */
@Value
class Product {
    int id;
}

@Value
class Packed {
    Product product;

    static CompletableFuture<Packed> pack(Product product, Executor executor) {
        return CompletableFuture.supplyAsync(() -> new Packed(product), executor);
    }
}

@Value
class Send {
    Packed packed;
    
    static CompletableFuture<Send> send(Packed packed, Executor executor) {
        return CompletableFuture.supplyAsync(() -> new Send(packed), executor);
    }
}