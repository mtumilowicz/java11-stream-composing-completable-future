import lombok.Value;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

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
}

@Value
class Send {
    Packed packed;
}

class XXX {
    public static void main(String[] args) {
        var collected = Stream.of(1, 2, 3, 4)
                .map(id -> CompletableFuture.supplyAsync(() -> new Product(id)))
                .map(product -> product.thenCompose(p -> CompletableFuture.supplyAsync(() -> new Packed(p))))
                .map(packed -> packed.thenCompose(p -> CompletableFuture.supplyAsync(() -> new Send(p))))
                .map(send -> send.thenApply(Send::toString))
                .collect(toList());

        collected.stream()
                .map(CompletableFuture::join)
                .forEach(System.out::println);
    }
}