package com.rongxiaotong.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.dto.*;
import com.rongxiaotong.entity.*;
import com.rongxiaotong.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公开数据服务：不需要登录就能访问的数据（首页各板块）
 */
@Service
@RequiredArgsConstructor
public class PublicService {

    private final NewsMapper newsMapper;
    private final LoanProductMapper loanProductMapper;
    private final ProductMapper productMapper;
    private final ExpertProfileMapper expertProfileMapper;
    private final ArticleMapper articleMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final UserMapper userMapper;

    /** 新闻列表（按发布时间倒序） */
    public List<News> newsList() {
        return newsMapper.selectList(
                new LambdaQueryWrapper<News>().orderByDesc(News::getCreateTime));
    }

    /** 上架贷款产品列表 */
    public List<LoanProduct> loanProducts() {
        return loanProductMapper.selectList(
                new LambdaQueryWrapper<LoanProduct>().eq(LoanProduct::getStatus, 1));
    }

    /** 用户公开信息（姓名头像，聊天页显示对方信息用；密码不外传） */
    public User userBasic(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) {
            throw new BizException("用户不存在");
        }
        u.setPassword(null);
        u.setPhone(null);
        return u;
    }

    /** 贷款产品详情 */
    public LoanProduct loanProductDetail(Long id) {
        LoanProduct p = loanProductMapper.selectById(id);
        if (p == null) {
            throw new BizException("贷款产品不存在");
        }
        return p;
    }

    /** 首页聚合数据：轮播、金融产品、专家、商品、知识、问答一次全返回 */
    public HomeVO home() {
        HomeVO vo = new HomeVO();

        // 1. 轮播新闻：最新 3 条
        vo.setNews(newsMapper.selectList(
                new LambdaQueryWrapper<News>().orderByDesc(News::getCreateTime).last("LIMIT 3")));

        // 2. 金融产品：上架的前 3 个
        vo.setLoanProducts(loanProductMapper.selectList(
                new LambdaQueryWrapper<LoanProduct>().eq(LoanProduct::getStatus, 1).last("LIMIT 3")));

        // 3. 专家一览：在职专家前 4 位（附姓名头像）
        List<ExpertProfile> expertList = expertProfileMapper.selectList(
                new LambdaQueryWrapper<ExpertProfile>().eq(ExpertProfile::getStatus, 1).last("LIMIT 4"));
        vo.setExperts(fillExpert(expertList));

        // 4. 助农电商：上架商品前 8 个（附农户姓名）
        List<Product> productList = productMapper.selectList(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)
                        .orderByDesc(Product::getCreateTime).last("LIMIT 8"));
        vo.setProducts(fillProduct(productList));

        // 5. 农业知识：浏览量最高的 4 篇（附专家姓名）
        List<Article> articleList = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().orderByDesc(Article::getViews).last("LIMIT 4"));
        vo.setArticles(fillArticle(articleList));

        // 6. 专家问答：已回答的最新 4 条（附回答内容）
        List<Question> questionList = questionMapper.selectList(
                new LambdaQueryWrapper<Question>().eq(Question::getStatus, 1)
                        .orderByDesc(Question::getCreateTime).last("LIMIT 4"));
        vo.setQuestions(fillQuestion(questionList));

        return vo;
    }

    /** 给专家列表补充姓名和头像 */
    private List<ExpertVO> fillExpert(List<ExpertProfile> list) {
        Map<Long, User> userMap = userMap(list.stream().map(ExpertProfile::getUserId).toList());
        return list.stream().map(e -> {
            ExpertVO vo = new ExpertVO();
            BeanUtil.copyProperties(e, vo);
            User u = userMap.get(e.getUserId());
            if (u != null) {
                vo.setRealName(u.getRealName());
                vo.setAvatar(u.getAvatar());
            }
            return vo;
        }).toList();
    }

    /** 给商品列表补充农户姓名 */
    private List<ProductVO> fillProduct(List<Product> list) {
        Map<Long, User> userMap = userMap(list.stream().map(Product::getFarmerId).toList());
        return list.stream().map(p -> {
            ProductVO vo = new ProductVO();
            BeanUtil.copyProperties(p, vo);
            User u = userMap.get(p.getFarmerId());
            if (u != null) {
                vo.setFarmerName(u.getRealName());
            }
            return vo;
        }).toList();
    }

    /** 给文章列表补充专家姓名 */
    private List<ArticleVO> fillArticle(List<Article> list) {
        Map<Long, User> userMap = userMap(list.stream().map(Article::getExpertId).toList());
        return list.stream().map(a -> {
            ArticleVO vo = new ArticleVO();
            BeanUtil.copyProperties(a, vo);
            User u = userMap.get(a.getExpertId());
            if (u != null) {
                vo.setExpertName(u.getRealName());
            }
            return vo;
        }).toList();
    }

    /** 给问答列表补充提问人姓名和回答内容 */
    private List<QuestionVO> fillQuestion(List<Question> list) {
        Map<Long, User> farmerMap = userMap(list.stream().map(Question::getFarmerId).toList());
        // 查询这些问题的回答
        Map<Long, List<Answer>> answerMap = list.isEmpty() ? Map.of()
                : answerMapper.selectList(new LambdaQueryWrapper<Answer>()
                        .in(Answer::getQuestionId, list.stream().map(Question::getId).toList()))
                .stream().collect(Collectors.groupingBy(Answer::getQuestionId));
        // 回答专家的姓名
        Map<Long, User> expertMap = userMap(answerMap.values().stream()
                .flatMap(List::stream).map(Answer::getExpertId).toList());

        return list.stream().map(q -> {
            QuestionVO vo = new QuestionVO();
            BeanUtil.copyProperties(q, vo);
            User f = farmerMap.get(q.getFarmerId());
            if (f != null) {
                vo.setFarmerName(f.getRealName());
            }
            List<Answer> answers = answerMap.getOrDefault(q.getId(), List.of());
            if (!answers.isEmpty()) {
                vo.setAnswerContent(answers.get(0).getContent());
                User e = expertMap.get(answers.get(0).getExpertId());
                vo.setAnswerExpertName(e == null ? null : e.getRealName());
            }
            return vo;
        }).toList();
    }

    /** 批量查用户，转成 id -> 用户 的 Map */
    private Map<Long, User> userMap(List<Long> ids) {
        List<Long> distinctIds = ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(distinctIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }
}
