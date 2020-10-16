package cn.cqy.redis;

import org.junit.Test;

public class RedisTest {

    @Test
    public void testRedis () {
        //总的测试类
        new RedisClient().call();
    }
}
