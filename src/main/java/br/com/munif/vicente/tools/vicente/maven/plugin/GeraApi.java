package br.com.munif.vicente.tools.vicente.maven.plugin;

import br.com.munif.framework.vicente.core.Utils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Entity;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author munif
 */
@Mojo(name = "api", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraApi extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "entidade", defaultValue = "all")
    private String nomeCompletoEntidade;
    private String nomePacoteBase;
    private String nomeEntidade;
    private ClassLoader classLoader;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        classLoader = Util.getClassLoader(project);
        try {
            if ("all".equals(nomeCompletoEntidade)) {
                List<Class> onlyEntities = scanClasses();
                for (Class c : onlyEntities) {
                    geraRepositoryServiceApi(c.getCanonicalName());
                }

            } else {
                geraRepositoryServiceApi(nomeCompletoEntidade);
            }
        } catch (IOException ex) {
            Logger.getLogger(GeraApi.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void geraRepositoryServiceApi(String nce) throws IOException {
        nomePacoteBase = nce.substring(0, nce.lastIndexOf(".domain"));
        nomeEntidade = nce.substring(nce.lastIndexOf('.') + 1);
        getLog().info("nomeCompletoEntidade:" + nce);
        geraRepository(nce, nomePacoteBase, nomeEntidade);
        geraService(nce, nomePacoteBase, nomeEntidade);
        geraAPI(nce, nomePacoteBase, nomeEntidade);
    }

    private void geraRepository(String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String arquivoRepository = Util.windowsSafe(project.getFile().getParent() + "/src/main/java/" + (npb.replaceAll("\\.", "/")) + "/repository/" + ne + "Repository.java");
        fw = new FileWriter(arquivoRepository, false);
        fw.write(""
                + "package " + npb + ".repository;\n"
                + "\n"
                + "import br.com.munif.framework.vicente.application.VicRepository;\n"
                + "import " + nce + ";\n"
                + "import org.springframework.stereotype.Repository;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author GeradorVicente\n"
                + " */\n"
                + "@SuppressWarnings(\"unused\")\n"
                + "@Repository\n"
                + "public interface " + ne + "Repository extends VicRepository<" + ne + ">{\n"
                + "    \n"
                + "}\n"
                + ""
        );
        fw.close();

    }

    private void geraService(String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String arquivoRepository = Util.windowsSafe(project.getFile().getParent() + "/src/main/java/" + (npb.replaceAll("\\.", "/")) + "/service/" + ne + "Service.java");
        fw = new FileWriter(arquivoRepository, false);
        fw.write(""
                + "package " + npb + ".service;\n"
                + "\n"
                + "import br.com.munif.framework.vicente.application.BaseService;\n"
                + "import br.com.munif.framework.vicente.application.VicRepository;\n"
                + "import " + nce + ";\n"
                + "import org.springframework.stereotype.Service;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author GeradorVicente\n"
                + " */\n"
                + "@Service\n"
                + "public class " + ne + "Service extends BaseService<" + ne + ">{\n"
                + "    \n"
                + "    public " + ne + "Service(VicRepository<" + ne + "> repository) {\n"
                + "        super(repository);\n"
                + "    }\n"
                + "    \n"
                + "}\n"
                + ""
        );
        fw.close();
    }

    private void geraAPI(String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String arquivoRepository = Util.windowsSafe(project.getFile().getParent() + "/src/main/java/" + (npb.replaceAll("\\.", "/")) + "/api/" + ne + "Api.java");
        fw = new FileWriter(arquivoRepository, false);
        fw.write(""
                + "package " + npb + ".api;\n"
                + "\n"
                + "import br.com.munif.framework.vicente.api.BaseAPI;\n"
                + "import br.com.munif.framework.vicente.application.BaseService;\n"
                + "import " + nce + ";\n"
                + "import org.springframework.web.bind.annotation.RequestMapping;\n"
                + "import org.springframework.web.bind.annotation.RestController;\n"
                + "import org.apache.log4j.Logger;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author GeradorVicente\n"
                + " */\n"
                + "@RestController\n"
                + "@RequestMapping(\"/api/" + ne.toLowerCase() + "\")\n"
                + "public class " + ne + "Api extends BaseAPI<" + ne + "> {\n"
                + "\n"
                + "    private final Logger log = Logger.getLogger(" + ne + "Api.class);\n"
                + "\n"
                + "    private static final String ENTITY_NAME = \"" + ne.toLowerCase() + "\";\n"
                + "\n"
                + "    public " + ne + "Api(BaseService<" + ne + "> service) {\n"
                + "        super(service);\n"
                + "    }\n"
                + "    \n"
                + "\n"
                + "}\n"
                + ""
        );
        fw.close();

    }

    private String getClassName(File f, File baseFolder) {
        String compleName = Utils.windowsSafe(f.getAbsolutePath());
        String baseFolderName = Utils.windowsSafe(baseFolder.getAbsolutePath());
        return compleName.replaceFirst(baseFolderName, "").replace(".class", "").replaceAll("/", ".").replaceFirst(".", "");
    }

    public List<Class> scanFolder(File folder, File baseFolder) {
        List<Class> toReturn = new ArrayList<>();
        File[] fs = folder.listFiles();
        for (File f : fs) {
            if (f.isDirectory()) {
                toReturn.addAll(scanFolder(f, baseFolder));
            } else {
                String name = f.getName();
                if (name.endsWith(".class")) {
                    String className = getClassName(f, baseFolder);
                    try {
                        Class c = classLoader.loadClass(className);
                        if (c.isAnnotationPresent(Entity.class)) {
                            toReturn.add(c);
                        }
                    } catch (ClassNotFoundException ex) {
                        getLog().error(ex);
                    }
                }
            }
        }
        return toReturn;
    }

    public List<Class> scanClasses() throws IOException {
        List<Class> toReturn = new ArrayList<>();
        File f = new File(project.getFile().getParent() + "/target/classes/");
        toReturn.addAll(scanFolder(f, f));
        return toReturn;
    }

}
