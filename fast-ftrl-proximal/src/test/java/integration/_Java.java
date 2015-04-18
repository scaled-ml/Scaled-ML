package integration;

import org.junit.Test;

/**
 * @author Ilya Smagin ilya-sm@yandex-team.ru on 4/8/15.
 */
public class _Java {

    @Test
    public void split(){
        String[] split = "asd,,,df".split(",");
        System.out.println("split = " + split);
    }
}
