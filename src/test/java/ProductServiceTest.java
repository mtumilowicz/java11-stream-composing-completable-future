import org.junit.Test;

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
        var productService = new ProductService();

        var sendProducts = productService.send(IntStream.rangeClosed(1, 500).boxed().collect(toList()));
        
        assertThat(sendProducts, hasSize(500));
        assertTrue(sendProducts.stream().allMatch(report -> report.startsWith("Successfully send")));
    }
}