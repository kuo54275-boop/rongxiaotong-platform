package com.rongxiaotong.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.common.PageResult;
import com.rongxiaotong.common.Roles;
import com.rongxiaotong.dto.*;
import com.rongxiaotong.entity.*;
import com.rongxiaotong.mapper.*;
import com.rongxiaotong.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专家助力服务：农业知识库、专家一览、在线问答
 */
@Service
@RequiredArgsConstructor
public class ExpertService {

    private final ArticleMapper articleMapper;
    private final ExpertProfileMapper expertProfileMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final UserMapper userMapper;

    // ==================== 农业知识 ====================

    /** 知识列表（分页 + 分类筛选） */
    public PageResult<ArticleVO> pageArticles(long page, long size, String category) {
        LambdaQueryWrapper<Article> qw = new LambdaQueryWrapper<Article>()
                .eq(StrUtil.isNotBlank(category), Article::getCategory, category)
                .orderByDesc(Article::getCreateTime);
        Page<Article> p = articleMapper.selectPage(new Page<>(page, size), qw);
        Map<Long, User> userMap = userMap(p.getRecords().stream().map(Article::getExpertId).toList());
        List<ArticleVO> list = p.getRecords().stream().map(a -> {
            ArticleVO vo = new ArticleVO();
            BeanUtil.copyProperties(a, vo);
            User u = userMap.get(a.getExpertId());
            if (u != null) {
                vo.setExpertName(u.getRealName());
            }
            return vo;
        }).toList();
        return PageResult.of(list, p.getTotal(), p.getCurrent(), p.getSize());
    }

    /** 知识详情（浏览量 +1） */
    public ArticleVO articleDetail(Long id) {
        Article a = articleMapper.selectById(id);
        if (a == null) {
            throw new BizException("文章不存在");
        }
        a.setViews(a.getViews() == null ? 1 : a.getViews() + 1);
        articleMapper.updateById(a);
        ArticleVO vo = new ArticleVO();
        BeanUtil.copyProperties(a, vo);
        User u = userMapper.selectById(a.getExpertId());
        if (u != null) {
            vo.setExpertName(u.getRealName());
        }
        return vo;
    }

