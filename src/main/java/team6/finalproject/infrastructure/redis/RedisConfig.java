package team6.finalproject.infrastructure.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisConfig {

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory();
  }

  @Bean
  public StringRedisTemplate stringRedisTemplate(
      RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }
}
