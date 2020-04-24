package com.example.lambda.config;

import com.zaxxer.hikari.HikariDataSource;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * @ClassName ActivitiConfig
 * @Description TODO
 * @Author Chao.Qin
 * @Datw 2019/8/28 15:50
 */
@Configuration
public class ActivitiConfig extends AbstractProcessEngineAutoConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource activitiDataSource(DataSourceProperties properties) {
        return DataSourceBuilder.create(properties.getClassLoader())
                .type(HikariDataSource.class)
                .driverClassName(properties.determineDriverClassName())
                .url(properties.determineUrl())
                .username(properties.determineUsername())
                .password(properties.determinePassword())
                .build();
    }

    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(PlatformTransactionManager transactionManager, SpringAsyncExecutor executor, DataSourceProperties properties) throws IOException {
        return baseSpringProcessEngineConfiguration(activitiDataSource(properties), transactionManager, executor);
    }

    //    @Autowired
//    private ProcessEngine processEngine;


//    @Bean
//    public void fingVersionProcesses() {
//        List<ProcessDefinition> processDefinitionList = processEngine.getRepositoryService()
//                .createProcessDefinitionQuery()
//                .orderByProcessDefinitionVersion()
//                .asc()
//                .list();
//    }
}
