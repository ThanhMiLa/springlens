package vn.io.codelearning.springapitester.scanner;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.TestSourcesFilter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.DelegatingGlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Tiện ích nhận diện và lọc bỏ mã nguồn kiểm thử (test sources, test resources, test annotations).
 */
public final class SpringTestFilterUtils {

    private SpringTestFilterUtils() {
    }

    /**
     * Trả về GlobalSearchScope chỉ gồm mã nguồn production của project (loại bỏ toàn bộ test và thư viện).
     */
    public static GlobalSearchScope getProjectProductionScope(@Nullable Project project) {
        if (project == null || project.isDisposed()) {
            return GlobalSearchScope.EMPTY_SCOPE;
        }
        GlobalSearchScope baseScope = GlobalSearchScope.projectScope(project);
        return new ProductionDelegatingScope(baseScope, project);
    }

    /**
     * Trả về GlobalSearchScope cho module chỉ chứa production runtime scope (không chứa test).
     */
    public static GlobalSearchScope getModuleProductionScope(@Nullable Module module, @Nullable Project project) {
        if (module != null && !module.isDisposed()) {
            if (isTestModule(module)) {
                return GlobalSearchScope.EMPTY_SCOPE;
            }
            GlobalSearchScope baseScope = GlobalSearchScope.moduleRuntimeScope(module, false);
            return new ProductionDelegatingScope(baseScope, project);
        }
        return getProjectProductionScope(project);
    }

    /**
     * Kiểm tra xem module có phải là test module (ví dụ: module .test sinh ra bởi Gradle) hay không.
     */
    public static boolean isTestModule(@Nullable Module module) {
        if (module == null) return false;
        String name = module.getName();
        return name.endsWith(".test") || name.endsWith("-test") || name.endsWith(".integrationTest");
    }

    /**
     * Kiểm tra xem một VirtualFile có thuộc về test (test source root, test resource root, hoặc đường dẫn test) hay không.
     */
    public static boolean isTestFile(@Nullable VirtualFile file, @Nullable Project project) {
        if (file == null) return false;

        if (project != null && !project.isDisposed()) {
            try {
                if (TestSourcesFilter.isTestSources(file, project)) {
                    return true;
                }
                ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
                if (fileIndex.isInTestSourceContent(file)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        String path = file.getPath();
        return isTestPath(path);
    }

    /**
     * Kiểm tra đường dẫn có thuộc cấu trúc thư mục test hay không (Maven, Gradle, v.v.).
     */
    public static boolean isTestPath(@Nullable String path) {
        if (path == null || path.isBlank()) return false;
        return path.contains("/src/test/")
                || path.contains("/src/testFixtures/")
                || path.contains("/src/integrationTest/")
                || path.contains("/src/integration-test/")
                || path.contains("/test/java/")
                || path.contains("/test/resources/")
                || path.contains("/test/kotlin/")
                || path.contains("/test/groovy/");
    }

    /**
     * Kiểm tra xem một PsiElement / PsiClass có nằm trong file test hoặc chứa annotation test hay không.
     */
    public static boolean isTestElement(@Nullable PsiElement element, @Nullable Project project) {
        if (element == null) return false;
        PsiFile psiFile = element.getContainingFile();
        if (psiFile == null) return false;

        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            virtualFile = psiFile.getOriginalFile().getVirtualFile();
        }
        if (virtualFile != null && isTestFile(virtualFile, project)) {
            return true;
        }

        if (element instanceof PsiClass psiClass && hasTestAnnotations(psiClass)) {
            return true;
        }

        return false;
    }

    /**
     * Kiểm tra xem PsiClass có chứa các annotation đặc trưng cho test framework hay không.
     */
    public static boolean hasTestAnnotations(@Nullable PsiClass psiClass) {
        if (psiClass == null) return false;
        for (PsiAnnotation annotation : psiClass.getAnnotations()) {
            String qName = annotation.getQualifiedName();
            if (qName != null && (qName.startsWith("org.springframework.boot.test.")
                    || qName.endsWith(".SpringBootTest")
                    || qName.endsWith(".WebMvcTest")
                    || qName.endsWith(".DataJpaTest")
                    || qName.endsWith(".RestClientTest")
                    || qName.endsWith(".JsonTest")
                    || qName.endsWith(".TestConfiguration")
                    || qName.endsWith(".TestComponent"))) {
                return true;
            }
        }
        return false;
    }

    private static final class ProductionDelegatingScope extends DelegatingGlobalSearchScope {
        private final Project project;

        private ProductionDelegatingScope(@NotNull GlobalSearchScope baseScope, @Nullable Project project) {
            super(baseScope);
            this.project = project;
        }

        @Override
        public boolean contains(@NotNull VirtualFile file) {
            return super.contains(file) && !isTestFile(file, project);
        }

        @Override
        public boolean isSearchInModuleContent(@NotNull Module aModule, boolean testSources) {
            if (testSources || isTestModule(aModule)) {
                return false;
            }
            return super.isSearchInModuleContent(aModule, false);
        }

        @Override
        public boolean isSearchInLibraries() {
            return false;
        }
    }
}
