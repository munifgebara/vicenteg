/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.munif.vicente.tools.vicente.maven.plugin;

import br.com.munif.framework.vicente.domain.BaseEntity;
import br.com.munif.framework.vicente.domain.tenancyfields.VicTenancyFieldsBaseEntity;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import javax.persistence.GeneratedValue;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author munif
 */
public class Util {

    public final static String IDENTACAO04 = "    ";
    public final static String IDENTACAO08 = "        ";
    public final static String IDENTACAO12 = "            ";
    public final static String IDENTACAO16 = "                ";
    public final static String IDENTACAO20 = "                    ";
    public final static String IDENTACAO24 = "                        ";
    public final static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public final static SimpleDateFormat fsdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    public static String primeiraMaiuscula(String s) {
        return s.substring(0, 1).toUpperCase().concat(s.substring(1));
    }

    public static String primeiraMinuscula(String s) {
        return s.substring(0, 1).toLowerCase().concat(s.substring(1));
    }

    public static List<Field> getTodosAtributosMenosIdAutomatico(Class classe) {
        List<Field> todosAtributos = getTodosAtributosNaoEstaticos(classe);
        Field aRemover = null;
        Field aRemoverOi = null;
        Field aRemoverVersion = null;
        Field aRemoverGumgaCustomFields = null;
        for (Field f : todosAtributos) {
            if ("id".equals(f.getName())) {
                aRemover = f;
            }
            if ("oi".equals(f.getName())) {
                aRemoverOi = f;
            }
            if ("version".equals(f.getName())) {
                aRemoverVersion = f;
            }
            if ("gumgaCustomFields".equals(f.getName())) {
                aRemoverGumgaCustomFields = f;
            }
        }
        if (aRemover != null) {
            todosAtributos.remove(aRemover);
        }
        if (aRemoverOi != null) {
            todosAtributos.remove(aRemoverOi);
        }
        if (aRemoverVersion != null) {
            todosAtributos.remove(aRemoverVersion);
        }
        if (aRemoverGumgaCustomFields != null) {
            todosAtributos.remove(aRemoverGumgaCustomFields);
            todosAtributos.add(aRemoverGumgaCustomFields);
            System.out.println(todosAtributos);
        }

        return todosAtributos;
    }

    public static List<Field> getTodosAtributosNaoEstaticos(Class classe) throws SecurityException {
        List<Field> aRetornar = new ArrayList<Field>();
        List<Field> aRemover = new ArrayList<Field>();
        if (!classe.getSuperclass().equals(Object.class)) {
            aRetornar.addAll(getTodosAtributosNaoEstaticos(classe.getSuperclass()));
        }
        aRetornar.addAll(Arrays.asList(classe.getDeclaredFields()));
        for (Field f : aRetornar) {
            if (Modifier.isStatic(f.getModifiers())) {
                aRemover.add(f);
            }
            if (f.getName().equals("version")) {
                aRemover.add(f);
            }
            if (f.getName().equals("oi")) {
                aRemover.add(f);
            }
        }
        aRetornar.removeAll(aRemover);
        return aRetornar;
    }

    public static List<Field> getTodosAtributosNaoBase(Class classe) throws SecurityException {
        List<Field> aRetornar = new ArrayList<Field>();
        List<Field> aRemover = new ArrayList<Field>();
        if (!classe.getSuperclass().equals(BaseEntity.class) && !classe.getSuperclass().equals(VicTenancyFieldsBaseEntity.class)) {
            aRetornar.addAll(getTodosAtributosNaoBase(classe.getSuperclass()));
        }
        aRetornar.addAll(Arrays.asList(classe.getDeclaredFields()));
        for (Field f : aRetornar) {
            if (Modifier.isStatic(f.getModifiers())) {
                aRemover.add(f);
            }
        }
        aRetornar.removeAll(aRemover);
        return aRetornar;
    }

    public static ClassLoader getClassLoader(MavenProject project) {
        ClassLoader aRetornar = null;
        try {
            List elementos = new ArrayList();
            elementos.addAll(project.getRuntimeClasspathElements());
            elementos.addAll(project.getTestClasspathElements());

            URL[] runtimeUrls = new URL[elementos.size()];
            for (int i = 0; i < elementos.size(); i++) {
                String element = (String) elementos.get(i);
                runtimeUrls[i] = new File(element).toURI().toURL();
            }
            aRetornar = new URLClassLoader(runtimeUrls,
                    Thread.currentThread().getContextClassLoader());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return aRetornar;
    }

    public static Field primeiroAtributo(Class classe) {
        return getTodosAtributosNaoBase(classe).get(0);
    }

    public static Class getTipoGenerico(Field atributo) {
        Class tipoGenerico;
        if (atributo.getGenericType() instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) atributo.getGenericType();
            Type[] typeArguments = type.getActualTypeArguments();
            tipoGenerico = (Class) typeArguments[atributo.getType().equals(Map.class) ? 1 : 0];
        } else {
            tipoGenerico = atributo.getType();
        }
        return tipoGenerico;
    }

