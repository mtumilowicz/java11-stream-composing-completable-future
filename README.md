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

# solution
