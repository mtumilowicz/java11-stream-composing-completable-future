[![Build Status](https://travis-ci.com/mtumilowicz/java11-stream-composing-completable-future.svg?branch=master)](https://travis-ci.com/mtumilowicz/java11-stream-composing-completable-future)

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
getting product by its id could be done by querying
external service.**

# solution
* `Delay`
    ```
    class Delay {
        static void delay() {
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                // not used
            }
        }
    }
    ```
* classes `Product`, `Packed` and `Send` are as simple as they can be (with some utility functions):
    ```
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
    ```
* `ProductService`
    * packing (two approaches)
        * sequence of `maps`
            ```
            List<String> send(List<Integer> ids) {
        
                var executors = productThreadPool(ids.size());
        
                var sendFutures = ids.stream()
                        .map(id -> CompletableFuture.supplyAsync(() -> new Product(id), executors))
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
            **cons: weak error handling - we cannot customize message in a reasonable way** - for example 
            we do not have access to the product `id`.
        * `map` with composed `CompletableFuture`
            ```
            var executors = productThreadPool(ids.size());
            
            var sendFutures = ids.stream()
                    .map(id -> Product.getProduct(productsProvider).apply(id)
                            .thenCompose(Packed.pack(by(executors)))
                            .thenCompose(Send.send(by(executors)))
                            .thenApply(Send::asReport)
                            .orTimeout(500, TimeUnit.MILLISECONDS)
                            .handle((ok, ex) -> nonNull(ok) ? ok : "FAILED with product = " + id + ": " + ex))
                    .collect(toList());
            
            return sendFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(toList());
            ```
            **pros: we have direct access to id when it comes to error handling**
            
        **we want to stress** that it is nearly always a good idea to support timeout
        and handle exception when using completable future:
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