package vn.io.codelearning.springapitester.scanner;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import vn.io.codelearning.springapitester.model.EndpointModel;

import java.util.List;

public class SpringEndpointScannerTestExclusionTest extends BasePlatformTestCase {

    public void testScannerExcludesTestControllersAndIncludesProductionControllers() throws Exception {
        addSpringWebAnnotations();

        // 1. Production Controller
        myFixture.addFileToProject("demo/ProductionController.java", """
                package demo;

                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                public class ProductionController {
                    @GetMapping("/api/production")
                    public String getProduction() {
                        return "prod";
                    }
                }
                """);

        // 2. Test Controller in src/test/java path
        myFixture.addFileToProject("src/test/java/demo/TestMockController.java", """
                package demo;

                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                public class TestMockController {
                    @GetMapping("/api/test-mock")
                    public String getTestMock() {
                        return "mock";
                    }
                }
                """);

        // 3. Controller annotated with test slice annotation @WebMvcTest
        myFixture.addFileToProject("org/springframework/boot/test/autoconfigure/web/servlet/WebMvcTest.java", """
                package org.springframework.boot.test.autoconfigure.web.servlet;
                public @interface WebMvcTest {}
                """);

        myFixture.addFileToProject("demo/AnnotatedTestController.java", """
                package demo;

                import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;

                @WebMvcTest
                @RestController
                public class AnnotatedTestController {
                    @GetMapping("/api/test-annotated")
                    public String getTestAnnotated() {
                        return "annotated";
                    }
                }
                """);

        List<EndpointModel> endpoints = ApplicationManager.getApplication().executeOnPooledThread(
                () -> SpringEndpointScanner.getInstance().scanEndpoints(getProject())
        ).get();

        boolean hasProductionEndpoint = endpoints.stream()
                .anyMatch(e -> "/api/production".equals(e.getPath()));
        boolean hasTestMockEndpoint = endpoints.stream()
                .anyMatch(e -> "/api/test-mock".equals(e.getPath()));
        boolean hasAnnotatedTestEndpoint = endpoints.stream()
                .anyMatch(e -> "/api/test-annotated".equals(e.getPath()));

        assertTrue("Production endpoint must be included", hasProductionEndpoint);
        assertFalse("Endpoint under src/test/java must be excluded", hasTestMockEndpoint);
        assertFalse("Endpoint with test annotation must be excluded", hasAnnotatedTestEndpoint);
    }

    private void addSpringWebAnnotations() {
        myFixture.addFileToProject("org/springframework/web/bind/annotation/RestController.java", """
                package org.springframework.web.bind.annotation;
                public @interface RestController {}
                """);
        myFixture.addFileToProject("org/springframework/web/bind/annotation/GetMapping.java", """
                package org.springframework.web.bind.annotation;
                public @interface GetMapping { String[] value() default {}; String[] path() default {}; }
                """);
    }
}
