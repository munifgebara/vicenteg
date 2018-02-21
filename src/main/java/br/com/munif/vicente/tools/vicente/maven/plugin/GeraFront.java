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
@Mojo(name = "front", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraFront extends AbstractMojo {

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
                    geraFront(c.getCanonicalName());
                }

            } else {
                geraFront(nomeCompletoEntidade);
            }
        } catch (IOException ex) {
            Logger.getLogger(GeraFront.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void geraFront(String nce) throws IOException {
        nomePacoteBase = nce.substring(0, nce.lastIndexOf(".domain"));
        nomeEntidade = nce.substring(nce.lastIndexOf('.') + 1);
        getLog().info("nomeCompletoEntidade:" + nce);
        String pastaModulo = Util.windowsSafe(project.getFile().getParent() + "/front/project/src/app/" + nomeEntidade.toLowerCase());
        if (new File(pastaModulo).exists()) {
            System.out.println("-----> JA existe " + pastaModulo);
            return;
        }
        criaPastasModulo(pastaModulo);
        geraModuleTS(pastaModulo, nce, nomePacoteBase, nomeEntidade);
        geraServiceTS(pastaModulo, nce, nomePacoteBase, nomeEntidade);
        geraRoutingModuleTS(pastaModulo, nce, nomePacoteBase, nomeEntidade);
        geraCrudTS(pastaModulo, nce, nomePacoteBase, nomeEntidade);
        geraCrudCSS(pastaModulo, nce, nomePacoteBase, nomeEntidade);
        geraCrudHTML(pastaModulo, nce, nomePacoteBase, nomeEntidade);
        geraDetalhesTS(pastaModulo, nce, nomePacoteBase, nomeEntidade);
        geraDetalhesCSS(pastaModulo, nce, nomePacoteBase, nomeEntidade);
        geraDetalhesHTML(pastaModulo, nce, nomePacoteBase, nomeEntidade);
        geraListaTS(pastaModulo, nce, nomePacoteBase, nomeEntidade);
        geraListaCSS(pastaModulo, nce, nomePacoteBase, nomeEntidade);
        geraListaHTML(pastaModulo, nce, nomePacoteBase, nomeEntidade);
        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent() + "/front/project/src/app/app.component.html"), "<!-- MENU -->", ""
                + "                    <li class=\"nav-item\">\n"
                + "                        <a class=\"nav-link\" [routerLink]=\"['./" + nomeEntidade.toLowerCase() + "']\">\n"
                + "                            <span data-feather=\"file\"></span>\n"
                + "                            " + nomeEntidade + "\n"
                + "                        </a>\n"
                + "                    </li>"
                + "");
        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent() + "/front/project/src/app/app.module.ts"), "app.module.ts1", ""
                + "import { "+nomeEntidade+"Module} from './"+nomeEntidade.toLowerCase()+"/"+nomeEntidade.toLowerCase()+".module';\n"
                + "import { "+nomeEntidade+"Service} from './"+nomeEntidade.toLowerCase()+"/"+nomeEntidade.toLowerCase()+".service';"
                + "");
        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent() + "/front/project/src/app/app.module.ts"), "app.module.ts2", ""
                + "    "+nomeEntidade+"Module,"
                + "");
        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent() + "/front/project/src/app/app.module.ts"), "app.module.ts3", ""
                + "    "+nomeEntidade+"Service,"
                + "");

        
    }

    private void criaPastasModulo(String pastaModulo) throws IOException {
        new File(pastaModulo).mkdir();
        new File(pastaModulo + "/crud").mkdir();
        new File(pastaModulo + "/detalhes").mkdir();
        new File(pastaModulo + "/lista").mkdir();
    }

    private void geraModuleTS(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/" + minusculas + ".module.ts";
        fw = new FileWriter(arquivo, false);
        fw.write(""
                + "import { NgModule } from '@angular/core';\n"
                + "import { FormsModule }   from '@angular/forms';\n"
                + "import { CommonModule } from '@angular/common';\n"
                + "import { VicComponentsModule } from '../vic-components/vic-components.module';\n"
                + "import { " + ne + "RoutingModule } from './" + minusculas + "-routing.module';\n"
                + "import { CrudComponent } from './crud/crud.component';\n"
                + "import { ListaComponent } from './lista/lista.component';\n"
                + "import { DetalhesComponent } from './detalhes/detalhes.component';\n"
                + "\n"
                + "\n"
                + "@NgModule({\n"
                + "  imports: [\n"
                + "    CommonModule,\n"
                + "    FormsModule,\n"
                + "    " + ne + "RoutingModule,\n"
                + "    VicComponentsModule\n"
                + "  ],\n"
                + "  declarations: [CrudComponent, ListaComponent, DetalhesComponent]\n"
                + "})\n"
                + "export class " + ne + "Module { }\n"
                + ""
        );
        fw.close();
    }

    private void geraServiceTS(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/" + minusculas + ".service.ts";
        fw = new FileWriter(arquivo, false);
        fw.write(""
                + "import { Injectable } from '@angular/core';\n"
                + "import { Http, Headers, Response } from '@angular/http';\n"
                + "import { SuperService} from '../comum/super-service';\n"
                + "\n"
                + "@Injectable()\n"
                + "export class " + ne + "Service extends SuperService{\n"
                + "\n"
                + "  constructor(http:Http) {\n"
                + "    super('" + ne.toLowerCase() + "',http);\n"
                + "  }\n"
                + "\n"
                + "}"
        );
        fw.close();
    }

    private void geraRoutingModuleTS(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/" + minusculas + "-routing.module.ts";
        fw = new FileWriter(arquivo, false);
        fw.write(""
                + "import { NgModule } from '@angular/core';\n"
                + "import { Routes, RouterModule } from '@angular/router';\n"
                + "import { CrudComponent } from './crud/crud.component';\n"
                + "import { ListaComponent } from './lista/lista.component';\n"
                + "import { DetalhesComponent } from './detalhes/detalhes.component';\n"
                + "\n"
                + "const routes: Routes = [\n"
                + "  {\n"
                + "    path: '" + minusculas + "', component: CrudComponent,\n"
                + "    children: [\n"
                + "      { path: '', component: ListaComponent },\n"
                + "      { path: 'detalhes/:id', component: DetalhesComponent }\n"
                + "    ]\n"
                + "  }\n"
                + "];\n"
                + "\n"
                + "@NgModule({\n"
                + "  imports: [RouterModule.forChild(routes)],\n"
                + "  exports: [RouterModule]\n"
                + "})\n"
                + "export class " + ne + "RoutingModule { }"
                + ""
        );
        fw.close();
    }

    private void geraCrudTS(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/crud/crud.component.ts";
        fw = new FileWriter(arquivo, false);
        fw.write(""
                + "import { Component, OnInit } from '@angular/core';\n"
                + " \n"
                + "@Component({\n"
                + "  selector: 'app-crud',\n"
                + "  templateUrl: './crud.component.html',\n"
                + "  styleUrls: ['./crud.component.css']\n"
                + "})\n"
                + "export class CrudComponent  {\n"
                + "\n"
                + "\n"
                + "}"
        );
        fw.close();
    }

    private void geraCrudCSS(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/crud/crud.component.css";
        fw = new FileWriter(arquivo, false);
        fw.write(""
                + ""
        );
        fw.close();
    }

    private void geraCrudHTML(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/crud/crud.component.html";
        fw = new FileWriter(arquivo, false);
        fw.write(""
                + "<div class=\"d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pb-2 mb-3 border-bottom\">\n"
                + "  <h1 class=\"h2\">" + ne + "</h1>\n"
                + "</div>\n"
                + "<router-outlet></router-outlet>"
        );
        fw.close();
    }

    private void geraDetalhesTS(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/detalhes/detalhes.component.ts";
        fw = new FileWriter(arquivo, false);
        fw.write(""
                + "import { Component, OnInit } from '@angular/core';\n"
                + "import { Router, ActivatedRoute, Params } from '@angular/router';\n"
                + "import { Location } from '@angular/common';\n"
                + "import { " + ne + "Service } from '../"+ne.toLowerCase()+".service';\n"
                + "import { VicReturn } from '../../comum/vic-return';\n"
                + "import { SuperDetalhesComponent } from '../../comum/super-detalhes';\n"
                + "\n"
                + "\n"
                + "@Component({\n"
                + "  selector: 'app-detalhes',\n"
                + "  templateUrl: './detalhes.component.html',\n"
                + "  styleUrls: ['./detalhes.component.css']\n"
                + "})\n"
                + "export class DetalhesComponent extends SuperDetalhesComponent {\n"
                + "\n"
                + "  constructor(protected service: " + ne + "Service, protected router: Router, protected route: ActivatedRoute) {\n"
                + "    super(service,router,route);\n"
                + "  }\n"
                + "\n"
                + "\n"
                + "}\n"
                + ""
        );
        fw.close();
    }

    private void geraDetalhesCSS(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/detalhes/detalhes.component.css";
        fw = new FileWriter(arquivo, false);
        fw.write(""
                + ""
        );
        fw.close();
    }

    private void geraDetalhesHTML(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/detalhes/detalhes.component.html";
        fw = new FileWriter(arquivo, false);
        fw.write(""
                + "<div *ngIf=\"!selecionado\">\n"
                + "  Carregando....\n"
                + "</div>\n"
                + "<div *ngIf=\"selecionado\">\n"
                + "  <h2>{{ selecionado.nome | uppercase }}</h2>\n"
                + "  <div>\n"
                + "    <label>Nome:\n"
                + "      <input type=\"text\" id=\"inNome\" name=\"nome\" placeholder=\"Nome\" [(ngModel)]=\"selecionado.nome\" />\n"
                + "    </label>\n"
                + "  </div>\n"
                + "  <div>\n"
                + "    <button type=\"button\" class=\"btn btn-success\" (click)=\"salvar()\">Salvar</button>\n"
                + "    <button type=\"button\" class=\"btn btn-warning\" (click)=\"cancelar()\">Cancelar</button>\n"
                + "    <button type=\"button\" class=\"btn btn-danger\" (click)=\"excluir()\">Excluir</button>\n"
                + "  </div>\n"
                + "\n"
                + "  <div class=\"alert alert-danger\" role=\"alert\" *ngIf=\"erro\">\n"
                + "    {{erro|json}}\n"
                + "  </div>\n"
                + "  <div>\n"
                + "      <vic-system-fields [entidade]=\"selecionado\"></vic-system-fields>\n"
                + "    </div>\n"
                + "  \n"
                + "\n"
                + "</div>"
        );
        fw.close();
    }

    private void geraListaTS(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/lista/lista.component.ts";
        fw = new FileWriter(arquivo, false);
        fw.write(""
                + "import { Component, OnInit } from '@angular/core';\n"
                + "import { Router, ActivatedRoute, Params } from '@angular/router';\n"
                + "import { " + ne + "Service } from '../"+minusculas+".service';\n"
                + "import { BaseEntity } from \"../../comum/base-entity\";\n"
                + "import { VicReturn } from '../../comum/vic-return';\n"
                + "import { SuperListaComponent } from '../../comum/super-lista';\n"
                + "\n"
                + "\n"
                + "@Component({\n"
                + "  selector: 'app-lista',\n"
                + "  templateUrl: './lista.component.html',\n"
                + "  styleUrls: ['./lista.component.css']\n"
                + "})\n"
                + "export class ListaComponent extends SuperListaComponent {\n"
                + "\n"
                + "  constructor(protected service: " + ne + "Service, protected router: Router, protected route: ActivatedRoute) {\n"
                + "    super(service,router,route);\n"
                + "    this.colunas = [{ field: \"nome\", label: \"Nome\" }];\n"
                + "    this.consulta = { hql: \"obj.nome like '_pesquisa%' \", orderBy: \"nome\" };\n"
                + "\n"
                + "  }\n"
                + "\n"
                + "}"
        );
        fw.close();
    }

    private void geraListaCSS(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/lista/lista.component.css";
        fw = new FileWriter(arquivo, false);
        fw.write(""
                + ""
        );
        fw.close();
    }

    private void geraListaHTML(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/lista/lista.component.html";
        fw = new FileWriter(arquivo, false);
        fw.write(""
                + "<h2>Lista {{resposta.quantity}} </h2>\n"
                + "<div>\n"
                + "    <label>Pesquisa:\n"
                + "        <input type=\"text\" id=\"inPesquisa\" name=\"pesquisa\" placeholder=\"Pesquisa\" [(ngModel)]=\"pesquisa\" />\n"
                + "    </label>\n"
                + "    <button type=\"button\" class=\"btn btn-info\" (click)=\"pesquisar()\">Pesquisar</button>\n"
                + "    <button type=\"button\" class=\"btn btn-info\" (click)=\"novo()\">Novo</button>\n"
                + "</div>\n"
                + "<vic-tabela [(dados)]=\"resposta\" [colunas]=\"colunas\" (acao)=\"goDetalhes($event)\"></vic-tabela>"
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
