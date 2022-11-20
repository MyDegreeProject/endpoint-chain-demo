package com.wust.endpoint.chain.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fluidops.fedx.Config;
import com.fluidops.fedx.DefaultEndpointListProvider;
import com.fluidops.fedx.FedXFactory;
import com.fluidops.fedx.sail.FedXSailRepository;
import com.fluidops.fedx.structures.QueryInfo;
import com.wust.endpoint.chain.enums.OpenStatusEnum;
import com.wust.endpoint.chain.persist.entity.EndpointInfoEntity;
import com.wust.endpoint.chain.persist.entity.EndpointUserEntity;
import com.wust.endpoint.chain.persist.entity.FileEntity;
import com.wust.endpoint.chain.persist.mapper.EndpointInfoMapper;
import com.wust.endpoint.chain.persist.mapper.EndpointUserMapper;
import com.wust.endpoint.chain.response.EndpointResponse;
import com.wust.endpoint.chain.service.EndpointService;
import com.wust.endpoint.chain.quetsal.summary.TBSSSummariesGenerator;
import com.wust.endpoint.chain.service.EndpointSummaryDataService;
import com.wust.endpoint.chain.service.EndpointUserService;
import com.wust.endpoint.chain.service.FileService;
import com.wust.endpoint.chain.util.IPFSUtil;
import com.wust.endpoint.chain.util.PathUtils;
import com.wust.endpoint.chain.util.SnowflakeUtils;
import com.wust.endpoint.chain.vo.req.DataFileReq;
import com.wust.endpoint.chain.vo.req.DataSaveReq;
import com.wust.endpoint.chain.vo.req.EndpointReq;
import io.ipfs.api.IPFS;
import io.ipfs.multiaddr.MultiAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置端点表 服务实现类
 * @author xujiao
 * @since 2022-02-20
 */
@Slf4j
@Service
public class EndpointServiceImpl extends ServiceImpl<EndpointInfoMapper, EndpointInfoEntity> implements EndpointService {

    @Value("${store.dir:null}")
    private String storeDir;

    @Autowired
    private FileService fileService;
    @Autowired
    private EndpointSummaryDataService summaryDataService;
    @Autowired
    private EndpointUserMapper userMapper;

