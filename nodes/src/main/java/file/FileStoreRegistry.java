package file;

import org.redisson.api.RedissonClient;

/**
 * Created by Shunjie Ding on 16/12/2016.
 */
public class FileStoreRegistry {
    private RedissonClient redissonClient;

    public FileStoreRegistry(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
}
