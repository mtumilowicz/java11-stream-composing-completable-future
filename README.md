# java11-stream-composing-completable-future
Example of mixing stream with completable future using
composition.

# preview
Please refer my other github project: https://github.com/mtumilowicz/java11-stream-completablefuture-dedicated-executor
about providing dedicated thread pool for completable future tasks.

# problem
Suppose we have stream of product ids and we want 
to perform operations:
1. get the product by its id
1. pack the product
1. send the product
1. generate the report

**Note that operation could be time consuming, for example
getting product by its id could be provided by querying
external service.**

# solution
* classes `Product`, `Packed` and `Send` as simple as they can be:
    ```
    @Value
    class Product {
        int id;
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
    ```
* `ProductService`
    * packing
        ```
        List<String> pack(List<Integer> ids) {
        
            var executors = productThreadPool(ids.size());
        
            var sendFutures = IntStream.range(1, 300)
                    .mapToObj(id -> CompletableFuture.supplyAsync(() -> new Product(id), executors))
                    .map(product -> product.thenCompose(Packed.pack(by(executors))))
                    .map(packed -> packed.thenCompose(Send.send(by(executors))))
                    .map(send -> send.thenApply(Send::toString))
                    .map(future -> future.orTimeout(500, TimeUnit.MILLISECONDS))
                    .map(future -> future.handle((ok, ex) -> nonNull(ok) ? ok : "FAILED: " + ex))
                    .collect(toList());
        
            return sendFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(toList());
        }
        ```
        **it is nearly always a good idea to support timeout
        and handle exception when using completable future**:
            * `future -> future.orTimeout(500, TimeUnit.MILLISECONDS)`
            * `future -> future.handle((ok, ex) -> nonNull(ok) ? ok : "FAILED: " + ex)`
    * executors
        ```
        private Executor productThreadPool(int size) {
            return Executors.newFixedThreadPool(Math.min(size, 100),
                    r -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        return t;
                    });
        }
        ```
    * and small utility function for readability:
        ```
        private Executor by(Executor executor) {
            return executor;
        }
        ```