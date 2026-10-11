package vn.io.codelearning.springapitester.scanner;

import org.junit.Assert;
import org.junit.Test;

public class SpringTestFilterUtilsTest {

    @Test
    public void testIsTestPathIdentifiesTestDirectories() {
        Assert.assertTrue(SpringTestFilterUtils.isTestPath("/workspace/my-app/src/test/java/com/example/TestController.java"));
        Assert.assertTrue(SpringTestFilterUtils.isTestPath("/workspace/my-app/src/test/resources/application-test.yml"));
        Assert.assertTrue(SpringTestFilterUtils.isTestPath("/workspace/my-app/src/testFixtures/java/com/example/Fixtures.java"));
        Assert.assertTrue(SpringTestFilterUtils.isTestPath("/workspace/my-app/src/integrationTest/java/com/example/ItTest.java"));
        Assert.assertTrue(SpringTestFilterUtils.isTestPath("/workspace/my-app/src/integration-test/java/com/example/ItTest.java"));
        Assert.assertTrue(SpringTestFilterUtils.isTestPath("/workspace/my-app/test/java/com/example/TestController.java"));
        Assert.assertTrue(SpringTestFilterUtils.isTestPath("/workspace/my-app/test/resources/test.properties"));
        Assert.assertTrue(SpringTestFilterUtils.isTestPath("/workspace/my-app/test/kotlin/com/example/TestController.kt"));
        Assert.assertTrue(SpringTestFilterUtils.isTestPath("/workspace/my-app/test/groovy/com/example/TestController.groovy"));

        // Production paths should not be treated as test paths
        Assert.assertFalse(SpringTestFilterUtils.isTestPath("/workspace/my-app/src/main/java/com/example/UserController.java"));
        Assert.assertFalse(SpringTestFilterUtils.isTestPath("/workspace/my-app/src/main/resources/application.properties"));
        Assert.assertFalse(SpringTestFilterUtils.isTestPath("/workspace/test-runner/src/main/java/com/example/Main.java"));
        Assert.assertFalse(SpringTestFilterUtils.isTestPath(null));
        Assert.assertFalse(SpringTestFilterUtils.isTestPath(""));
    }

    @Test
    public void testIsTestModule() {
        com.intellij.openapi.module.Module mainModule = new MockModule("order-service.main");
        com.intellij.openapi.module.Module testModule = new MockModule("order-service.test");
        com.intellij.openapi.module.Module hyphenTestModule = new MockModule("order-service-test");
        com.intellij.openapi.module.Module itModule = new MockModule("order-service.integrationTest");
        com.intellij.openapi.module.Module regularModule = new MockModule("order-service");

        Assert.assertTrue(SpringTestFilterUtils.isTestModule(testModule));
        Assert.assertTrue(SpringTestFilterUtils.isTestModule(hyphenTestModule));
        Assert.assertTrue(SpringTestFilterUtils.isTestModule(itModule));
        Assert.assertFalse(SpringTestFilterUtils.isTestModule(mainModule));
        Assert.assertFalse(SpringTestFilterUtils.isTestModule(regularModule));
        Assert.assertFalse(SpringTestFilterUtils.isTestModule(null));
    }

    private static class MockModule extends com.intellij.mock.MockModule {
        private final String name;

        public MockModule(String name) {
            super(new com.intellij.openapi.Disposable() {
                @Override
                public void dispose() {}
            });
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
