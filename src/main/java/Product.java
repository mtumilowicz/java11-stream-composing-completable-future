import lombok.Value;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

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

    static CompletableFuture<Packed> pack(Product product) {
        return CompletableFuture.supplyAsync(() -> new Packed(product));
    }
}

@Value
class Send {
    Packed packed;
    
    static CompletableFuture<Send> send(Packed packed) {
        return CompletableFuture.supplyAsync(() -> new Send(packed));
    }
}

class XXX {
    public static void main(String[] args) {
        var futures = Stream.of(1, 2, 3, 4)
                .map(id -> CompletableFuture.supplyAsync(() -> new Product(id)))
                .map(product -> product.thenCompose(Packed::pack))
                .map(packed -> packed.thenCompose(Send::send))
                .map(send -> send.thenApply(Send::toString))
                .map(future -> future.orTimeout(500, TimeUnit.MILLISECONDS))
                .map(future -> future.handle((ok, ex) -> nonNull(ok) ? ok : "FAILED: " + ex))
                .collect(toList());

        futures.stream()
                .map(CompletableFuture::join)
                .forEach(System.out::println);
    }
}