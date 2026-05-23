package org.example.enterprisebacksystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.enterprisebacksystem.domain.OaProcess;
import org.example.enterprisebacksystem.mapper.OaProcessMapper;
import org.example.enterprisebacksystem.service.OaProcessService;
import org.springframework.stereotype.Service;

@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, OaProcess> implements OaProcessService {
}