    @Override
    public EndpointResponse endpointList(EndpointReq endpointReq) {
        LambdaQueryWrapper<EndpointInfoEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(endpointReq.getEndpointName())) {
            wrapper.eq(EndpointInfoEntity::getEndpointName, endpointReq.getEndpointName());
        }
        EndpointUserEntity userEntity = userMapper.selectById(endpointReq.getUserId());
        if(!"admin".equals(userEntity.getUname())){
            wrapper.eq(EndpointInfoEntity::getOrgName,userEntity.getOrgName());
        }
        wrapper.orderByDesc(EndpointInfoEntity::getId);
        IPage<EndpointInfoEntity> iPage = new Page<>(endpointReq.getCurrentPage(), endpointReq.getPageSize());
        IPage<EndpointInfoEntity> data = this.page(iPage, wrapper);
        return EndpointResponse.ok(data);
    }

    @Override
    public EndpointResponse selectList() {
        List<EndpointInfoEntity> endpointList = this.list();
        return EndpointResponse.ok(endpointList);
    }

    @Override
    public EndpointInfoEntity getEndpointById(Integer productId) {
        return this.getById(productId);
    }

    @Override
    public EndpointResponse addEndpoint(EndpointReq endpointReq) {
        LambdaQueryWrapper<EndpointInfoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EndpointInfoEntity::getEndpointUrl, endpointReq.getEndpointUrl());
        int count = this.count(wrapper);
        if (count > 0) {
            log.warn("端点地址={}已存在,不能重复添加", endpointReq.getEndpointUrl());
            return EndpointResponse.error("端点名称已存在");
        }
        EndpointInfoEntity endpointInfoEntity = new EndpointInfoEntity();
        endpointInfoEntity.setEndpointUrl(endpointReq.getEndpointUrl());
        endpointInfoEntity.setEndpointName(endpointReq.getEndpointName());
        endpointInfoEntity.setOpenStatus(OpenStatusEnum.START.code);
        //获取用户对应的机构
        EndpointUserEntity userEntity = userMapper.selectById(endpointReq.getUserId());
        endpointInfoEntity.setOrgName(userEntity.getOrgName());
        this.save(endpointInfoEntity);
        return EndpointResponse.ok();
    }

    @Override
    public EndpointResponse modifyEndpoint(EndpointReq endpointReq) {
        LambdaQueryWrapper<EndpointInfoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(EndpointInfoEntity::getId, endpointReq.getId());
        wrapper.eq(EndpointInfoEntity::getEndpointName, endpointReq.getEndpointName());
        wrapper.eq(EndpointInfoEntity::getEndpointUrl, endpointReq.getEndpointUrl());
        int count = this.count(wrapper);
        if (count > 0) {
            log.warn("端点名称={}已存在,不能修改", endpointReq.getEndpointName());
            return EndpointResponse.error("端点名称和地址已存在");
        }
        EndpointInfoEntity endpointInfoEntity = new EndpointInfoEntity();
        endpointInfoEntity.setId(endpointReq.getId());
        endpointInfoEntity.setEndpointName(endpointReq.getEndpointName());
        endpointInfoEntity.setEndpointUrl(endpointReq.getEndpointUrl());
        //获取用户对应的机构
        EndpointUserEntity userEntity = userMapper.selectById(endpointReq.getUserId());
        endpointInfoEntity.setOrgName(userEntity.getOrgName());
        this.updateById(endpointInfoEntity);
        return EndpointResponse.ok();
    }

    @Override
    @Transactional
    public EndpointResponse generatorSummaries(EndpointReq endpointReq) throws IOException {
        List<String> endpointsTest = new ArrayList<>();
        endpointsTest.add(endpointReq.getEndpointUrl());
/*        File f = new File("");
        String path = f.getCanonicalPath();
        String fullPath = PathUtils.concat(path,storeDir);*/
        String fullPath = storeDir;
        String fileName = endpointReq.getEndpointName()+".n3";
        String outputFile = PathUtils.concat(fullPath, fileName);
        String namedGraph = null;
        TBSSSummariesGenerator generator = new TBSSSummariesGenerator(outputFile);
        long startTime = System.currentTimeMillis();
        int branchLimit = 4;
        generator.generateSummaries(endpointsTest, namedGraph, branchLimit);
        log.info("Data Summaries Generation Time (min): " + (double)(System.currentTimeMillis() - startTime) / (1000 * 60));
        log.info("Data Summaries are secessfully stored at " + outputFile);
        //摘要上传到IPFS并保存记录
        IPFS ipfs = new IPFS(new MultiAddress("/ip4/47.108.202.60/tcp/9091"));
        String cid = IPFSUtil.upload(ipfs,outputFile);
        log.info("上传到ipfs后的cid：{}",cid);
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileNo(cid);
        fileEntity.setAttachName(fileName);
        fileEntity.setAttachPath(outputFile);
        fileService.save(fileEntity);
        //摘要hash上区块链存储
        /*File file = new File(outputFile);
        InputStream inputStream = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(file.getName(), inputStream);
        EndpointResponse response = fileService.uploadFile(multipartFile);*/

        DataSaveReq dataSaveReq = new DataSaveReq();
        dataSaveReq.setEndpointId(endpointReq.getId());
        dataSaveReq.setFilePath(outputFile);
        DataFileReq dataFileReq = new DataFileReq();
        dataFileReq.setFileName(fileName);
        dataFileReq.setFileHash(cid);
        dataSaveReq.setDataFileReq(dataFileReq);
        summaryDataService.bqSave(dataSaveReq);
        return EndpointResponse.ok();
    }

    @Override
    public void sparqlQuery(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        String accept = request.getHeader("Accept");
        if (StringUtils.isEmpty(accept)) {
            accept = "application/sparql-results+json";
        }
        //先获取注册的端点集合
        List<EndpointInfoEntity> endpointList = this.list();
        List<String> endpointUrlList = new ArrayList<>();
        endpointList.forEach(v->{
            endpointUrlList.add(v.getEndpointUrl());
        });
        //读取配置文件
        Config config = new Config("/root/endpoint-chain-demo/costfed.props");
        //Config config = new Config("D:/baby/endpoint-chain-demo/costfed.props");
        FedXSailRepository rep = FedXFactory.initializeFederation(config, new DefaultEndpointListProvider(endpointUrlList));
        try {
            OutputStream outputStream = response.getOutputStream();
            RepositoryConnection conn = rep.getConnection();
            String curQuery = request.getParameter("query");
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, curQuery);
            long startTime = System.currentTimeMillis();
            TupleQueryResult res = tupleQuery.evaluate();
            long count = 0;
            while (res.hasNext()) {
                BindingSet row = res.next();
                count++;
            }
            long runTime = System.currentTimeMillis() - startTime;
            log.info("Query exection time (msec): "+ runTime + ", Total Number of Records: " + count + ", Source count: " + QueryInfo.queryInfo.get().numSources.longValue());
            try(TupleQueryResult queryResult = tupleQuery.evaluate()) {
                if (accept.contains("application/sparql-results+json")) {
                    QueryResults.report(queryResult, new SPARQLResultsJSONWriter(outputStream));
                } else if (accept.contains("application/sparql-results+xml")) {
                    QueryResults.report(queryResult, new SPARQLResultsXMLWriter(outputStream));
                } else {
                    QueryResults.report(queryResult, new SPARQLResultsJSONWriter(outputStream));
                }
            }
        } catch (Exception e) {
            log.error("", e);
            e.printStackTrace();
        } finally {
            rep.shutDown();
        }
    }

    @Override
    public List<Integer> getEndpointIdsByUserId(Integer userId) {
        List<Integer> ids = new ArrayList<>();
        EndpointUserEntity userEntity = userMapper.selectById(userId);
        LambdaQueryWrapper<EndpointInfoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EndpointInfoEntity::getOrgName,userEntity.getOrgName());
        List<EndpointInfoEntity> endpointInfoEntityList = this.baseMapper.selectList(wrapper);
        endpointInfoEntityList.forEach(v->{
            ids.add(v.getId());
        });
        return ids;
    }
}
