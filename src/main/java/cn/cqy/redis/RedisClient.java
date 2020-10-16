package cn.cqy.redis;

import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RedisClient {
    private Jedis jedis;//非切片客户端连接
    private JedisPool jedisPool;//非切片连接池
    private ShardedJedis shardedJedis;//切片客户端连接
    private ShardedJedisPool shardedJedisPool;//切片连接池

    public RedisClient() {
        initJedisPool();
        jedis = jedisPool.getResource();
        //因为是没有集群就只用了非切片的连接
//        initShardedJedisPool();
//        shardedJedis = shardedJedisPool.getResource();
    }

    private void initJedisPool() {
        //1.创建jedis连接池配置对象
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //2.配置jedis
        jedisPoolConfig.setMaxIdle(5);
        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxWaitMillis(1000);
        jedisPoolConfig.setTestOnBorrow(true);//测试连接是否畅通
        //3.创建jedis连接池
        jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379, 1000, "12345");
    }

    private void initShardedJedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(5);
        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxWaitMillis(1000);
        jedisPoolConfig.setTestOnBorrow(true);

        //配置多个shared
        List<JedisShardInfo> sharedInfos = new ArrayList<JedisShardInfo>();
        JedisShardInfo jedisShardInfo = new JedisShardInfo("127.0.0.1", 6379, 1000);
        jedisShardInfo.setPassword("12345");
        sharedInfos.add(jedisShardInfo);


        shardedJedisPool = new ShardedJedisPool(jedisPoolConfig,sharedInfos);
    }

    public void call() {
        stringOperate();
        listOperate();
        setOperate();
        hashOperate();
        sortedSetOperate();

        jedisPool.returnResource(jedis);
//        shardedJedisPool.returnBrokenResource(shardedJedis);
    }

    /**
     * string的基本操作
     * set添加、修改
     * get 获取
     * append 追加
     * del 删除
     */
    public void stringOperate() {
        jedis.flushAll();
        System.out.println("reids的string类型命令============================");
        jedis.set("test1", "10");
        jedis.set("test2", "20");
        System.out.println("取出存储的string类型的值");
        System.out.println(jedis.get("test1"));
        System.out.println(jedis.get("test2"));
        System.out.println("让两个key的自增");
        System.out.println(jedis.incr("test1"));
        System.out.println(jedis.incr("test2"));
        System.out.println("setnx功能测试");
        System.out.println("第一次设置，如果为1，设置成功，0失败；两次设置key相同返回值为" + jedis.setnx("test3", "suo"));
        System.out.println("第二次设置，如果为1，设置成功，0失败；返回值为" + jedis.setnx("test3", "suo"));

    }

    /**
     * list操作
     * lpush rpush设置列表值
     * lrange 获取list值
     * lrem 删除
     * lset 修改
     * <p>
     * sort排序
     * 数字，不用加sortrams，字母需要加，并且是ASCII排序
     */
    public void listOperate() {
        jedis.flushAll();
        System.out.println("redis对list类型操作=============");
        jedis.lpush("key1", "value1", "value2", "value3");
        jedis.rpush("key1", "value4");
        System.out.println("获取添加list的值");
        System.out.println(jedis.lrange("key1", 0, -1));
        jedis.lset("key1", 0, "test5");
        System.out.println("查看修改过后索引位置的值：" + jedis.lindex("key1", 0));
        jedis.lrem("key1", 1, "value1");
        System.out.println("删除1个值为value1后的list:" + jedis.lrange("key1", 0, -1));

    }

    /**
     * sadd 添加
     * srem 删除
     * smembers 查看所有成员
     * sinter 交集
     * sinterstore 交集保存到新的set
     * sunion 并集
     * sunionstore 并集保存到新的set
     * sdiff 差集
     * sdiffstore 差集保存到新的set
     */
    public void setOperate() {
        jedis.flushAll();
        System.out.println("对set类型的操作=========================");
        jedis.sadd("key1", "a1", "a2", "a3", "a4", "a5");
        jedis.sadd("key2", "a1", "a3", "a5");
        jedis.sadd("key3", "a2", "a4");
        System.out.println("获取对应key的成员值key1" + jedis.smembers("key1") + " key2:" + jedis.smembers("key2") + " key3:" + jedis.smembers("key3"));
        System.out.println("key1和key3交集：" + jedis.sinter("key1", "key3"));
        jedis.sinterstore("key4", "key1", "key3");
        System.out.println("交集获得新的set key4：" + jedis.smembers("key4"));
        System.out.println("key2和key3并集：" + jedis.sunion("key2", "key3"));
        jedis.sunionstore("key5", "key2", "key3");
        System.out.println("并集获得新的set key5：" + jedis.smembers("key5"));
        System.out.println("key1和key2差集：" + jedis.sdiff("key1", "key2"));
        jedis.sdiffstore("key6", "key1", "key2");
        System.out.println("差集获得新的set key6" + jedis.smembers("key6"));
    }

    /**
     * hset 设置或者修改
     * hgetAll 获取所有
     * hdel 删除
     * hincrby 自增
     * hkeys 查询key所有的成员
     * hvalues 查询key所有的成员的value
     */
    public void hashOperate() {
        jedis.flushAll();
        System.out.println("对hash类型的操作===========================");
        jedis.hset("key1", "field1", "10");
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("field2", "20");
        hashMap.put("field3", "30");
        hashMap.put("field4", "22");
        jedis.hmset("key1", hashMap);
        System.out.println("获取key1所有的成员：" + jedis.hgetAll("key1"));
        jedis.hincrBy("key1", "field1", 5);
        System.out.println("为key1的指定成员field1增加5后的值：" + jedis.hget("key1", "field1"));
        System.out.println("获取key1的所有成员：" + jedis.hkeys("key1"));
        System.out.println("获取key1所有成员的value：" + jedis.hvals("key1"));
    }

    /**
     * zadd 添加
     * zcard 返回成员数
     * zcount 计算区间中的成员数
     * zrange 返回集合区间的成员 当start为0，stop为1时查看全部区间；
     * zrem 删除集合中成员
     * zremrangebyrank 删除集合中给定排名区间的成员
     */
    public void sortedSetOperate() {
        jedis.flushAll();
        System.out.println("对sortedset类型的操作");
        jedis.zadd("key1", 20, "member1");
        jedis.zadd("key1", 30, "member2");
        jedis.zadd("key1", 15, "member3");
        System.out.println("获取集合区间的所有成员和分数" + jedis.zrangeWithScores("key1", 0, -1));
        System.out.println("获取成员数" + jedis.zcard("key1"));
        System.out.println("获取20-30区间的成员数" + jedis.zcount("key1", 20, 30));
        jedis.zremrangeByRank("key1", 1, 2);
        System.out.println(jedis.zrangeWithScores("key1", 0, -1));

    }


}
