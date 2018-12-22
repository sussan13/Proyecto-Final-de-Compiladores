/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package analizador;

import java.util.ArrayList;

public class AnalizadorSintactico {
    private AnalizadorLexico al;
    private ArrayList<Token> tokensValidadosPorAL;
    private AutomataPilaSintactico aps;
    private int indiceToken;
    private boolean huboErrorLexico,huboErrorSintactico;
    public AnalizadorSintactico(AnalizadorLexico analizadorLexico)
    {
        al = analizadorLexico;
        tokensValidadosPorAL = new ArrayList<Token>();
        aps = new AutomataPilaSintactico();
        indiceToken=-1;
        huboErrorLexico=huboErrorSintactico=false;
    }
    /**
     * Hace que el analizador léxico escanee todo el archivo y guarda todo objeto Token que no sea error o espacio.
     */
    private void ejecutarAnalizadorLexico()
    {
        Tokens t;
        boolean sin_errores = true;
        while((t = al.siguienteToken())!=Tokens.EOT)
        {
            if(t != Tokens.ERROR && t != Tokens.ESPACIO) //Ignorar espacios además de errores.
                tokensValidadosPorAL.add(al.getTokenActual());
            if(t==Tokens.ERROR) sin_errores = false;
        }
        if(sin_errores) System.out.println("\033[32mAnálisis léxico sin errores.\033[30m");
    }
    /**
     * Arma y retorna un árbol sintáctico del código fuente.<br>
     * Imprime los errores sintácticos que encuentre por el camino.<br>
     * Si encuentra un error, intenta seguir analizando, aunque eso signifique errores de arrastre.
     * 
     * @return ArbolSintactico que representa la sintáxis concreta del programa fuente
     */
    public ArbolSintactico analizarSintaxis()
    {
        boolean sin_errores = true;
        ArbolSintactico as = new ArbolSintactico();
        ArrayList<Token> instruccion = new ArrayList();
        int llaves_abiertas=0;
        ejecutarAnalizadorLexico();
        if(tokensValidadosPorAL.size()>0)
        {
            for(indiceToken=0;indiceToken<tokensValidadosPorAL.size();indiceToken++)
            {
                if(aps.tokenValidoSintacticamente(tokensValidadosPorAL.get(indiceToken)))
                {
                    instruccion.add(tokensValidadosPorAL.get(indiceToken));
                    if(tokensValidadosPorAL.get(indiceToken).getTipo().equals("LLAVEIZQ"))
                    {
                        llaves_abiertas++;
                    }
                    if(tokensValidadosPorAL.get(indiceToken).getTipo().equals("LLAVEDER"))
                    {
                        llaves_abiertas--;
                        if(indiceToken==tokensValidadosPorAL.size()-1)
                        {
                            if(llaves_abiertas==0)
                            {
                                as.agregarTerminales(instruccion);
                                instruccion.clear();
                            }
                        }else{
                            if(!instruccion.get(0).getTipo().equals("IF")||!tokensValidadosPorAL.get(indiceToken+1).getTipo().equals("ELSE"))
                            {
                                if(llaves_abiertas==0)
                                {
                                    as.agregarTerminales(instruccion);
                                    instruccion.clear();
                                }
                            }
                        }
                    }
                    if(llaves_abiertas==0&&tokensValidadosPorAL.get(indiceToken).getTipo().equals("PYCOMA"))
                    {
                        if(indiceToken<tokensValidadosPorAL.size()-1)
                        {
                            if(!instruccion.get(0).getTipo().equals("IF")||!tokensValidadosPorAL.get(indiceToken+1).getTipo().equals("ELSE"))
                            {
                                as.agregarTerminales(instruccion);
                                instruccion.clear();
                            }
                        }else{
                            as.agregarTerminales(instruccion);
                            instruccion.clear();
                        }
                    }
                }else{
                    if(llaves_abiertas==0)
                    {
                        instruccion.clear();
                    }else{
                        for(int i=instruccion.size()-2;i>-1;i++)
                        {
                            if(instruccion.get(i).getTipo().equals("LLAVEIZQ")||instruccion.get(i).getTipo().equals("PYCOMA"))
                            {
                                instruccion.removeAll(instruccion.subList(i+1,instruccion.size()));
                                break;
                            }
                            if(i==0)
                                instruccion.removeAll(instruccion.subList(i,instruccion.size()));
                        }
                    }
                    sin_errores=false;
                }
            }
            if(sin_errores)
                sin_errores = aps.declararFinDeEntrada();
            else
                aps.declararFinDeEntrada();
            if(sin_errores) System.out.println("\033[32mAnálisis sintáctico sin errores.\033[30m");
        }else{
            System.out.println("\033[32mAnálisis sintáctico sin errores.\033[30m");
        }
        return as;
    }

    public boolean huboErrorLexico() {
        return huboErrorLexico;
    }

    public boolean huboErrorSintactico() {
        return huboErrorSintactico;
    }
    
}