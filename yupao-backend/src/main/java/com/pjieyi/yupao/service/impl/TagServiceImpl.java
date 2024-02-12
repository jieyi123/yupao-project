package com.pjieyi.yupao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pjieyi.yupao.model.entity.Tag;
import com.pjieyi.yupao.service.TagService;
import com.pjieyi.yupao.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author pjy17
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2024-02-12 16:16:31
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