    /** 专家：我发布的知识 */
    public List<Article> myArticles() {
        return articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getExpertId, UserContext.getUserId())
                        .orderByDesc(Article::getCreateTime));
    }

    /** 专家：发布 / 修改知识 */
    public Long saveArticle(Article article) {
        if (!UserContext.isRole(Roles.EXPERT)) {
            throw new BizException("只有技术专家可以维护知识库");
        }
        if (StrUtil.isBlank(article.getTitle()) || StrUtil.isBlank(article.getContent())) {
            throw new BizException("标题和内容不能为空");
        }
        if (article.getId() == null) {
            // 新增
            article.setExpertId(UserContext.getUserId());
            article.setViews(0);
            article.setCreateTime(DateUtil.now());
            articleMapper.insert(article);
        } else {
            // 修改（只能改自己的）
            Article old = articleMapper.selectById(article.getId());
            if (old == null || !old.getExpertId().equals(UserContext.getUserId())) {
                throw new BizException("只能修改自己发布的知识");
            }
            article.setExpertId(old.getExpertId());
            article.setViews(old.getViews());
            article.setCreateTime(old.getCreateTime());
            articleMapper.updateById(article);
        }
        return article.getId();
    }

    /** 专家：删除自己的知识 */
    public void deleteArticle(Long id) {
        Article old = articleMapper.selectById(id);
        if (old == null || !old.getExpertId().equals(UserContext.getUserId())) {
            throw new BizException("只能删除自己发布的知识");
        }
        articleMapper.deleteById(id);
    }

    // ==================== 专家一览 ====================

    /** 专家列表（在职） */
    public List<ExpertVO> expertList() {
        List<ExpertProfile> list = expertProfileMapper.selectList(
                new LambdaQueryWrapper<ExpertProfile>().eq(ExpertProfile::getStatus, 1));
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

    /** 专家详情 */
    public ExpertVO expertDetail(Long userId) {
        ExpertProfile ep = expertProfileMapper.selectOne(
                new LambdaQueryWrapper<ExpertProfile>().eq(ExpertProfile::getUserId, userId));
        if (ep == null) {
            throw new BizException("专家不存在");
        }
        User u = userMapper.selectById(userId);
        ExpertVO vo = new ExpertVO();
        BeanUtil.copyProperties(ep, vo);
        if (u != null) {
            vo.setRealName(u.getRealName());
            vo.setAvatar(u.getAvatar());
        }
        return vo;
    }

    /** 专家：查看 / 修改自己的专家信息 */
    public ExpertProfile myProfile() {
        return expertProfileMapper.selectOne(
                new LambdaQueryWrapper<ExpertProfile>().eq(ExpertProfile::getUserId, UserContext.getUserId()));
    }

    public void updateProfile(ExpertProfile profile) {
        if (!UserContext.isRole(Roles.EXPERT)) {
            throw new BizException("只有技术专家可以维护专家信息");
        }
        ExpertProfile old = expertProfileMapper.selectOne(
                new LambdaQueryWrapper<ExpertProfile>().eq(ExpertProfile::getUserId, UserContext.getUserId()));
        if (old == null) {
            throw new BizException("专家档案不存在");
        }
        // 只更新允许修改的字段
        old.setTitle(profile.getTitle());
        old.setSpecialty(profile.getSpecialty());
        old.setIntro(profile.getIntro());
        old.setYears(profile.getYears());
        expertProfileMapper.updateById(old);
    }

    // ==================== 在线问答 ====================

    /** 问答列表（分页，可只看已回答） */
    public PageResult<QuestionVO> pageQuestions(long page, long size, Integer onlyAnswered) {
        LambdaQueryWrapper<Question> qw = new LambdaQueryWrapper<Question>()
                .eq(onlyAnswered != null && onlyAnswered == 1, Question::getStatus, 1)
                .orderByDesc(Question::getCreateTime);
        Page<Question> p = questionMapper.selectPage(new Page<>(page, size), qw);
        List<QuestionVO> list = fillQuestionVO(p.getRecords());
        return PageResult.of(list, p.getTotal(), p.getCurrent(), p.getSize());
    }

    /** 问答详情（问题 + 全部回答） */
    public QuestionDetailVO questionDetail(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new BizException("问题不存在");
        }
        QuestionDetailVO vo = new QuestionDetailVO();
        BeanUtil.copyProperties(q, vo);
        User f = userMapper.selectById(q.getFarmerId());
        if (f != null) {
            vo.setFarmerName(f.getRealName());
            vo.setFarmerAvatar(f.getAvatar());
        }
        // 回答列表（附专家姓名头像）
        List<Answer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<Answer>().eq(Answer::getQuestionId, id)
                        .orderByAsc(Answer::getCreateTime));
        Map<Long, User> expertMap = userMap(answers.stream().map(Answer::getExpertId).toList());
        vo.setAnswers(answers.stream().map(a -> {
            AnswerVO av = new AnswerVO();
            av.setId(a.getId());
            av.setQuestionId(a.getQuestionId());
            av.setExpertId(a.getExpertId());
            av.setContent(a.getContent());
            av.setCreateTime(a.getCreateTime());
            User e = expertMap.get(a.getExpertId());
            if (e != null) {
                av.setExpertName(e.getRealName());
                av.setExpertAvatar(e.getAvatar());
            }
            return av;
        }).toList());
        return vo;
    }

    /** 农户：提问 */
    public Long ask(Question question) {
        if (!UserContext.isRole(Roles.FARMER)) {
            throw new BizException("只有农户可以提问");
        }
        if (StrUtil.isBlank(question.getTitle()) || StrUtil.isBlank(question.getContent())) {
            throw new BizException("问题标题和描述不能为空");
        }
        question.setFarmerId(UserContext.getUserId());
        question.setStatus(0);
        question.setCreateTime(DateUtil.now());
        questionMapper.insert(question);
        return question.getId();
    }

    /** 农户：删除自己的提问（连带该问题的回答一起删除） */
    public void deleteQuestion(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null || !q.getFarmerId().equals(UserContext.getUserId())) {
            throw new BizException("只能删除自己的提问");
        }
        questionMapper.deleteById(id);
        answerMapper.delete(new LambdaQueryWrapper<Answer>().eq(Answer::getQuestionId, id));
    }

    /** 专家：删除自己的回答（问题若无其他回答则恢复为待回答） */
    public void deleteAnswer(Long id) {
        Answer a = answerMapper.selectById(id);
        if (a == null || !a.getExpertId().equals(UserContext.getUserId())) {
            throw new BizException("只能删除自己的回答");
        }
        answerMapper.deleteById(id);
        Long count = answerMapper.selectCount(
                new LambdaQueryWrapper<Answer>().eq(Answer::getQuestionId, a.getQuestionId()));
        if (count == 0) {
            Question q = questionMapper.selectById(a.getQuestionId());
            if (q != null) {
                q.setStatus(0);
                questionMapper.updateById(q);
            }
        }
    }

    /** 专家：回答问题（问题状态改为已回答） */
    public Long answer(Long questionId, String content) {
        if (!UserContext.isRole(Roles.EXPERT)) {
            throw new BizException("只有技术专家可以回答问题");
        }
        if (StrUtil.isBlank(content)) {
            throw new BizException("回答内容不能为空");
        }
        Question q = questionMapper.selectById(questionId);
        if (q == null) {
            throw new BizException("问题不存在");
        }
        Answer a = new Answer();
        a.setQuestionId(questionId);
        a.setExpertId(UserContext.getUserId());
        a.setContent(content);
        a.setCreateTime(DateUtil.now());
        answerMapper.insert(a);
        // 问题标记为已回答
        q.setStatus(1);
        questionMapper.updateById(q);
        return a.getId();
    }

    /** 专家：待回答的问题列表 */
    public List<QuestionVO> todoQuestions() {
        List<Question> list = questionMapper.selectList(
                new LambdaQueryWrapper<Question>().eq(Question::getStatus, 0)
                        .orderByDesc(Question::getCreateTime));
        return fillQuestionVO(list);
    }

    /** 农户：我的提问 */
    public List<QuestionDetailVO> myQuestions() {
        List<Question> list = questionMapper.selectList(
                new LambdaQueryWrapper<Question>().eq(Question::getFarmerId, UserContext.getUserId())
                        .orderByDesc(Question::getCreateTime));
        return list.stream().map(q -> questionDetail(q.getId())).toList();
    }

    /** 给问题列表补充提问人和回答信息 */
    private List<QuestionVO> fillQuestionVO(List<Question> list) {
        Map<Long, User> farmerMap = userMap(list.stream().map(Question::getFarmerId).toList());
        Map<Long, List<Answer>> answerMap = list.isEmpty() ? Map.of()
                : answerMapper.selectList(new LambdaQueryWrapper<Answer>()
                        .in(Answer::getQuestionId, list.stream().map(Question::getId).toList())
                        .orderByAsc(Answer::getCreateTime))
                .stream().collect(Collectors.groupingBy(Answer::getQuestionId));
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

    /** 批量查用户 */
    private Map<Long, User> userMap(List<Long> ids) {
        List<Long> distinctIds = ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(distinctIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }
}
