import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

/**
 * Created by mtumilowicz on 2019-01-05.
 */
class ProductService {
    List<String> pack(List<Integer> ids) {

        var executor = productThreadPool(ids.size());

        var sendFutures = IntStream.range(1, 300)
                .mapToObj(id -> CompletableFuture.supplyAsync(() -> new Product(id), executor))
                .map(product -> product.thenCompose(p -> Packed.pack(p, executor)))
                .map(packed -> packed.thenCompose(p -> Send.send(p, executor)))
                .map(send -> send.thenApply(Send::toString))
                .map(future -> future.orTimeout(500, TimeUnit.MILLISECONDS))
                .map(future -> future.handle((ok, ex) -> nonNull(ok) ? ok : "FAILED: " + ex))
                .collect(toList());

        return sendFutures.stream()
                .map(CompletableFuture::join)
                .collect(toList());
    }

    private Executor productThreadPool(int size) {
        return Executors.newFixedThreadPool(Math.min(size, 100),
                r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    return t;
                });
    }
}
