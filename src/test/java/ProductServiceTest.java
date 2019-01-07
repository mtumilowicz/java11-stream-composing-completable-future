import org.junit.Test;

import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;

/**
 * Created by mtumilowicz on 2019-01-05.
 */
public class ProductServiceTest {

    @Test
    public void pack() {
        var productService = new ProductService(Product::new);

        var sendProducts = productService.send(IntStream.rangeClosed(1, 500).boxed().collect(toList()));

        assertThat(sendProducts, hasSize(500));
        assertTrue(sendProducts.stream().allMatch(report -> report.startsWith("Successfully send")));
    }

    @Test
    public void pack_timeout() {
        Function<Integer, Product> productProvider = id -> {
            Delay.delay();
            return new Product(id);
        };
        
        var productService = new ProductService(productProvider);

        var sendProducts = productService.send(IntStream.rangeClosed(1, 500).boxed().collect(toList()));

        assertThat(sendProducts, hasSize(500));
        assertTrue(sendProducts.stream().allMatch(report -> report.startsWith("FAILED")));
    }
}