/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package analizador;

import java.util.ArrayList;


public class AnalizadorSemantico {
    private final AnalizadorSintactico ansi;
    private ArbolSintactico arsi;
    private boolean sin_errores;
    public AnalizadorSemantico(AnalizadorSintactico as)
    {
        ansi=as;
        sin_errores = true;
    }
    
    public ArbolSintactico analizarSemantica() throws Error
    {
        arsi = ansi.analizarSintaxis();
        analizarSecuencia(arsi.getRaiz(),new ArrayList());
        if(sin_errores)
            System.out.println("\033[32mAnálisis semántico sin errores.\033[30m");
        if(ansi.huboErrorLexico()||ansi.huboErrorSintactico()||!sin_errores)
        {
            throw new Error("No se puede generar el código intermedio si hubo algún error en el análisis.");
        }
        return arsi;
    }
    private void analizarSecuencia(NodoSintactico inicioSecuencia,ArrayList<Variable> variablesGlobales)
    {
        ArrayList<Variable> variablesDeclaradas = new ArrayList();
        if(!variablesGlobales.isEmpty()) variablesDeclaradas.addAll(variablesGlobales);
        ArrayList<NodoSintactico> declaraciones = arsi.obtenerDerivadosDirectosDeUnaSecuencia(inicioSecuencia, "DECLARACION");
        ArrayList<NodoSintactico> asignaciones = arsi.obtenerDerivadosDirectosDeUnaSecuencia(inicioSecuencia, "ASIGNACION");
        ArrayList<NodoSintactico> leeres = arsi.obtenerDerivadosDirectosDeUnaSecuencia(inicioSecuencia, "LEER");
        ArrayList<NodoSintactico> sies = arsi.obtenerDerivadosDirectosDeUnaSecuencia(inicioSecuencia, "SI");
        ArrayList<NodoSintactico> siBucles = arsi.obtenerDerivadosDirectosDeUnaSecuencia(inicioSecuencia, "SIBUCLE");
        ArrayList<NodoSintactico> mientras = arsi.obtenerDerivadosDirectosDeUnaSecuencia(inicioSecuencia, "MIENTRAS");
        ArrayList<NodoSintactico> escribires = arsi.obtenerDerivadosDirectosDeUnaSecuencia(inicioSecuencia, "ESCRIBIR");
        if(!variablesGlobales.isEmpty()) variablesDeclaradas.addAll(variablesGlobales);
        analizarDeclaraciones(declaraciones,variablesDeclaradas);
        analizarAsignaciones(asignaciones,variablesDeclaradas);
        analizarLeeres(leeres,variablesDeclaradas);
        analizarSies(sies,variablesDeclaradas);
        analizarSies(siBucles,variablesDeclaradas);
        analizarMientras(mientras,variablesDeclaradas);
        analizarEscribires(escribires,variablesDeclaradas);
    }
    private void analizarDeclaraciones(ArrayList<NodoSintactico> declaraciones,ArrayList<Variable> variablesDeclaradas)
    {
        for(int i=0;i<declaraciones.size();i++)
        {
            if(declaraciones.get(i).getDerivado(1).getNombre().equals("LISTADO"))
            {
                //DECLARACION->TIPO->(DOUBLE|LONG)
                Token tipo = declaraciones.get(i).getDerivado(0).getDerivado(0).getToken();
                ArrayList<NodoSintactico> listados = arsi.obtenerTodosLosDerivados(declaraciones.get(i),"LISTADO");
                for(int j=0;j<listados.size();j++)
                {
                    Token identificador = listados.get(j).getDerivado(0).getToken();
                    if(variablesDeclaradas.isEmpty())
                    {
                        variablesDeclaradas.add(new Variable(tipo,identificador));
                    }else{
                        for(int k=0;k<variablesDeclaradas.size();k++)
                        {
                            if(variablesDeclaradas.get(k).getIdentificador().getToken().equals(identificador.getToken()))
                            {
                                System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": la variable \""+identificador.getToken()+"\" ya fue declarada.");
                                sin_errores=false;
                                break;
                            }else{
                                if(k==variablesDeclaradas.size()-1)
                                {
                                    variablesDeclaradas.add(new Variable(tipo,identificador));
                                    break;
                                }
                            }
                        }
                    }
                }
            }else{
                Token tipo = declaraciones.get(i).getDerivado(0).getDerivado(0).getToken();
                Token identificador = declaraciones.get(i).getDerivado(1).getToken();
                boolean saltear=false;
                if(variablesDeclaradas.isEmpty())
                {
                    variablesDeclaradas.add(new Variable(tipo,identificador));
                }else{
                    for(int k=0;k<variablesDeclaradas.size();k++)
                    {
                        if(variablesDeclaradas.get(k).getIdentificador().getToken().equals(identificador.getToken()))
                        {
                            System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": la variable \""+identificador.getToken()+"\" ya fue declarada.");
                            sin_errores=false;
                            saltear=true;
                            break;
                        }else{
                            if(k==variablesDeclaradas.size()-1)
                            {
                                variablesDeclaradas.add(new Variable(tipo,identificador));
                                break;
                            }
                        }
                    }
                }
                if(!saltear)
                {
                    //DECLARACION->TERMINO->OPERANDO?
                    if(declaraciones.get(i).getDerivado(3).getDerivados().size()==1)
                    {
                        //DECLARACION->TERMINO->OPERANDO->(NUMERO_DOUBLE|NUMERO_LONG)
                        Token valor = declaraciones.get(i).getDerivado(3).getDerivado(0).getDerivado(0).getToken();
                        if(valor.getTipo().equals("NUMERO_DOUBLE"))
                        {
                            if(tipo.getTipo().equals("LONG"))
                            {
                                System.err.println("Error semántico en línea "+(valor.getIndiceFila()+1)+", caracter "+(valor.getIndiceComienzo()+1)+": \""+valor.getToken()+"\" es un número double que se está asignando a una variable long.");
                                sin_errores=false;
                            }else{
                                variablesDeclaradas.get(i).setValor(declaraciones.get(i).getDerivado(3).getDerivado(0).getDerivado(0));
                            }
                        }
                        if(valor.getTipo().equals("NUMERO_LONG"))
                        {
                            if(tipo.getTipo().equals("LONG"))
                            {
                                variablesDeclaradas.get(i).setValor(declaraciones.get(i).getDerivado(3).getDerivado(0).getDerivado(0));
                            }else{
                                System.err.println("Error semántico en línea "+(valor.getIndiceFila()+1)+", caracter "+(valor.getIndiceComienzo()+1)+": \""+valor.getToken()+"\" es un número long que se está asignando a una variable double.");
                                sin_errores=false;
                            }
                        }
                        if(valor.getTipo().equals("IDENTIFICADOR"))
                        {
                            if(valor.getToken().equals(identificador.getToken()))
                            {
                                System.err.println("Error semántico en línea "+(valor.getIndiceFila()+1)+", caracter "+(valor.getIndiceComienzo()+1)+": la variable \""+identificador.getToken()+"\" está asignada a sí misma.");
                                sin_errores = false;
                            }else{
                                Token id2=valor;
                                for(int k=0;k<variablesDeclaradas.size();k++)
                                {
                                    Variable actual = variablesDeclaradas.get(k);
                                    if(actual.getIdentificador().getToken().equals(id2.getToken()))
                                    {
                                        Variable ultima = variablesDeclaradas.get(variablesDeclaradas.size()-1);
                                        if(actual.tieneValor())
                                        {
                                            if(actual.getTipo().getToken().equals(ultima.getTipo().getToken()))
                                            {
                                                ultima.setValor(declaraciones.get(i).getDerivado(3).getDerivado(0).getDerivado(0));
                                            }else{
                                                System.err.println("Error semántico en línea "+(id2.getIndiceFila()+1)+", caracter "+(id2.getIndiceComienzo()+1)+": la variable \""+ultima.getIdentificador().getToken()+"\" es "+ultima.getTipo().getToken()+" y se quiere asignar a \""+actual.getIdentificador().getToken()+"\", que es "+actual.getTipo().getToken()+".");
                                                sin_errores = false;
                                            }
                                        }else{
                                            System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": la variable \""+identificador.getToken()+"\" no tiene valor asignado.");
                                            sin_errores = false;
                                        }
                                        break;
                                    }else{
                                        if(k==variablesDeclaradas.size()-1)
                                        {
                                            System.err.println("Error semántico en línea "+(id2.getIndiceFila()+1)+", caracter "+(id2.getIndiceComienzo()+1)+": la variable \""+id2.getToken()+"\" no ha sido declarada.");
                                            sin_errores = false;
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        if(tipo.getTipo().equals("LONG"))
                        {
                            if(!arsi.obtenerTodosLosDerivados(declaraciones.get(i), "NUMERO_DOUBLE").isEmpty())
                            {
                                Token error = arsi.obtenerTodosLosDerivados(declaraciones.get(i), "NUMERO_DOUBLE").get(0).getToken();
                                System.err.println("Error semántico en línea "+(error.getIndiceFila()+1)+", caracter "+(error.getIndiceComienzo()+1)+": \""+error.getToken()+"\" es un número double dentro de un cálculo de long.");
                                sin_errores=false;
                            }else{
                                variablesDeclaradas.get(variablesDeclaradas.size()-1).setValor(declaraciones.get(i).getDerivado(3));
                            }
                        }else{
                            if(!arsi.obtenerTodosLosDerivados(declaraciones.get(i), "NUMERO_LONG").isEmpty())
                            {
                                Token error = arsi.obtenerTodosLosDerivados(declaraciones.get(i), "NUMERO_LONG").get(0).getToken();
                                System.err.println("Error semántico en línea "+(error.getIndiceFila()+1)+", caracter "+(error.getIndiceComienzo()+1)+": \""+error.getToken()+"\" es un número long dentro de un cálculo de double.");
                                sin_errores=false;
                            }else{
                                variablesDeclaradas.get(variablesDeclaradas.size()-1).setValor(declaraciones.get(i).getDerivado(3));
                            }
                        }
                    }
                }
            }
        }
    }
    private void analizarAsignaciones(ArrayList<NodoSintactico> asignaciones,ArrayList<Variable> variablesDeclaradas)
    {
        for(int i=0;i<asignaciones.size();i++)
        {
            Token identificador = asignaciones.get(i).getDerivado(0).getToken();
            boolean saltear = false;
            int ivar=-1;
            if(variablesDeclaradas.isEmpty())
            {
                System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                sin_errores=false;
                saltear=true;
            }else{
                for(int j=0;j<variablesDeclaradas.size();j++)
                {
                    if(variablesDeclaradas.get(j).getIdentificador().getToken().equals(identificador.getToken()))
                    {
                        ivar = j;
                        break;
                    }else{
                        if(j==variablesDeclaradas.size()-1)
                        {
                            System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                            sin_errores=false;
                            saltear=true;
                        }
                    }
                }
            }
            if(!saltear)
            {
                Token tipo = variablesDeclaradas.get(ivar).getTipo();
                if(asignaciones.get(i).getDerivados().size()==2)
                {
                    if(!variablesDeclaradas.get(ivar).tieneValor())
                    {
                        System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": la variable \""+identificador.getToken()+"\" no tiene valor asignado.");
                        sin_errores = false;
                    }
                }else{
                    if(asignaciones.get(i).getDerivado(2).getDerivados().size()==1)
                    {
                        //ASIGNACION->TERMINO->OPERANDO->(NUMERO_LONG|NUMERO_DOUBLE)
                        Token valor = asignaciones.get(i).getDerivado(2).getDerivado(0).getDerivado(0).getToken();
                        if(valor.getTipo().equals("NUMERO_LONG"))
                        {
                            if(tipo.getTipo().equals("LONG"))
                            {
                                int ifd = variablesDeclaradas.get(ivar).getIdentificador().getIndiceFila();
                                int icd = variablesDeclaradas.get(ivar).getIdentificador().getIndiceComienzo();
                                int ifv = valor.getIndiceFila();
                                int icv = valor.getIndiceComienzo();
                                if(ifd<ifv||(ifd==ifv&&icd<icv))
                                {
                                    variablesDeclaradas.get(ivar).setValor(asignaciones.get(i).getDerivado(2).getDerivado(0).getDerivado(0));
                                }else{
                                    System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                                    sin_errores = false;
                                }
                            }else{
                                System.err.println("Error semántico en línea "+(valor.getIndiceFila()+1)+", caracter "+(valor.getIndiceComienzo()+1)+": \""+valor.getToken()+"\" es un número double que se está asignando a una variable long.");
                                sin_errores=false;
                            }
                        }
                        if(valor.getTipo().equals("NUMERO_DOUBLE"))
                        {
                            if(tipo.getTipo().equals("LONG"))
                            {
                                System.err.println("Error semántico en línea "+(valor.getIndiceFila()+1)+", caracter "+(valor.getIndiceComienzo()+1)+": \""+valor.getToken()+"\" es un número double que se está asignando a una variable long.");
                                sin_errores=false;
                            }else{
                                int ifd = variablesDeclaradas.get(ivar).getIdentificador().getIndiceFila();
                                int icd = variablesDeclaradas.get(ivar).getIdentificador().getIndiceComienzo();
                                int ifv = valor.getIndiceFila();
                                int icv = valor.getIndiceComienzo();
                                if(ifd<ifv||(ifd==ifv&&icd<icv))
                                {
                                    variablesDeclaradas.get(ivar).setValor(asignaciones.get(i).getDerivado(2).getDerivado(0).getDerivado(0));
                                }else{
                                    System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                                    sin_errores = false;
                                }
                            }
                        }
                        if(valor.getTipo().equals("IDENTIFICADOR"))
                        {
                            for(int j=0;j<variablesDeclaradas.size();j++)
                            {
                                if(variablesDeclaradas.get(j).getIdentificador().getToken().equals(valor.getToken()))
                                {
                                    if(!variablesDeclaradas.get(j).tieneValor())
                                    {
                                        System.err.println("Error semántico en línea "+(valor.getIndiceFila()+1)+", caracter "+(valor.getIndiceComienzo()+1)+": la variable \""+valor.getToken()+"\" no tiene valor asignado.");
                                        sin_errores = false;
                                    }else{
                                        int ifd = variablesDeclaradas.get(ivar).getIdentificador().getIndiceFila();
                                        int icd = variablesDeclaradas.get(ivar).getIdentificador().getIndiceComienzo();
                                        int ifv = valor.getIndiceFila();
                                        int icv = valor.getIndiceComienzo();
                                        if(ifd<ifv||(ifd==ifv&&icd<icv))
                                        {
                                            variablesDeclaradas.get(ivar).setValor(asignaciones.get(i).getDerivado(2).getDerivado(0).getDerivado(0));
                                        }else{
                                            System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                                            sin_errores = false;
                                        }
                                    }
                                    break;
                                }else{
                                    if(j==variablesDeclaradas.size()-1)
                                    {
                                        System.err.println("Error semántico en línea "+(valor.getIndiceFila()+1)+", caracter "+(valor.getIndiceComienzo()+1)+": no se declaró la variable \""+valor.getToken()+"\".");
                                        sin_errores=false;
                                    }
                                }
                            }
                        }
                    }else{
                        if(tipo.getTipo().equals("LONG"))
                        {
                            if(!arsi.obtenerTodosLosDerivados(asignaciones.get(i), "NUMERO_DOUBLE").isEmpty())
                            {
                                Token error = arsi.obtenerTodosLosDerivados(asignaciones.get(i), "NUMERO_DOUBLE").get(0).getToken();
                                System.err.println("Error semántico en línea "+(error.getIndiceFila()+1)+", caracter "+(error.getIndiceComienzo()+1)+": \""+error.getToken()+"\" es un número double dentro de un cálculo de long.");
                                sin_errores=false;
                            }else{
                                variablesDeclaradas.get(ivar).setValor(asignaciones.get(i).getDerivado(2));
                            }
                        }else{
                            if(!arsi.obtenerTodosLosDerivados(asignaciones.get(i), "NUMERO_LONG").isEmpty())
                            {
                                Token error = arsi.obtenerTodosLosDerivados(asignaciones.get(i), "NUMERO_LONG").get(0).getToken();
                                System.err.println("Error semántico en línea "+(error.getIndiceFila()+1)+", caracter "+(error.getIndiceComienzo()+1)+": \""+error.getToken()+"\" es un número long dentro de un cálculo de double.");
                                sin_errores=false;
                            }else{
                                variablesDeclaradas.get(ivar).setValor(asignaciones.get(i).getDerivado(2));
                            }
                        }
                    }
                }
            }
        }
    }
    private void analizarSies(ArrayList<NodoSintactico> sies,ArrayList<Variable> variablesGlobales)
    {
        ArrayList<Variable> variablesDeclaradas = new ArrayList();
        if(!variablesGlobales.isEmpty()) variablesDeclaradas.addAll(variablesGlobales);
        for(int i=0;i<sies.size();i++)
        {
            ArrayList<NodoSintactico> identificadores = arsi.obtenerTodosLosDerivados(sies.get(i).getDerivado(2), "IDENTIFICADOR");
            for(int j=0;j<identificadores.size();j++)
            {
                Token identificador = identificadores.get(j).getToken();
                if(variablesDeclaradas.isEmpty())
                {
                    System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                    sin_errores = false;
                }else{
                    for(int k=0;k<variablesDeclaradas.size();k++)
                    {
                        if(variablesDeclaradas.get(k).getIdentificador().getToken().equals(identificadores.get(j).getToken().getToken()))
                        {
                            if(!variablesDeclaradas.get(k).tieneValor())
                            {
                                System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": la variable \""+identificador.getToken()+"\" no tiene valor asignado.");
                                sin_errores = false;
                            }
                            break;
                        }else{
                            if(k==variablesDeclaradas.size()-1)
                            {
                                System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                                sin_errores = false;
                            }
                        }
                    }
                }
            }
            if(sies.get(i).getDerivado("SECUENCIA").size()==1)
            {
                if(sies.get(i).getDerivado("SECUENCIA").get(0).tieneDerivados())
                    analizarSecuencia(sies.get(i).getDerivado("SECUENCIA").get(0),variablesDeclaradas);
            }else{
                ArrayList<NodoSintactico> aux = new ArrayList();
                switch(sies.get(i).getDerivado(4).getNombre())
                {
                    case "MIENTRAS":
                        aux.add(sies.get(i).getDerivado(4));
                        analizarMientras(aux,variablesDeclaradas);
                        break;
                    case "ORACION":
                        switch(sies.get(i).getDerivado(4).getDerivado(0).getDerivado(0).getNombre())
                        {
                            case "ASIGNACION":
                                aux.add(sies.get(i).getDerivado(4).getDerivado(0).getDerivado(0));
                                analizarAsignaciones(aux,variablesDeclaradas);
                                break;
                            case "DECLARACION":
                                Token posible_error = sies.get(i).getDerivado(4).getDerivado(0).getDerivado(0).getDerivado(1).getDerivado(0).getToken();
                                System.out.println("\033[35mAdvertencia semántica en línea "+(posible_error.getIndiceFila()+1)+", caracter "+(posible_error.getIndiceComienzo()+1)+": la variable \""+posible_error.getToken()+"\" está declarada en un if de cuerpo de una sola línea.  Se aconseja (y mucho) usar llaves para el cuerpo del if o sacar la declaración del if.\033[30m");
                                aux.add(sies.get(i).getDerivado(4).getDerivado(0).getDerivado(0));
                                analizarDeclaraciones(aux,variablesDeclaradas);
                                break;
                            case "ESCRIBIR":
                                aux.add(sies.get(i).getDerivado(4).getDerivado(0).getDerivado(0));
                                analizarEscribires(aux,variablesDeclaradas);
                                break;
                            case "LEER":
                                aux.add(sies.get(i).getDerivado(4).getDerivado(0).getDerivado(0));
                                analizarLeeres(aux,variablesDeclaradas);
                                break;
                        }
                        break;
                    case "SI":
                        aux.add(sies.get(i).getDerivado(4));
                        analizarSies(aux,variablesDeclaradas);
                        break;
                    case "SIBUCLE":
                        aux.add(sies.get(i).getDerivado(4));
                        analizarSies(aux,variablesDeclaradas);
                        break;
                }
            }
            if(sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1).getNombre().equals("SINOBUCLE"))
            {
                if(sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1).getDerivado("SECUENCIA").size()==1)
                {
                    if(sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1).getDerivado("SECUENCIA").get(0).tieneDerivados())
                        analizarSecuencia(sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1).getDerivado("SECUENCIA").get(0),variablesDeclaradas);
                }else{
                    ArrayList<NodoSintactico> aux = new ArrayList();
                    switch(sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1).getDerivado(1).getNombre())
                    {
                        case "MIENTRAS":
                            aux.add(sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1));
                            analizarMientras(aux,variablesDeclaradas);
                            break;
                        case "ORACION":
                            switch(sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1).getDerivado(1).getDerivado(0).getDerivado(0).getNombre())
                            {
                                case "ASIGNACION":
                                    aux.add(sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1).getDerivado(1).getDerivado(0));
                                    analizarAsignaciones(aux,variablesDeclaradas);
                                    break;
                                case "DECLARACION":
                                    //                                                                          SINO->   ORACION->     INSTRUCCION->  DECLARACION->   LISTADO->    IDENTIFICADOR
                                    Token posible_error = sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1).getDerivado(1).getDerivado(0).getDerivado(0).getDerivado(1).getDerivado(0).getToken();
                                    System.out.println("\033[35mAdvertencia semántica en línea "+(posible_error.getIndiceFila()+1)+", caracter "+(posible_error.getIndiceComienzo()+1)+": la variable \""+posible_error.getToken()+"\" está declarada en un else de cuerpo de una sola línea.  Se aconseja (y mucho) usar llaves para el cuerpo del else o sacar la declaración del else.\033[30m");
                                    aux.add(sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1).getDerivado(1).getDerivado(0).getDerivado(0));
                                    analizarDeclaraciones(aux,variablesDeclaradas);
                                    break;
                                case "ESCRIBIR":
                                    aux.add(sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1).getDerivado(1).getDerivado(0));
                                    analizarEscribires(aux,variablesDeclaradas);
                                    break;
                                case "LEER":
                                    aux.add(sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1).getDerivado(1).getDerivado(0));
                                    analizarLeeres(aux,variablesDeclaradas);
                                    break;
                            }
                            break;
                        case "SI":
                            aux.add(sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1));
                            analizarSies(aux,variablesDeclaradas);
                            break;
                        case "SIBUCLE":
                            aux.add(sies.get(i).getDerivado(sies.get(i).getDerivados().size()-1));
                            analizarSies(aux,variablesDeclaradas);
                            break;
                    }
                }
            }
        }
    }
    private void analizarMientras(ArrayList<NodoSintactico> mientras,ArrayList<Variable> variablesGlobales)
    {
        ArrayList<Variable> variablesDeclaradas = new ArrayList();
        if(!variablesGlobales.isEmpty()) variablesDeclaradas.addAll(variablesGlobales);
        for(int i=0;i<mientras.size();i++)
        {
            ArrayList<NodoSintactico> identificadores = arsi.obtenerTodosLosDerivados(mientras.get(i).getDerivado(2), "IDENTIFICADOR");
            for(int j=0;j<identificadores.size();j++)
            {
                Token identificador = identificadores.get(j).getToken();
                if(variablesDeclaradas.isEmpty())
                {
                    System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                    sin_errores = false;
                }else{
                    for(int k=0;k<variablesDeclaradas.size();k++)
                    {
                        if(variablesDeclaradas.get(k).getIdentificador().getToken().equals(identificadores.get(j).getToken().getToken()))
                        {
                            if(!variablesDeclaradas.get(k).tieneValor())
                            {
                                System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": la variable \""+identificador.getToken()+"\" no tiene valor asignado.");
                                sin_errores = false;
                            }
                            break;
                        }else{
                            if(k==variablesDeclaradas.size()-1)
                            {
                                System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                                sin_errores = false;
                            }
                        }
                    }
                }
            }
            if(mientras.get(i).getDerivado("BLOQUEBUCLE").size()==1)
            {
                if(mientras.get(i).getDerivado("BLOQUEBUCLE").get(0).tieneDerivados())
                    analizarSecuencia(mientras.get(i).getDerivado("BLOQUEBUCLE").get(0),variablesDeclaradas);
            }else{
                ArrayList<NodoSintactico> aux = new ArrayList();
                switch(mientras.get(i).getDerivado(mientras.get(i).getDerivados().size()-1).getNombre())
                {
                    case "MIENTRAS":
                        aux.add(mientras.get(i).getDerivado(mientras.get(i).getDerivados().size()-1));
                        analizarMientras(aux,variablesDeclaradas);
                        break;
                    case "ORACION":
                        switch(mientras.get(i).getDerivado(mientras.get(i).getDerivados().size()-1).getDerivado(0).getDerivado(0).getNombre())
                        {
                            case "ASIGNACION":
                                aux.add(mientras.get(i).getDerivado(mientras.get(i).getDerivados().size()-1).getDerivado(0).getDerivado(0));
                                analizarAsignaciones(aux,variablesDeclaradas);
                                break;
                            case "DECLARACION":
                                Token posible_error = mientras.get(i).getDerivado(mientras.get(i).getDerivados().size()-1).getDerivado(0).getDerivado(0).getDerivado(1).getDerivado(0).getToken();
                                System.out.println("\033[35mAdvertencia semántica en línea "+(posible_error.getIndiceFila()+1)+", caracter "+(posible_error.getIndiceComienzo()+1)+": la variable \""+posible_error.getToken()+"\" está declarada en un while de cuerpo de una sola línea. Se aconseja (y mucho) usar llaves para el cuerpo del while o sacar la declaración del while.\033[30m");
                                aux.add(mientras.get(i).getDerivado(mientras.get(i).getDerivados().size()-1).getDerivado(0).getDerivado(0));
                                analizarDeclaraciones(aux,variablesDeclaradas);
                                break;
                            case "ESCRIBIR":
                                aux.add(mientras.get(i).getDerivado(mientras.get(i).getDerivados().size()-1).getDerivado(0).getDerivado(0));
                                analizarEscribires(aux,variablesDeclaradas);
                                break;
                            case "LEER":
                                aux.add(mientras.get(i).getDerivado(mientras.get(i).getDerivados().size()-1).getDerivado(0).getDerivado(0));
                                analizarLeeres(aux,variablesDeclaradas);
                                break;
                        }
                        break;
                    case "SIBUCLE":
                        aux.add(mientras.get(i).getDerivado(mientras.get(i).getDerivados().size()-1));
                        analizarSies(aux,variablesDeclaradas);
                        break;
                }
            }
        }
    }
    private void analizarEscribires(ArrayList<NodoSintactico> escribires,ArrayList<Variable> variablesGlobales)
    {
        ArrayList<Variable> variablesDeclaradas = new ArrayList();
        if(!variablesGlobales.isEmpty()) variablesDeclaradas.addAll(variablesGlobales);
        for(int i=0;i<escribires.size();i++)
        {
            //ESCRIBIR->TERMINO->OPERANDO?
            if(escribires.get(i).getDerivado(1).getDerivados().size()==1)
            {
                //ESCRIBIR->TERMINO->OPERANDO->IDENTIFICADOR?
                if(escribires.get(i).getDerivado(1).getDerivado(0).getDerivado(0).getNombre().equals("IDENTIFICADOR"))
                {
                    Token identificador = escribires.get(i).getDerivado(1).getDerivado(0).getDerivado(0).getToken();
                    if(variablesDeclaradas.isEmpty())
                    {
                        System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                        sin_errores = false;
                    }else{
                        for(int j=0;j<variablesDeclaradas.size();j++)
                        {
                            if(variablesDeclaradas.get(j).getIdentificador().getToken().equals(identificador.getToken()))
                            {
                                if(!variablesDeclaradas.get(j).tieneValor())
                                {
                                    System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": la variable \""+identificador.getToken()+"\" no tiene valor asignado.");
                                    sin_errores = false;
                                }else{
                                    int ifd = variablesDeclaradas.get(j).getIdentificador().getIndiceFila();
                                    int icd = variablesDeclaradas.get(j).getIdentificador().getIndiceComienzo();
                                    int ifv = identificador.getIndiceFila();
                                    int icv = identificador.getIndiceComienzo();
                                    if(!(ifd<ifv||(ifd==ifv&&icd<icv)))
                                    {
                                        System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                                        sin_errores = false;
                                    }
                                }
                                break;
                            }else{
                                if(j==variablesDeclaradas.size()-1)
                                {
                                    System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                                    sin_errores = false;
                                }
                            }
                        }
                    }
                }
            }else{
                int nds = arsi.obtenerTodosLosDerivados(escribires.get(i).getDerivado(1), "NUMERO_DOUBLE").size();
                int nls = arsi.obtenerTodosLosDerivados(escribires.get(i).getDerivado(1), "NUMERO_LONG").size();
                ArrayList<NodoSintactico> identificadores = arsi.obtenerTodosLosDerivados(escribires.get(i).getDerivado(1), "IDENTIFICADOR");
                boolean hay_var_double = false;
                boolean hay_var_long = false;
                if(nds>0&&nls>0)
                {
                    Token erroneo = escribires.get(i).getDerivado(1).getDerivado(0).getDerivado(0).getToken();
                    System.err.println("Error semántico en línea "+(erroneo.getIndiceFila()+1)+", caracter "+(erroneo.getIndiceComienzo()+1)+": expresión que mezcla números double con números long.");
                    sin_errores = false;
                }
                for(int k=0;k<identificadores.size();k++)
                {
                    for(int j=0;j<variablesDeclaradas.size();j++)
                    {
                        Token identificador = identificadores.get(k).getToken();
                        if(variablesDeclaradas.get(j).getIdentificador().getToken().equals(identificadores.get(k).getToken().getToken()))
                        {
                            if(variablesDeclaradas.get(j).getTipo().getTipo().equals("DOUBLE"))
                            {
                                hay_var_double = true;
                            }
                            if(variablesDeclaradas.get(j).getTipo().getTipo().equals("LONG"))
                            {
                                hay_var_long = true;
                            }
                        }
                        if(variablesDeclaradas.get(j).getIdentificador().getToken().equals(identificador.getToken()))
                        {
                            if(!variablesDeclaradas.get(j).tieneValor())
                            {
                                System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": la variable \""+identificador.getToken()+"\" no tiene valor asignado.");
                                sin_errores = false;
                            }else{
                                int ifd = variablesDeclaradas.get(j).getIdentificador().getIndiceFila();
                                int icd = variablesDeclaradas.get(j).getIdentificador().getIndiceComienzo();
                                int ifv = identificador.getIndiceFila();
                                int icv = identificador.getIndiceComienzo();
                                if(!(ifd<ifv||(ifd==ifv&&icd<icv)))
                                {
                                    System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                                    sin_errores = false;
                                }
                            }
                            break;
                        }else{
                            if(j==variablesDeclaradas.size()-1)
                            {
                                System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                                sin_errores = false;
                            }
                        }
                    }
                }
                if(nds>0&&nls>0)
                {
                    Token erroneo = escribires.get(i).getDerivado(1).getDerivado(0).getDerivado(0).getToken();
                    System.err.println("Error semántico en línea "+(erroneo.getIndiceFila()+1)+", caracter "+(erroneo.getIndiceComienzo()+1)+": expresión que mezcla números double con números long.");
                    sin_errores = false;
                }
                if(nds>0&&hay_var_long)
                {
                    Token erroneo = escribires.get(i).getDerivado(1).getDerivado(0).getDerivado(0).getToken();
                    System.err.println("Error semántico en línea "+(erroneo.getIndiceFila()+1)+", caracter "+(erroneo.getIndiceComienzo()+1)+": expresión que mezcla números double con variables long.");
                    sin_errores = false;
                }
                if(nls>0&hay_var_double)
                {
                    Token erroneo = escribires.get(i).getDerivado(1).getDerivado(0).getDerivado(0).getToken();
                    System.err.println("Error semántico en línea "+(erroneo.getIndiceFila()+1)+", caracter "+(erroneo.getIndiceComienzo()+1)+": expresión que mezcla variables double con números long.");
                    sin_errores = false;
                }
                if(hay_var_double&&hay_var_long)
                {
                    Token erroneo = escribires.get(i).getDerivado(1).getDerivado(0).getDerivado(0).getToken();
                    System.err.println("Error semántico en línea "+(erroneo.getIndiceFila()+1)+", caracter "+(erroneo.getIndiceComienzo()+1)+": expresión que mezcla variables double con variables long.");
                    sin_errores = false;
                }
            }
        }
    }
    private void analizarLeeres(ArrayList<NodoSintactico> leeres,ArrayList<Variable> variablesDeclaradas)
    {
        for(int i=0;i<leeres.size();i++)
        {
            Token identificador = leeres.get(i).getDerivado(1).getToken();
            for(int j=0;j<variablesDeclaradas.size();j++)
            {
                if(variablesDeclaradas.get(j).getIdentificador().getToken().equals(identificador.getToken()))
                {
                    int ifd = variablesDeclaradas.get(j).getIdentificador().getIndiceFila();
                    int icd = variablesDeclaradas.get(j).getIdentificador().getIndiceComienzo();
                    int ifv = identificador.getIndiceFila();
                    int icv = identificador.getIndiceComienzo();
                    if(ifd<ifv||(ifd==ifv&&icd<icv))
                    {
                        variablesDeclaradas.get(j).setValor(new NodoSintactico("INGRESO"));
                    }else{
                        System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                        sin_errores = false;
                    }
                    break;
                }else{
                    if(j==variablesDeclaradas.size()-1)
                    {
                        System.err.println("Error semántico en línea "+(identificador.getIndiceFila()+1)+", caracter "+(identificador.getIndiceComienzo()+1)+": no se declaró la variable \""+identificador.getToken()+"\".");
                        sin_errores = false;
                    }
                }
            }
        }
    }
}