package com.example.lambda;


import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivitiTestForMe {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private FormService formService;

    @Autowired
    private ManagementService managementService;

    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    Deployment deployment;

    @Test
    public void testAddUserAndGroup() {
        User managerUser = createUser("managerUser");
        User hrUser = createUser("hrUser");
        Group hrGroup = createGroup("hrGroup");
        Group managerGroup = createGroup("managerGroup");
//        User user = identityService.createUserQuery().userEmail("newuser@qq.com").singleResult();
//        Group group = identityService.createGroupQuery().groupType("员工组").singleResult();
        identityService.createMembership(managerUser.getId(), managerGroup.getId());
        identityService.createMembership(hrUser.getId(), hrGroup.getId());
    }

    @Test
    public void processDeploy() {
        //发起人
        String userId = "pulsek";
        //获取用户信息
        User user = identityService.createUserQuery().userId(userId).singleResult();
        //将bpmn文件部署流程
        Deployment deploy = repositoryService.createDeployment()
                .name("要加薪1")
                .addClasspathResource("processes/addSalary.bpmn")
                .deploy();
        //根据部署的流程 定义新的流程对象
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
                deploymentId(deploy.getId()).singleResult();
        //本次流程初始化
        Map<String, Object> map = new ConcurrentHashMap() {{
            put("event", "加薪");
            put("text", "入职一年，我要加薪");
            put("date", "2020-04-25");
            put("sponsor", user);
        }};
        //流程实例初始化
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), map);
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        System.out.println(task.getProcessInstanceId());

        //流程本次完成，流转至下一个阶段

    }

    @Test
    public void completeTask(){
        List<Task> taskList = taskService.createTaskQuery().taskUnassigned().list();
        for (Task task : taskList) {
            taskService.addComment(task.getId(),task.getProcessInstanceId(),"comment","通过");
            taskService.claim(task.getId(),"Gao Sir");
            taskService.complete(task.getId(),new HashMap<String, Object>(){{
                put("biaoxian","表现不错");
                put("gongzuoqiangkuang","工作情况很好");
                put("xinzi","薪资必须加");
            }});
        }

    }


    public User createUser(String userName) {
        User user = identityService.newUser(userName);
        user.setFirstName(userName);
        user.setEmail(userName + "@qq.com");
        user.setPassword(userName);
        user.setLastName(userName);
        identityService.saveUser(user);
        return user;
    }

    public Group createGroup(String groupName) {
        Group group = identityService.newGroup(groupName);
        group.setName(groupName);
        group.setType(groupName + groupName.substring(0, 1));
        identityService.saveGroup(group);
        return group;
    }


    @Test
    public void activitiTest() {
        List<User> users = identityService.createUserQuery().listPage(0, 5);
        System.out.println(users.size());
        users.forEach(user -> {
            System.out.println(user.getId());
            System.out.println(user.getFirstName());
            System.out.println(user.getEmail());
            System.out.println(user.getPassword());
        });


    }


    @Test
    public void deploymentProcess() {
        deployment = repositoryService
                .createDeployment()
                .name("涨薪流程")//创建流程名称
                .addClasspathResource("processes/addSalary.bpmn")//指定流程文件
                .deploy();

    }

    @Test
    public void startProcess() {
        //通过部署的流程id，定义一个流程
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
                deploymentId(deployment.getId()).singleResult();
        //通过定义的流程Id，开启一个流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
        Object renderedStartForm = formService.getRenderedStartForm("");
    }


    @Test
    public void findMyTask() {
        String assignee = "laoliu";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .list();
        processEngine.close();
        if (CollectionUtil.isNotEmpty(list)) {
            for (Task task : list) {
                System.out.println("任务ID" + task.getId());
                System.out.println("任务名称" + task.getName());
                System.out.println("任务创建时间" + task.getCreateTime());
                System.out.println("任务办理人" + task.getAssignee());
                System.out.println("流程实例ID" + task.getProcessInstanceId());
                System.out.println("执行对象ID" + task.getExecutionId());
                System.out.println("流程定义ID" + task.getDueDate());
                System.out.println("" + task.getProcessDefinitionId());
                System.out.println("<<=====================================>>");
            }
        }

    }


    @Test
    public void testProcess() {
        log.info("启动程序");

        // 创建流程引擎
        ProcessEngineConfiguration cfg = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        ProcessEngine processEngine = cfg.buildProcessEngine();
        String name = processEngine.getName();
        String version = ProcessEngine.VERSION;

        log.info("流程引擎名称:{},版本:{}", name, version);

        // 部署流程定义文件
        RepositoryService repositoryService = processEngine.getRepositoryService();
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        deploymentBuilder.addClasspathResource("second_approve.bpmn20.xml");
        Deployment deployment = deploymentBuilder.deploy();
        String deploymentId = deployment.getId();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
                deploymentId(deploymentId).singleResult();

        log.info("流程文件:{},流程定义id:{}", processDefinition.getName(), processDefinition.getId());

        // 启动流程
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
        log.info("流程实例id为:{}", processInstance.getId());

        // 处理任务
        Scanner scanner = new Scanner(System.in);
        while (processInstance != null && !processInstance.isEnded()) {

            TaskService taskService = processEngine.getTaskService();
            List<Task> taskList = taskService.createTaskQuery().list();

            for (Task task : taskList) {
                log.info("待处理任务:{}", task.getName());
                FormService formService = processEngine.getFormService();
                TaskFormData taskFormData = formService.getTaskFormData(task.getId());
                List<FormProperty> formPropertyList = taskFormData.getFormProperties();
                Map<String, Object> variables = new HashMap<String, Object>();
                for (FormProperty formProperty : formPropertyList) {
                    String line = null;
                    if (StringFormType.class.isInstance(formProperty.getType())) {
                        log.info("请输入{}:", formProperty.getName());
                        line = scanner.nextLine();
                        variables.put(formProperty.getId(), line);
                    } else if (DateFormType.class.isInstance(formProperty.getType())) {
                        log.info("请输入{}:(格式:yyyy-MM-dd)", formProperty.getName());
                        line = scanner.nextLine();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = new Date();
                        try {
                            date = dateFormat.parse(line);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        variables.put(formProperty.getId(), date);
                    }
                    log.info("您输入的内容是:{}", line);
                }

                taskService.complete(task.getId(), variables);
            }
            log.info("待处理任务数量有{}个", taskList.size());
        }

        log.info("结束程序");
    }


}
