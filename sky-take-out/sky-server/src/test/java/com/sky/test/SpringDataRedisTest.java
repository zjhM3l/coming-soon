package com.sky.test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

// @SpringBootTest
// 注释掉防止每次启动都执行
public class SpringDataRedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    public void testRedisTemplate() {
        System.out.println(redisTemplate);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        HashOperations hashOperations = redisTemplate.opsForHash();
        ListOperations listOperations = redisTemplate.opsForList();
        SetOperations setOperations = redisTemplate.opsForSet();
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
    }

    /**
     * 测试字符串操作
     */
    @Test
    public void testString() {
        // set
        redisTemplate.opsForValue().set("name", "melbourne");
        // get
        String name = (String) redisTemplate.opsForValue().get("name");
        System.out.println(name);
        // setex
        redisTemplate.opsForValue().set("age", "22", 60, TimeUnit.SECONDS);
        // setnx
        redisTemplate.opsForValue().setIfAbsent("lock", "1");
        redisTemplate.opsForValue().setIfAbsent("lock", "2");
    }

    /**
     * 测试hash操作
     */
    @Test
    public void testHash() {
        HashOperations hashOperations = redisTemplate.opsForHash();
        // hset
        hashOperations.put("user", "name", "melbourne"); // key-field-value
        hashOperations.put("user", "age", "22");
        // hget
        String name = (String) hashOperations.get("user", "name");
        System.out.println(name);
        // hkeys
        Set keys = hashOperations.keys("user");
        System.out.println(keys);
        // hvals
        List values = hashOperations.values("user");
        System.out.println(values);
        // hdel
        hashOperations.delete("user", "name");
    }

    /**
     * 测试list操作
     */
    public void testList() {
        ListOperations listOperations = redisTemplate.opsForList();
        // lpush
        listOperations.leftPushAll("mylist", "a", "b", "c");
        listOperations.leftPush("mylist", "d");
        // lrange
        List mylish = listOperations.range("mylist", 0, -1);
        System.out.println(mylish);
        // rpop
        listOperations.rightPop("mylist");
        // llen
        Long size = listOperations.size("mylist");
        System.out.println(size);
    }

    /**
     * 测试set操作
     */
    public void testSet() {
        SetOperations setOperations = redisTemplate.opsForSet();
        // sadd
        setOperations.add("set1", "a", "b", "c", "d");
        setOperations.add("set2", "a", "b", "x", "y");
        // smembers
        Set members = setOperations.members("set1");
        System.out.println(members);
        // scard
        Long size = setOperations.size("set1");
        System.out.println(size);
        // sinter 
        Set inter = setOperations.intersect("set1", "set2");
        System.out.println(inter);
        // sunion
        Set union = setOperations.union("set1", "set2");
        System.out.println(union);
        // srem 
        setOperations.remove("set1", "a", "b");
    }

    /**
     * 测试zset操作
     */
    public void testZSet() {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        // zadd
        zSetOperations.add("zset1", "a", 10);
        zSetOperations.add("zset1", "b", 12);
        zSetOperations.add("zset1", "c", 9);
        // zrange
        Set range = zSetOperations.range("zset1", 0, -1);
        System.out.println(range);
        // zincrby
        zSetOperations.incrementScore("zset1", "c", 10);
        // zrem
        zSetOperations.remove("zset1", "a", "b");
    }

    /**
     * 测试通用操作
     */
    public void testCommon() {
        // 直接获取redisTemplate对象即可进行操作
        // keys
        Set keys = redisTemplate.keys("*");
        System.out.println(keys);
        // exists
        Boolean name = redisTemplate.hasKey("name");
        Boolean set1 = redisTemplate.hasKey("set1");
        // type
        for (Object key : keys) {
            DataType type = redisTemplate.type(key);
            System.out.println(key + " : " + type.name());
        }
        // delete
        redisTemplate.delete("mylist");
    }
}