    public static String etiqueta(Field atributo) {
        return primeiraMaiuscula(atributo.getName());
    }

    public static String windowsSafe(String s) {
        return s.replaceAll("\\\\", "/");
    }

    public static void adicionaLinha(String nomeArquivo, String linhaMarcador, String linhaNova) throws IOException {
        String arquivo = nomeArquivo;
        if (!new File(arquivo).exists()){
            return;
        }
        String arquivoTmp = nomeArquivo + "-tmp";

        BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoTmp));
        BufferedReader reader = new BufferedReader(new FileReader(arquivo));

        String linha;
        boolean colocou = false;
        while ((linha = reader.readLine()) != null) {
            if (linha.contains(linhaMarcador) && !colocou) {
                writer.write(linhaNova + "\n");
                colocou = true;
            }
            writer.write(linha + "\n");
        }

        writer.close();
        reader.close();

        new File(arquivo).delete();
        new File(arquivoTmp).renameTo(new File(arquivo));
    }

    public static String todosAtributosSeparadosPorVirgula(Class classeEntidade) {
        StringBuilder sb = new StringBuilder();
        for (Field f : getTodosAtributosMenosIdAutomatico(classeEntidade)) {
            sb.append(f.getName() + ",");

        }
        sb.setLength(sb.length() - 1);

        return sb.toString().replace("oi,", "");
    }

    public static String hoje() {
        return sdf.format(new Date());
    }

    public static void escreveCabecario(FileWriter fw) throws IOException {
        fw.write("/*\n"
                + "* Gerado automaticamente por GUMGAGenerator em " + hoje() + "\n"
                + "*/\n"
                + "\n");
    }

    public static String dependenciasSeparadasPorVirgula(Set<Class> dependencias, String sufixo, boolean apostrofe) {
        StringBuilder sb = new StringBuilder();
        for (Class clazz : dependencias) {
            sb.append(", " + (apostrofe ? "'" : "") + clazz.getSimpleName() + sufixo + (apostrofe ? "'" : ""));
        }
        return sb.toString();
    }

    static Class getClassLoader(String nce) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static FileWriter abreArquivo(String arquivo, boolean b) throws IOException {
        if (new File(arquivo).exists()) {
            BufferedReader brTest = new BufferedReader(new FileReader(arquivo));
            String firstLine = brTest.readLine();
            if (firstLine == null) {
//                System.out.println("--->" + arquivo + " NULL");
            } else if (firstLine.contains("VICIGNORE")) {
                String ignorados = System.getProperty("user.home") + "/VICIGNORE/";
                new File(ignorados).mkdir();
                String at = fsdf.format(new Date()) + "_" + arquivo.substring(arquivo.indexOf("src")).replaceAll("/", "__");
                System.out.println("-i->" + arquivo + " " + at + " " + ignorados);
                return new FileWriter(new File(ignorados, at), false);
            }
            brTest.close();
        }
        
        

        FileWriter fileWriter = new FileWriter(arquivo, b);
        //VICIGNORE
        String extensao = arquivo.substring(arquivo.lastIndexOf(".") + 1);
        String comentario = null;
        switch (extensao) {
            case "java":
                comentario = "/* %s */\n";
                break;
            case "ts":
                comentario = "/* %s */\n";
                break;
            case "html":
                comentario = "<!-- %s -->\n";
                break;
            case "css":
                comentario = "/* %s */\n";
                break;

            default:
                System.out.println("----> Nao sei comentar " + extensao);
        }
        if (comentario != null) {
            fileWriter.write(String.format(comentario, "Arquivo gerado utilizando VICGERADOR por " + System.getProperty("user.name") + " as " + sdf.format(new Date())));
            fileWriter.write(String.format(comentario, "Para não gerar o arquivo novamente coloque na primeira linha um comentário com  VICIGNORE , pode ser essa mesmo"));
        }

        return fileWriter;

    }

}
