/**
 * Created by mtumilowicz on 2018-01-07.
 */
class Delay {
    static void delay() {
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            // not used
        }
    }
}