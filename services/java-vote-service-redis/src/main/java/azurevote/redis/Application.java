package azurevote.redis;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import redis.clients.jedis.Jedis;
import java.util.Set;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import azurevote.redis.models.VoteCount;

@SpringBootApplication
@RestController
public class Application {

    static JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), System.getenv("REDIS_HOST"));

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public String health() {
        System.out.println("Health check");  
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hget("Cats","count");
            return "OK!";
        } catch(Exception ex) {
            return "NOT OK! " + ex.getMessage();
        }
    }

    @RequestMapping(value = "/votes", method = RequestMethod.POST)
    public void add_vote(@RequestBody String vote) {
        System.out.println("Incoming vote: " + vote);  
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hincrBy(vote, "count", 1);
        }
    }

    @RequestMapping(value = "/votes", method = RequestMethod.GET, produces = "application/json")
    public ArrayList<VoteCount> get_votes() {
        System.out.println("Get votes");       
        try (Jedis jedis = jedisPool.getResource()) {
            ArrayList<VoteCount> result = new ArrayList<VoteCount>();
            String vote1Count = jedis.hget("Cats","count");
            System.out.println("Vote1: " + vote1Count);

            String vote2Count = jedis.hget("Dogs","count");
            System.out.println("Vote2: " + vote2Count);
            
            result.add(new VoteCount("Cats", vote1Count));
            result.add(new VoteCount("Dogs", vote2Count));
            return result;
        } 
    }

    @RequestMapping(value = "/reset", method = RequestMethod.POST)
    public void reset_votes() {
        System.out.println("Reset votes"); 
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("Cats","count", String.valueOf(0));               
            jedis.hset("Dogs","count", String.valueOf(0));       
        }        
    }

    public static void main(String[] args) {
        if(System.getenv("REDIS_HOST") == null) {
            throw new IllegalArgumentException("No REDIS_HOST is set!");
        }
        SpringApplication.run(Application.class, args);
    }

}