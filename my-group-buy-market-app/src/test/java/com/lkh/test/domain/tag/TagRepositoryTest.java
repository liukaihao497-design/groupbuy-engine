package com.lkh.test.domain.tag;

import com.lkh.domain.tag.model.entity.TagDetailEntity;
import com.lkh.domain.tag.service.ITagService;
import com.lkh.infrastructure.adapter.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class TagRepositoryTest {
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private ITagService tagService;
    @Test
    public void test(){

        tagService.executeTagJob("RQ_KJHKL98UU78H66554GFDV", "10001");

    }

    @Test
    public void test2(){
        tagService.refreshTagsDetail2Redis("RQ_KJHKL98UU78H66554GFDV");
        boolean fir = tagRepository.getBit("RQ_KJHKL98UU78H66554GFDV", userId2Int("xfg08"));
        log.info("fir:{}",fir);
        boolean sec = tagRepository.getBit("RQ_KJHKL98UU78H66554GFDV", userId2Int("xfg09"));
        log.info("sec:{}",sec);

    }

    private Integer userId2Int(String userId)  {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(userId.getBytes(StandardCharsets.UTF_8));
            // 将哈希字节数组转换为正整数
            BigInteger bigInt = new BigInteger(1, hashBytes);
            // 取模以确保索引在合理范围内
            return bigInt.mod(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
