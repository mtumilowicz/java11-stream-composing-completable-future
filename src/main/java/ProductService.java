import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

/**
 * Created by mtumilowicz by 2019-01-05.
 */
class ProductService {
    List<String> send(List<Integer> ids) {

        var executors = productThreadPool(ids.size());

        var sendFutures = ids.stream()
                .map(id -> Product.getProduct(id)
                        .thenCompose(Packed.pack(by(executors)))
                        .thenCompose(Send.send(by(executors)))
                        .thenApply(Send::asReport)
                        .orTimeout(500, TimeUnit.MILLISECONDS)
                        .handle((ok, ex) -> nonNull(ok) ? ok : "FAILED with product = " + id + ": " + ex))
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

    private Executor by(Executor executor) {
        return executor;
    }
}
