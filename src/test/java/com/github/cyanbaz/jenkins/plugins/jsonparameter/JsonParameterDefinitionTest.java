package com.github.cyanbaz.jenkins.plugins.jsonparameter;

/*@WithJenkins
class JsonParameterDefinitionTest {

    final String name = "Bobby";

    @Test
    void testConfigRoundtrip(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new JsonParameterDefinition(name));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(
                new JsonParameterDefinition(name), project.getBuildersList().get(0));
    }

    @Test
    void testConfigRoundtripFrench(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        JsonParameterDefinition builder = new JsonParameterDefinition(name);
        builder.setUseFrench(true);
        project.getBuildersList().add(builder);
        project = jenkins.configRoundtrip(project);

        JsonParameterDefinition lhs = new JsonParameterDefinition(name);
        lhs.setUseFrench(true);
        jenkins.assertEqualDataBoundBeans(lhs, project.getBuildersList().get(0));
    }

    @Test
    void testBuild(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        JsonParameterDefinition builder = new JsonParameterDefinition(name);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Hello, " + name, build);
    }

    @Test
    void testBuildFrench(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        JsonParameterDefinition builder = new JsonParameterDefinition(name);
        builder.setUseFrench(true);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Bonjour, " + name, build);
    }

    @Test
    void testScriptedPipeline(JenkinsRule jenkins) throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "node {greet '" + name + "'}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "Hello, " + name + "!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }
}*/
