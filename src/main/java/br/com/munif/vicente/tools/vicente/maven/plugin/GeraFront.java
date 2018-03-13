package br.com.munif.vicente.tools.vicente.maven.plugin;

import br.com.munif.framework.vicente.core.Utils;
import static br.com.munif.vicente.tools.vicente.maven.plugin.Util.abreArquivo;
import static br.com.munif.vicente.tools.vicente.maven.plugin.Util.primeiraMaiuscula;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

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
                    //System.out.println("mvn br.com.munif.vicente:g:0.0.1-SNAPSHOT:front -Dentidade=" + c.getCanonicalName());
                }
                
            } else {
                geraFront(nomeCompletoEntidade);
            }
        } catch (IOException ex) {
            Logger.getLogger(GeraFront.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GeraFront.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void geraFront(String nce) throws IOException, ClassNotFoundException {
        nomePacoteBase = nce.substring(0, nce.lastIndexOf(".domain"));
        nomeEntidade = nce.substring(nce.lastIndexOf('.') + 1);
        getLog().info("nomeCompletoEntidade:" + nce);
        String pastaModulo = Util.windowsSafe(project.getFile().getParent() + "/front/project/src/app/" + nomeEntidade.toLowerCase());
        boolean jaExiste = new File(pastaModulo).exists();
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
        if (!jaExiste) {
            Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent() + "/front/project/src/app/app.component.ts"), "/* MENU  */", ""
                    + "{ link: './" + nomeEntidade.toLowerCase() + "', iconeTipo: 'fas', icone: 'fa-home', label: '" + nomeEntidade + "', active: false },\n"
                    + "");
            Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent() + "/front/project/src/app/app.module.ts"), "app.module.ts1", ""
                    + "import { " + nomeEntidade + "Module} from './" + nomeEntidade.toLowerCase() + "/" + nomeEntidade.toLowerCase() + ".module';\n"
                    + "import { " + nomeEntidade + "Service} from './" + nomeEntidade.toLowerCase() + "/" + nomeEntidade.toLowerCase() + ".service';"
                    + "");
            Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent() + "/front/project/src/app/app.module.ts"), "app.module.ts2", ""
                    + "    " + nomeEntidade + "Module,"
                    + "");
            Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent() + "/front/project/src/app/app.module.ts"), "app.module.ts3", ""
                    + "    " + nomeEntidade + "Service,"
                    + "");
        }
        
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
        fw = abreArquivo(arquivo, false);
        fw.write(""
                + "import { NgModule } from '@angular/core';\n"
                + "import { OwlDateTimeModule, OwlNativeDateTimeModule } from 'ng-pick-datetime';\n"
                + "import { FormsModule }   from '@angular/forms';\n"
                + "import { CommonModule } from '@angular/common';\n"
                + "import { VicComponentsModule } from '../vic-components/vic-components.module';\n"
                + "import { " + ne + "RoutingModule } from './" + minusculas + "-routing.module';\n"
                + "import { CrudComponent } from './crud/crud.component';\n"
                + "import { ListaComponent } from './lista/lista.component';\n"
                + "import { DetalhesComponent } from './detalhes/detalhes.component';\n"
                + "\n"
                + "/*IMPORTS*/\n"
                + "\n"
                + "@NgModule({\n"
                + "  imports: [\n"
                + "    CommonModule,\n"
                + "    FormsModule, OwlDateTimeModule, OwlNativeDateTimeModule,\n"
                + "    " + ne + "RoutingModule,\n"
                + "    VicComponentsModule\n"
                + "  ],\n"
                + "  declarations: [\n"
                + "/*DECLARATIONS*/\n"
                + "      CrudComponent, \n"
                + "      ListaComponent, \n"
                + "      DetalhesComponent\n,"
                + "]\n"
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
        fw = abreArquivo(arquivo, false);
        fw.write(""
                + "import { Injectable } from '@angular/core';\n"
                + "import { Http, Headers, Response } from '@angular/http';\n"
                + "import { SuperService} from '../vic-components/comum/super-service';\n"
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
        fw = abreArquivo(arquivo, false);
        fw.write(""
                + "import { NgModule } from '@angular/core';\n"
                + "import { Routes, RouterModule } from '@angular/router';\n"
                + "import { CrudComponent } from './crud/crud.component';\n"
                + "import { ListaComponent } from './lista/lista.component';\n"
                + "import { DetalhesComponent } from './detalhes/detalhes.component';\n\n\n"
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
        fw = abreArquivo(arquivo, false);
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
        fw = abreArquivo(arquivo, false);
        fw.write(""
                + ""
        );
        fw.close();
    }
    
    private void geraCrudHTML(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/crud/crud.component.html";
        fw = abreArquivo(arquivo, false);
        fw.write(""
                + "<div class=\"d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pb-2 mb-3 border-bottom\">\n"
                + "  <h1 class=\"h2\">" + ne + "</h1>\n"
                + "</div>\n"
                + "<router-outlet></router-outlet>"
        );
        fw.close();
    }
    
    private void geraDetalhesTS(String pastaModulo, String nce, String npb, String ne) throws IOException, ClassNotFoundException {
        
        Class classe = classLoader.loadClass(nce);
        
        List<Field> atributos = Util.getTodosAtributosNaoBase(classe);
        Field primeiroAtributo = atributos.get(0);
        
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/detalhes/detalhes.component.ts";
        fw = abreArquivo(arquivo, false);
        fw.write(""
                + "import { Component, OnInit } from '@angular/core';\n"
                + "import { Router, ActivatedRoute, Params } from '@angular/router';\n"
                + "import { Location } from '@angular/common';\n"
                + "import { " + ne + "Service } from '../" + ne.toLowerCase() + ".service';\n");
        Set<Class> jaImportou = new HashSet<>();
        jaImportou.add(classe);
        for (Field f : atributos) {
            if (f.isAnnotationPresent(ManyToOne.class)) {
                if (!jaImportou.contains(f.getType())) {
                    fw.write("import { " + f.getType().getSimpleName() + "Service } from '../../" + f.getType().getSimpleName().toLowerCase() + "/" + f.getType().getSimpleName().toLowerCase() + ".service';\n");
                    jaImportou.add(f.getType());
                }
                
            }
        }
        for (Field f : atributos) {
            if (f.isAnnotationPresent(ManyToMany.class)) {
                Class tipo = Util.getTipoGenerico(f);
                if (!jaImportou.contains(tipo)) {
                    fw.write("import { " + tipo.getSimpleName() + "Service } from '../../" + tipo.getSimpleName().toLowerCase() + "/" + tipo.getSimpleName().toLowerCase() + ".service';\n");
                    jaImportou.add(tipo);
                }
            }
        }
        
        fw.write(""
                + "import { VicReturn } from '../../vic-components/comum/vic-return';\n"
                + "import { SuperDetalhesComponent } from '../../vic-components/comum/super-detalhes';\n"
                + "\n"
                + "\n"
                + "@Component({\n"
                + "  selector: 'app-detalhes',\n"
                + "  templateUrl: './detalhes.component.html',\n"
                + "  styleUrls: ['./detalhes.component.css']\n"
                + "})\n"
                + "export class DetalhesComponent extends SuperDetalhesComponent {\n"
                + "\n"
                + "  constructor(protected service: " + ne + "Service, protected router: Router, protected route: ActivatedRoute");
        for (Class tipo : jaImportou) {
            fw.write(",protected " + Util.primeiraMinuscula(tipo.getSimpleName()) + "Service:" + tipo.getSimpleName() + "Service");
        }
        fw.write(""
                + ") {\n"
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
        fw = abreArquivo(arquivo, false);
        fw.write(""
                + ".footer{\n"
                + "    float: right !important;\n"
                + "    border: 1px 0px 0px 0px solid black;\n"
                + "    text-align: right;\n"
                + "}\n"
                + "\n"
                + ".margin-bottom{\n"
                + "    margin-bottom: 20px;\n"
                + "}"
        );
        fw.close();
    }
    
    private void geraDetalhesHTML(String pastaModulo, String nce, String npb, String ne) throws IOException, ClassNotFoundException {
        Class classe = classLoader.loadClass(nce);
        List<Field> atributos = Util.getTodosAtributosNaoBase(classe);
        Field primeiroAtributo = atributos.get(0);
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/detalhes/detalhes.component.html";
        fw = abreArquivo(arquivo, false);
        fw.write(""
                + "<div class=\"container\">\n"
                + "\n"
                + "  <div *ngIf=\"!selecionado\">\n"
                + "    Carregando....\n"
                + "  </div>\n"
                + "  <div *ngIf=\"selecionado\">\n"
                + "\n"
                + "    <h2>{{ selecionado." + primeiroAtributo.getName() + "| uppercase }}</h2>\n"
                + "\n"
                + "    <div class=\"row\">\n\n");
        
        for (Field atributo : atributos) {
            geraCampoAtributo(fw, atributo, pastaModulo);
        }
        
        fw.write(""
                + "    </div>\n"
                + "    <div class=\"row\">\n"
                + "      <div class=\"col-sm-6 text-left\">\n"
                + "        <button type=\"button\" class=\"btn btn-danger\" (click)=\"excluir()\" [disabled]=\"isNew(selecionado)||!canDelete(selecionado)\">\n"
                + "          <i class=\"far fa-trash-alt\"></i> Excluir\n"
                + "        </button>\n"
                + "      </div>\n"
                + "      <div class=\"col-sm-6 text-right\">\n"
                + "        <button type=\"button\" class=\"btn btn-warning\" (click)=\"cancelar()\">\n"
                + "          <i class=\"far fa-times-circle\"></i> Cancelar\n"
                + "        </button>\n"
                + "        <button type=\"button\" class=\"btn btn-success\" (click)=\"salvar()\"  [disabled]=\"notNew(selecionado)&&!canUpdate(selecionado)\" >\n"
                + "          <i class=\"far fa-save\"></i> Salvar\n"
                + "        </button>\n"
                + "      </div>\n"
                + "    </div>\n"
                + "    <div class=\"alert alert-danger\" role=\"alert\" *ngIf=\"erro\">\n"
                + "      {{erro|json}}\n"
                + "    </div>\n"
                + "");

//        fw.write(""
//                + "</div>\n"
//                + ""
//                + "    <div class=\"row\">\n"
//                + "\n"
//                + "      <div class=\"col-sm-12 text-right\">\n"
//                + "        <button type=\"button\" class=\"btn btn-warning\" (click)=\"cancelar()\">\n"
//                + "          <i class=\"far fa-times-circle\"></i> Cancelar</button>\n"
//                + "        <button type=\"button\" class=\"btn btn-success\" (click)=\"salvar()\">\n"
//                + "          <i class=\"far fa-save\"></i> Salvar</button>\n"
//                + "        <button type=\"button\" class=\"btn btn-danger\" (click)=\"excluir()\">\n"
//                + "          <i class=\"far fa-delete\"></i>Excluir</button>\n"
//                + "      </div>\n"
//                + "    </div>\n"
//                + ""
//                + ""
//                + "  <div class=\"alert alert-danger\" role=\"alert\" *ngIf=\"erro\">\n"
//                + "    {{erro|json}}\n"
//                + "  </div>\n"
//                + "</div>\n"
//        );
        fw.close();
    }
    
    public void geraCampoAtributo(FileWriter fw, Field atributo, String pastaModulo) throws IOException {
        Class tipo = atributo.getType();
        if (atributo.isAnnotationPresent(OneToOne.class)) {
            fw.write("<!--OneToOne-->\n"
                    + "\n");
            
        } else if (atributo.isAnnotationPresent(ManyToMany.class)) {
            tipo = Util.getTipoGenerico(atributo);
            fw.write(""
                    + "  <div class=\"col-sm-12 margin-bottom\" *ngIf=\"(isNew(selecionado)||canUpdate(selecionado))\" >\n"
                    + "    <label>" + atributo.getName().toUpperCase() + ":</label>\n"
                    + "      <vic-many-to-many [(valor)]=\"selecionado." + atributo.getName() + "\" [service]=\"" + Util.primeiraMinuscula(tipo.getSimpleName()) + "Service\" atributoLabel=\"" + Util.primeiroAtributo(tipo).getName() + "\" ></vic-many-to-many>\n"
                    + "  </div>\n"
                    + "\n");
            
            fw.write(""
                    + "  <div class=\"col-sm-12 margin-bottom\" *ngIf=\"!(isNew(selecionado)||canUpdate(selecionado))\" >\n"
                    + "    <label>" + atributo.getName().toUpperCase() + ":</label>\n"
                    + "      <input type=\"text\" id=\"id" + atributo.getName() + "\" name=\"" + atributo.getName() + "\" placeholder=\"" + atributo.getName().toUpperCase() + "\" [(ngModel)]=\"selecionado." + atributo.getName() + "\" class=\"form-control\" [disabled]=\"notNew(selecionado)&&!canUpdate(selecionado)\" />\n"
                    + "  </div>\n"
                    + "\n");
            
        } else if (atributo.isAnnotationPresent(OneToMany.class)) {
            tipo = Util.getTipoGenerico(atributo);
            String nomeArquivo = atributo.getDeclaringClass().getSimpleName().toLowerCase() + "-" + atributo.getName().toLowerCase();
            fw.write(""
                    + "  <div class=\"col-sm-12 margin-bottom\" *ngIf=\"selecionado.version!==null   \">\n"
                    + "    <app-" + nomeArquivo + " [itens]=\"(selecionado." + atributo.getName() + "===null?[]:selecionado." + atributo.getName() + ")\" [proprietario]=\"selecionado\"></app-" + nomeArquivo + ">\n"
                    + "  </div>\n\n"
                    + "");
            fw.write(""
                    + "  <div class=\"col-sm-12 margin-bottom\" *ngIf=\"selecionado.version==null \">\n"
                    + "        <h2>" + atributo.getName().toUpperCase() + "</h2>\n"
                    + "        <button type=\"button\" class=\"btn btn-primary btn-lg btn-block\" (click)=\"salvarImediatamente()\" >\n"
                    + "           Para inserir " + tipo.getSimpleName().toUpperCase() + " é necessário salvar este " + atributo.getDeclaringClass().getSimpleName().toUpperCase() + " primeiro. Para salvar imediatamente click aqui.\n"
                    + "        </button>\n"
                    + "  </div>\n\n"
                    + "");
            
            geraComponenteOneToMany(atributo, pastaModulo);
            geraHTMLOneToMany(atributo, pastaModulo);
            
        } else if (atributo.isAnnotationPresent(ManyToOne.class)) {
            fw.write(""
                    + "  <div class=\"col-sm-12 margin-bottom\" *ngIf=\"(isNew(selecionado)||canUpdate(selecionado))\" >\n"
                    + "    <label>" + atributo.getName().toUpperCase() + ":</label>\n"
                    + "      <vic-many-to-one [(valor)]=\"selecionado." + atributo.getName() + "\" [service]=\"" + Util.primeiraMinuscula(tipo.getSimpleName()) + "Service\" atributoLabel=\"" + Util.primeiroAtributo(tipo).getName() + "\" ></vic-many-to-one>\n"
                    + "  </div>\n"
                    + "\n");
            fw.write(""
                    + "  <div class=\"col-sm-12 margin-bottom\" *ngIf=\"!(isNew(selecionado)||canUpdate(selecionado))\" >\n"
                    + "    <label>" + atributo.getName().toUpperCase() + ":</label>\n"
                    + "      <input type=\"text\" id=\"id" + atributo.getName() + "\" name=\"" + atributo.getName() + "\" placeholder=\"" + atributo.getName().toUpperCase() + "\" [(ngModel)]=\"selecionado." + atributo.getName() + "." + Util.primeiroAtributo(tipo).getName() + "\" class=\"form-control\" [disabled]=\"notNew(selecionado)&&!canUpdate(selecionado)\" />\n"
                    + "  </div>\n"
                    + "\n");
            
        } else if (atributo.getType().equals(ZonedDateTime.class)) {
            fw.write(""
                    + "  <div class=\"col-sm-12 margin-bottom\">\n"
                    + "    <label>" + atributo.getName().toUpperCase() + ":</label>\n"
                    + "      <input [owlDateTime]=\"dt" + atributo.getName().toUpperCase() + "\" [owlDateTimeTrigger]=\"dt" + atributo.getName().toUpperCase() + "\" type=\"text\" id=\"id" + atributo.getName() + "\" name=\"" + atributo.getName() + "\" placeholder=\"" + atributo.getName() + "\" [(ngModel)]=\"selecionado." + atributo.getName() + "\" class=\"form-control\" />\n"
                    + "      <owl-date-time #dt" + atributo.getName().toUpperCase() + "></owl-date-time>\n"
                    + "  </div>\n"
                    + "\n");
        } else {
            fw.write("  <div class=\"col-sm-12 margin-bottom\">\n"
                    + "    <label>" + atributo.getName().toUpperCase() + ":</label>\n"
                    + "      <input type=\"text\" id=\"id" + atributo.getName() + "\" name=\"" + atributo.getName() + "\" placeholder=\"" + atributo.getName().toUpperCase() + "\" [(ngModel)]=\"selecionado." + atributo.getName() + "\" class=\"form-control\" [disabled]=\"notNew(selecionado)&&!canUpdate(selecionado)\" />\n"
                    + "  </div>\n"
                    + "\n");
        }
    }
    
    private void geraListaTS(String pastaModulo, String nce, String npb, String ne) throws IOException, ClassNotFoundException {
        Class classe = classLoader.loadClass(nce);
        List<Field> atributos = Util.getTodosAtributosNaoBase(classe);
        List<String> chapa = chapa(classe);
        Field primeiroAtributo = atributos.get(0);
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/lista/lista.component.ts";
        fw = abreArquivo(arquivo, false);
        fw.write(""
                + "import { Component, OnInit } from '@angular/core';\n"
                + "import { Router, ActivatedRoute, Params } from '@angular/router';\n"
                + "import { " + ne + "Service } from '../" + minusculas + ".service';\n"
                + "import { BaseEntity } from \"../../vic-components/comum/base-entity\";\n"
                + "import { VicReturn } from '../../vic-components/comum/vic-return';\n"
                + "import { SuperListaComponent } from '../../vic-components/comum/super-lista';\n"
                + "\n"
                + "\n"
                + "@Component({\n"
                + "  selector: 'app-lista',\n"
                + "  templateUrl: './lista.component.html',\n"
                + "  styleUrls: ['./lista.component.css']\n"
                + "})\n"
                + "export class ListaComponent extends SuperListaComponent {\n"
                + "\n"
                + "  colunas = [\n");
        for (String s : chapa) {
            fw.write("    " + s + "\n");
        }
        
        fw.write(""
                + "  ];\n"
                + "\n"
                + "  constructor(protected service: " + ne + "Service, protected router: Router, protected route: ActivatedRoute) {\n"
                + "    super(service,router,route);\n"
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
        fw = abreArquivo(arquivo, false);
        fw.write(""
                + ".padding-bottom-20{\n"
                + "    padding-bottom: 20px;\n"
                + "}"
        );
        fw.close();
    }
    
    private void geraListaHTML(String pastaModulo, String nce, String npb, String ne) throws IOException {
        FileWriter fw = null;
        String minusculas = ne.toLowerCase();
        String arquivo = pastaModulo + "/lista/lista.component.html";
        fw = abreArquivo(arquivo, false);
        fw.write(""
                + "<h2>Lista {{resposta.quantity}} </h2>\n"
                + "<div class=\"row padding-bottom-20\">\n"
                + "    <div class=\"col-sm-6\">\n"
                + "        <input type=\"text\" id=\"inPesquisa\" name=\"pesquisa\" placeholder=\"Pesquisa\" class=\"form-control\" [(ngModel)]=\"pesquisa\" (keypress)=\"verificaEnter($event)\"/>\n"
                + "    </div>\n"
                + "    <div class=\"col-sm-3\" >\n"
                + "        <button type=\"button\" class=\"btn btn-info\" (click)=\"pesquisar()\">\n"
                + "            <i class=\"fas fa-search\"></i> Pesquisar </button>\n"
                + "    </div>\n"
                + "\n"
                + "    <div class=\"col-sm-3 text-right\">\n"
                + "        <button type=\"button\" class=\"btn btn-success\" (click)=\"novo()\">\n"
                + "            <i class=\"far fa-file\"></i> Novo</button>\n"
                + "    </div>\n"
                + "</div>\n"
                + "<vic-tabela [(dados)]=\"resposta\" [colunas]=\"colunas\" (acao)=\"goDetalhes($event)\" (carregarMais)=\"carregarMais()\"></vic-tabela>"
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
    
    private List<String> chapa(Class c) {
        i = 0;
        return chapa(c, "");
    }
    
    private List<String> chapa(Class c, String prefixo) {
        List<String> toReturn = new ArrayList<>();
        List<Field> atributos = Util.getTodosAtributosNaoBase(c);
        atributos.forEach((a) -> {
            String nome = a.getName();
            String nomeTipo = a.getType().getCanonicalName();
            if (nomeTipo.startsWith("java.lang") || nomeTipo.startsWith("java.time")) {
                toReturn.add(resolve(prefixo, a));
            }
            if (a.isAnnotationPresent(ManyToOne.class) || a.isAnnotationPresent(OneToOne.class)) {
                if (prefixo.endsWith(nome + ".")) {
                    System.out.println("----> Possivel recursao na classe  " + c.getSimpleName() + " atributo " + nome + " prefixo " + prefixo);
                } else {
                    toReturn.addAll(chapa(a.getType(), prefixo + nome + "."));
                }
            }
            
        });
        
        return toReturn;
    }
    
    private int i;
    
    private String resolve(String prefixo, Field atributo) {
        List<String> ignorados = Arrays.asList(new String[]{"descricao", "nome", "valor"});
        String name = atributo.getName();
        String label = primeiraMaiuscula(splitCamelCase(name));
        String nomeCompleto = prefixo + name;
        String pedacos[] = nomeCompleto.split("\\.");
        int length = pedacos.length;
        if (length > 1 && ignorados.contains(pedacos[length - 1])) {
            label = primeiraMaiuscula(splitCamelCase(pedacos[length - 2]));
        }
        List<String> collect = Arrays.asList(pedacos).stream().map(s -> ('"' + s + '"')).collect(Collectors.toList());
        
        return "{ active: " + (i++ < 4) + ", comparisonOperator: \"" + comparador(atributo.getType()) + "\", field: \"" + nomeCompleto + "\", label: \"" + label + "\",pedacos: " + collect + "},";
    }
    
    private String splitCamelCase(String s) { //by polygenelubricants https://stackoverflow.com/users/276101/polygenelubricants
        return s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }
    
    private String comparador(Class<?> type) {
        if (type.equals(String.class)) {
            return "STARTS_WITH";
        }
        return "EQUAL";
    }
    
    private void geraComponenteOneToMany(Field atributo, String pastaModulo) throws IOException {
        Class classe = Util.getTipoGenerico(atributo);
        List<Field> atributos = Util.getTodosAtributosNaoBase(classe);
        String nomeArquivo = atributo.getDeclaringClass().getSimpleName().toLowerCase() + "-" + atributo.getName().toLowerCase();
        String nomeClasseCoponente = atributo.getDeclaringClass().getSimpleName() + Util.primeiraMaiuscula(atributo.getName());
        String arquivo = pastaModulo + "/detalhes/" + nomeArquivo + ".component.ts";
        String atributoMappedBy = "NAO_TEM";
        OneToMany oneToMany = atributo.getAnnotation(OneToMany.class);
        if (oneToMany != null && oneToMany.mappedBy() != null && (!oneToMany.mappedBy().trim().isEmpty())) {
            atributoMappedBy = oneToMany.mappedBy();
        }
        FileWriter fw = abreArquivo(arquivo, false);
        
        fw.write("//Munif\n"
                + "import { Component, OnInit, Input } from '@angular/core';\n"
                + "import { VicReturn } from '../../vic-components/comum/vic-return';\n"
                + "import { BaseEntity } from '../../vic-components/comum/base-entity';\n"
                + "import { " + classe.getSimpleName() + "Service } from '../../" + classe.getSimpleName().toLowerCase() + "/" + classe.getSimpleName().toLowerCase() + ".service';\n"
        );
        Set<Class> jaImportou = new HashSet<>();
        jaImportou.add(classe);
        for (Field f : atributos) {
            if (f.getName().equals(atributoMappedBy)) {
                continue;
            }
            if (f.isAnnotationPresent(ManyToOne.class)) {
                if (!jaImportou.contains(f.getType())) {
                    fw.write("import { " + f.getType().getSimpleName() + "Service } from '../../" + f.getType().getSimpleName().toLowerCase() + "/" + f.getType().getSimpleName().toLowerCase() + ".service';\n");
                    jaImportou.add(f.getType());
                }
            }
        }
        for (Field f : atributos) {
            if (f.getName().equals(atributoMappedBy)) {
                continue;
            }
            if (f.isAnnotationPresent(ManyToMany.class)) {
                Class tipo = Util.getTipoGenerico(f);
                if (!jaImportou.contains(tipo)) {
                    fw.write("import { " + tipo.getSimpleName() + "Service } from '../../" + tipo.getSimpleName().toLowerCase() + "/" + tipo.getSimpleName().toLowerCase() + ".service';\n");
                    jaImportou.add(tipo);
                }
            }
        }
        
        jaImportou.forEach(a -> System.out.print(a.getSimpleName() + " "));
        System.out.println("");
        fw.write(""
                + "\n"
                + "\n"
                + "@Component({\n"
                + "    selector: 'app-" + nomeArquivo + "',\n"
                + "    templateUrl: './" + nomeArquivo + ".component.html',\n"
                + "    styleUrls: ['./detalhes.component.css']\n"
                + "})\n"
                + "export class " + nomeClasseCoponente + "Component implements OnInit {\n"
                + "\n"
                + "    editando: boolean;\n"
                + "    selecionado: BaseEntity;\n"
                + "    erro;\n"
                + "\n"
                + "    colunas = [\n");
        for (String s : chapa(classe)) {
            if (!s.contains("field: \"" + atributoMappedBy + ".")) {
                fw.write("    " + s + "\n");
            }
        }
        fw.write(""
                + "    ];\n"
                + "\n"
                + "\n"
                + "\n"
                + "    @Input() itens: BaseEntity[];\n"
                + "    @Input() proprietario: BaseEntity;\n"
                + "    lista: VicReturn;\n"
                + "\n"
                + "    constructor(");
        String separador = "";
        for (Class tipo : jaImportou) {
            fw.write(separador + "protected " + Util.primeiraMinuscula(tipo.getSimpleName()) + "Service:" + tipo.getSimpleName() + "Service");
            separador = ",";
        }
        String mClasse = Util.primeiraMinuscula(classe.getSimpleName());
        fw.write(""
                + ") {\n"
                + "\n"
                + "    }\n"
                + "\n"
                + "    ngOnInit() {\n"
                + "        this.atualiza();\n"
                + "    }\n"
                + "\n"
                + "    edita(evento) {\n"
                + "        this." + mClasse + "Service.getOne(evento).then(obj => {\n"
                + "            this.editando = true;\n"
                + "            this.selecionado = obj;\n"
                + "            this.selecionado['" + atributoMappedBy + "'] = { id: this.proprietario.id, version: this.proprietario.version };\n"
                + "        })\n"
                + "    }\n"
                + "\n"
                + "    criaVicReturnItens() {\n"
                + "        if (!this.lista) {\n"
                + "            this.lista = new VicReturn(this.itens);\n"
                + "        }\n"
                + "        return this.lista;\n"
                + "\n"
                + "    }\n"
                + "    salvar() {\n"
                + "        this." + mClasse + "Service.update(this.selecionado)\n"
                + "            .then(salvo => {\n"
                + "                this.selecionado = salvo;\n"
                + "                this.atualiza();\n"
                + "                this.editando = false;\n"
                + "            })\n"
                + "            .catch(erro => {\n"
                + "                console.log('salvar " + mClasse + "', erro);\n"
                + "                this.erro = erro;\n"
                + "            });\n"
                + "    }\n"
                + "    cancelar() {\n"
                + "        this.atualiza();\n"
                + "        this.editando = false;\n"
                + "    }\n"
                + "    excluir() {\n"
                + "        this." + mClasse + "Service.remove(this.selecionado.id)\n"
                + "            .then(obj => {\n"
                + "                this.selecionado = obj;\n"
                + "                this.atualiza();\n"
                + "                this.editando = false;\n"
                + "\n"
                + "            })\n"
                + "            .catch(erro => {\n"
                + "                console.log('excluir " + mClasse + "', erro);\n"
                + "                this.erro = { message: \"Impossível Excluir\", description: \"Este registro está sendo usado\" };\n"
                + "            });\n"
                + "    }\n"
                + "    atualiza() {\n"
                + "        this." + mClasse + "Service\n"
                + "            .vquery({\n"
                + "                orderBy: 'id',\n"
                + "                query: {\n"
                + "                    logicalOperator: \"OR\",\n"
                + "                    subQuerys: [\n"
                + "                        {\n"
                + "                            criteria: {\n"
                + "                                comparisonOperator: \"EQUAL\",\n"
                + "                                field: \"" + atributoMappedBy + ".id\", value: this.proprietario.id\n"
                + "                            }\n"
                + "                        }\n"
                + "                    ]\n"
                + "                }\n"
                + "            }).then(vr => {\n"
                + "                this.lista = vr;\n"
                + "                this.itens = vr.values;\n"
                + "            });\n"
                + "    }\n"
                + "\n"
                + "  public isOwner(obj): boolean {\n"
                + "    return obj && (obj.r.charAt(0) !== '_');\n"
                + "  }\n"
                + "  public commonGroup(obj): boolean {\n"
                + "    return obj && (obj.r.charAt(1) !== '_');\n"
                + "  }\n"
                + "  public canRead(obj): boolean {\n"
                + "    return obj && (obj.r.charAt(2) !== '_');\n"
                + "  }\n"
                + "  public canUpdate(obj): boolean {\n"
                + "    return obj && (obj.r.charAt(3) !== '_');\n"
                + "  }\n"
                + "  public canDelete(obj): boolean {\n"
                + "    return obj && (obj.r.charAt(4) !== '_');\n"
                + "  }\n"
                + "\n"
                + "  public isNew(obj): boolean {\n"
                + "    return obj.version === null;\n"
                + "  }\n"
                + "\n"
                + "  public notNew(obj): boolean {\n"
                + "    return obj.version !== null;\n"
                + "  }"
                + "}\n"
                + "");
        fw.close();
        String arquivoModule = pastaModulo + "/" + atributo.getDeclaringClass().getSimpleName().toLowerCase() + ".module.ts";
        
        
        Util.adicionaLinha(arquivoModule, "/*IMPORTS*/", "import { " + nomeClasseCoponente + "Component } from './detalhes/" + nomeArquivo + ".component';");
        
        Util.adicionaLinha(arquivoModule, "/*DECLARATIONS*/", "      " + nomeClasseCoponente + "Component,");
        
    }
    
    private void geraHTMLOneToMany(Field atributo, String pastaModulo) throws IOException {
        Class tipo = Util.getTipoGenerico(atributo);
        String nomeArquivo = atributo.getDeclaringClass().getSimpleName().toLowerCase() + "-" + atributo.getName().toLowerCase();
        String arquivo = pastaModulo + "/detalhes/" + nomeArquivo + ".component.html";
        String atributoMappedBy = "NAO_TEM";
        OneToMany oneToMany = atributo.getAnnotation(OneToMany.class);
        if (oneToMany != null && oneToMany.mappedBy() != null && (!oneToMany.mappedBy().trim().isEmpty())) {
            atributoMappedBy = oneToMany.mappedBy();
        }
        
        FileWriter fw = abreArquivo(arquivo, false);
        fw.write(""
                + "<div class=\"container\" *ngIf=\"!editando\">\n"
                + "    <h2 class=\"float-left\">" + atributo.getName().toUpperCase() + " {{itens.length}}</h2>\n"
                + "        <button type=\"button\" class=\"btn btn-success float-right\" (click)=\"edita('new')\" [disabled]=\"notNew(proprietario)&&!canUpdate(proprietario)\">\n"
                + "            <i class=\"far fa-file\"></i> Novo</button>\n"
                + "    <vic-tabela [dados]=\"criaVicReturnItens()\" [colunas]=\"colunas\" (acao)=\"edita($event)\" [selecionaColunas]=\"false\"></vic-tabela>\n"
                + "</div>\n"
                + "\n"
                + "<div class=\"container\" *ngIf=\"editando\">\n"
                + "    <h2>" + atributo.getName().toUpperCase() + "</h2>\n"
                + "    <div class=\"row\">\n");
        for (Field a : Util.getTodosAtributosNaoBase(tipo)) {
            if (!a.getName().equals(atributoMappedBy)) {
                geraCampoAtributo(fw, a, pastaModulo);
            }
        }
        fw.write(""
                + "    </div>\n"
                + "    <div class=\"row\">\n"
                + "        <div class=\"col-sm-6 text-left\">\n"
                + "            <button type=\"button\" class=\"btn btn-danger\" (click)=\"excluir()\" [disabled]=\"isNew(selecionado)||!canDelete(selecionado)\">\n"
                + "               <i class=\"far fa-trash-alt\"></i> Excluir</button>\n"
                + "        </div>\n"
                + "        <div class=\"col-sm-6 text-right\">\n"
                + "            <button type=\"button\" class=\"btn btn-warning\" (click)=\"cancelar()\">\n"
                + "                <i class=\"far fa-times-circle\"></i> Cancelar</button>\n"
                + "            <button type=\"button\" class=\"btn btn-success\" (click)=\"salvar()\" [disabled]=\"notNew(selecionado)&&!canUpdate(selecionado)\">\n"
                + "                <i class=\"far fa-save\"></i> Salvar</button>\n"
                + "        </div>\n"
                + "    </div>\n"
                + "</div>\n"
                + "\n");
        
        fw.close();
        
    }
    
}
