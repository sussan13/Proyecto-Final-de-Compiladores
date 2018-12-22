/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package analizador;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author JLCG17
 */
public class GeneradorDeCodigoIntermedio {
    private AnalizadorSemantico as;
    private ArbolSintactico arsi;
    private BufferedWriter bw;
    private int numeroLabel;
    private ArrayList<Integer> numeroLabelBucle;
    private int numeroTermino,numeroCondicion;
    public GeneradorDeCodigoIntermedio(AnalizadorSemantico as)
    {
        this.as=as;
        numeroLabel=1;
        numeroLabelBucle = new ArrayList();
        numeroTermino=numeroCondicion=-1;
    }
    /**
     * Genera un archivo con el código intermedio de tres direcciones hecho en C (debe ser guardado con extension .CPP).
     * 
     * @param directorioDestino String con la direccion, el nombre del archivo y la extension
     * @throws IOException cuando haya algún problema con el archivo
     */
    public void generarCodigoIntermedio(String directorioDestino) throws IOException
    {
        try{
            arsi = as.analizarSemantica();
            File archivo = new File(directorioDestino);
            FileWriter fw = new FileWriter(archivo);
            bw = new BufferedWriter(fw);
            bw.write("/*CODIGO INTERMEDIO GENERADO AUTOMATICAMENTE POR EL COMPILADOR DEL LENGUAJE FJL.\n"
                    + "DESARROLLADO POR F. ROSSI, J. CABALLERO Y L. VITALE.*/\n"
                    + "#include<iostream>\n"
                    + "#include<string.h>\n"
                    + "using namespace std;\n"
                    + "//NO BORRAR: NECESARIO PARA LAS VARIABLES DEL CODIGO DE TRES DIRECCIONES.\n"
                    + "template<class t> class v;\n"
                    + "typedef v<double> var;\n"
                    + "template<class t> class v{\n"
                    + "private:\n"
                    + "	t valor;\n"
                    + "	bool esLong;\n"
                    + "public:\n"
                    + "	v(t arg){valor=arg;esLong=(strcmp(typeid(arg).name(),\"long\")==0);if(strcmp(typeid(arg).name(),\"class v<double>\")==0)esLong=v<double>(arg).hasLongValue();}\n"
                    + "	void operator =(t arg){v(arg);}\n"
                    + "	void operator ++(){valor=valor+1;}\n"
                    + "	void operator --(){valor=valor-1;}\n"
                    + "	friend void operator +=(var &acumulador,var operando);\n"
                    + "	friend void operator -=(var &acumulador,var operando);\n"
                    + "	friend void operator *=(var &acumulador,var operando);\n"
                    + "	friend void operator /=(var &acumulador,var operando);\n"
                    + "	friend ostream& operator <<(ostream& os,const var val);\n"
                    + "	bool hasLongValue(){return esLong;}\n"
                    + "	template<class t> friend t operator +(t operando, var val);\n"
                    + "	template<class t> friend t operator -(t operando, var val);\n"
                    + "	template<class t> friend t operator *(t operando, var val);\n"
                    + "	template<class t> friend t operator /(t operando, var val);\n"
                    + "	friend bool operator ==(var v1,var v2);\n"
                    + "	friend bool operator !=(var v1,var v2);\n"
                    + "	friend bool operator >(var v1,var v2);\n"
                    + "	friend bool operator <(var v1,var v2);\n"
                    + "	friend bool operator >=(var v1,var v2);\n"
                    + "	friend bool operator <=(var v1,var v2);\n"
                    + "};\n"
                    + "ostream& operator <<(ostream& os, const var val){os << val.valor;return os;}\n"
                    + "template<class t>t operator +(t operando, var val){operando+=val.valor;return operando;}\n"
                    + "template<class t>t operator -(t operando, var val){operando-=val.valor;return operando;}\n"
                    + "template<class t>t operator *(t operando, var val){operando*=val.valor;return operando;}\n"
                    + "template<class t>t operator /(t operando, var val){operando/=val.valor;return operando;}\n"
                    + "void operator +=(var &acumulador,var operando){acumulador.valor=acumulador.valor+operando.valor;}\n"
                    + "void operator -=(var &acumulador,var operando){acumulador.valor=acumulador.valor-operando.valor;}\n"
                    + "void operator *=(var &acumulador,var operando){acumulador.valor=acumulador.valor*operando.valor;}\n"
                    + "void operator /=(var &acumulador,var operando){if(acumulador.esLong)acumulador.valor=long(acumulador.valor/operando.valor);else acumulador.valor=acumulador.valor/operando.valor;}\n"
                    + "bool operator ==(var v1,var v2){return (v1.valor==v2.valor);}\n"
                    + "bool operator !=(var v1,var v2){return (v1.valor!=v2.valor);}\n"
                    + "bool operator <(var v1,var v2){return (v1.valor<v2.valor);}\n"
                    + "bool operator >(var v1,var v2){return (v1.valor>v2.valor);}\n"
                    + "bool operator >=(var v1,var v2){return (v1.valor>=v2.valor);}\n"
                    + "bool operator <=(var v1,var v2){return (v1.valor<=v2.valor);}\n");
            bw.write("//DENTRO DE ESTE MAIN ESTA EL CODIGO DE TRES DIRECCIONES\nvoid main(){\n");
            escribirSecuencia(arsi.getRaiz());
            bw.write("}\n");
            bw.close();
            fw.close();
        }catch(Error exc){System.err.println(exc.getMessage());}
    }
    private void escribirSecuencia(NodoSintactico inicioSecuencia) throws IOException
    {
        ArrayList<NodoSintactico> secuencia = arsi.obtenerSecuencia(inicioSecuencia);
        for(int i=0;i<secuencia.size();i++)
        {
            if(secuencia.get(i).tieneDerivados())
            {
                if(secuencia.get(i).getDerivado(0).getNombre().equals("CONTROL"))
                {
                    switch(secuencia.get(i).getDerivado(0).getDerivado(0).getNombre())
                    {
                        case "BREAK":
                            escribirBreak();
                            break;
                        case "CONTINUE":
                            escribirContinue();
                            break;
                    }
                }else{
                    switch(secuencia.get(i).getDerivado(0).getNombre())
                    {
                        case "ORACION":
                            switch(secuencia.get(i).getDerivado(0).getDerivado(0).getDerivado(0).getNombre())
                            {
                                case "ASIGNACION":
                                    escribirAsignacion(secuencia.get(i).getDerivado(0).getDerivado(0).getDerivado(0));
                                    break;
                                case "DECLARACION":
                                    escribirDeclaracion(secuencia.get(i).getDerivado(0).getDerivado(0).getDerivado(0));
                                    break;
                                case "ESCRIBIR":
                                    escribirEscribir(secuencia.get(i).getDerivado(0).getDerivado(0).getDerivado(0));
                                    break;
                                case "LEER":
                                    escribirLeer(secuencia.get(i).getDerivado(0).getDerivado(0).getDerivado(0));
                                    break;
                            }
                            break;
                        case "MIENTRAS":
                            escribirMientras(secuencia.get(i).getDerivado(0));
                            break;
                        case "SI":
                            escribirSi(secuencia.get(i).getDerivado(0));
                            break;
                        case "SIBUCLE":
                            escribirSi(secuencia.get(i).getDerivado(0));
                            break;
                    }
                }
            }
        }
    }
    private void escribirAsignacion(NodoSintactico asignacion) throws IOException
    {
        String linea = asignacion.getDerivado(0).getToken().getToken();
        if(asignacion.getDerivados().size()==2)
        {
            if(asignacion.getDerivado(1).getNombre().equals("MASUNO"))
            {
                linea+="++;\n";
            }else{
                linea+="--;\n";
            }
        }else{
            switch(asignacion.getDerivado(1).getNombre())
            {
                case "ASIGNADOR":
                    linea+=" = ";
                    break;
                case "DIVIDIDONUMERO":
                    linea+=" /= ";
                    break;
                case "MASNUMERO":
                    linea+=" += ";
                    break;
                case "MENOSNUMERO":
                    linea+=" -= ";
                    break;
                case "PORNUMERO":
                    linea+=" *= ";
                    break;
            }
            escribirTermino(asignacion.getDerivado(2));
            linea+="termino;\n";
        }
        bw.write(linea);
    }
    private void escribirDeclaracion(NodoSintactico declaracion) throws IOException
    {
        if(declaracion.getDerivado(1).getNombre().equals("LISTADO"))
        {
            NodoSintactico aux = declaracion.getDerivado(1);
            while(aux.tieneDerivados())
            {
                bw.write("var "+aux.getDerivado(0).getToken().getToken()+";\n");
                aux=aux.getDerivado(0);
            }
        }
        if(declaracion.getDerivados().size()>2)
        {
            escribirTermino(declaracion.getDerivado(3));
            bw.write("var "+declaracion.getDerivado(1).getToken().getToken()+" = termino;\n");
        }
    }
    private void escribirEscribir(NodoSintactico escribir) throws IOException
    {
        String linea = "cout << ";
        if(escribir.getDerivado(1).getDerivados().size()==1)
        {
            linea+=escribir.getDerivado(1).getDerivado(0).getDerivado(0).getToken().getToken()+" ";
            linea+="<< endl;\n";
            bw.write(linea);
        }else{
            escribirTermino(escribir.getDerivado(1));
            bw.write("cout << termino << endl;\n");
        }
    }
    private void escribirLeer(NodoSintactico leer) throws IOException
    {
        bw.write("cin >> "+leer.getDerivado(1).getToken().getToken()+";\n");
    }
    private void escribirMientras(NodoSintactico mientras) throws IOException
    {
        int nl = numeroLabel;
        numeroLabel+=2;
        numeroLabelBucle.add(new Integer(nl));
        if(numeroCondicion==-1)
        {
            bw.write("var condicion;\n");
            numeroCondicion++;
        }
        bw.write("label"+(nl+1)+":\n");
        escribirCondicion(mientras.getDerivado(2));
        bw.write("if(!(condicion))\n");
        bw.write("goto label"+nl+";\n");
        if(mientras.getDerivado(4).esTerminal())
        {
            escribirSecuencia(mientras.getDerivado(5));
        }else{
            if(mientras.getDerivado(4).getNombre().equals("CONTROL"))
            {
                switch(mientras.getDerivado(4).getDerivado(0).getNombre())
                {
                    case "BREAK":
                        escribirBreak();
                        break;
                    case "CONTINUE":
                        escribirContinue();
                        break;
                }
            }else{
                switch(mientras.getDerivado(4).getDerivado(0).getNombre())
                {
                    case "ORACION":
                        switch(mientras.getDerivado(4).getDerivado(0).getDerivado(0).getDerivado(0).getNombre())
                        {
                            case "ASIGNACION":
                                escribirAsignacion(mientras.getDerivado(4).getDerivado(0).getDerivado(0).getDerivado(0));
                                break;
                            case "DECLARACION":
                                escribirDeclaracion(mientras.getDerivado(4).getDerivado(0).getDerivado(0).getDerivado(0));
                                break;
                            case "ESCRIBIR":
                                escribirEscribir(mientras.getDerivado(4).getDerivado(0).getDerivado(0).getDerivado(0));
                                break;
                            case "LEER":
                                escribirLeer(mientras.getDerivado(4).getDerivado(0).getDerivado(0).getDerivado(0));
                                break;
                        }
                        break;
                    case "MIENTRAS":
                        escribirMientras(mientras.getDerivado(4).getDerivado(0));
                        break;
                    case "SI":
                        escribirSi(mientras.getDerivado(4).getDerivado(0));
                        break;
                    case "SIBUCLE":
                        escribirSi(mientras.getDerivado(4).getDerivado(0));
                        break;
                }
            }
        }
        bw.write("goto label"+(nl+1)+";\n");
        bw.write("label"+nl+":\n");
        numeroLabelBucle.remove(numeroLabelBucle.size()-1);
    }
    private void escribirSi(NodoSintactico si) throws IOException
    {
        int nl = numeroLabel;
        numeroLabel+=2;
        if(numeroCondicion==-1)
        {
            bw.write("var condicion;\n");
            numeroCondicion++;
        }
        escribirCondicion(si.getDerivado(2));
        bw.write("if(!(condicion))\n");
        bw.write("goto label"+nl+";\n");
        if(si.getDerivado(4).esTerminal())
        {
            escribirSecuencia(si.getDerivado(5));
        }else{
            if(si.getDerivado(4).getNombre().equals("CONTROL"))
            {
                switch(si.getDerivado(4).getDerivado(0).getNombre())
                {
                    case "BREAK":
                        escribirBreak();
                        break;
                    case "CONTINUE":
                        escribirContinue();
                        break;
                }
            }else{
                switch(si.getDerivado(4).getDerivado(0).getNombre())
                {
                    case "ORACION":
                        switch(si.getDerivado(4).getDerivado(0).getDerivado(0).getDerivado(0).getNombre())
                        {
                            case "ASIGNACION":
                                escribirAsignacion(si.getDerivado(4).getDerivado(0).getDerivado(0).getDerivado(0));
                                break;
                            case "DECLARACION":
                                escribirDeclaracion(si.getDerivado(4).getDerivado(0).getDerivado(0).getDerivado(0));
                                break;
                            case "ESCRIBIR":
                                escribirEscribir(si.getDerivado(4).getDerivado(0).getDerivado(0).getDerivado(0));
                                break;
                            case "LEER":
                                escribirLeer(si.getDerivado(4).getDerivado(0).getDerivado(0).getDerivado(0));
                                break;
                        }
                        break;
                    case "MIENTRAS":
                        escribirMientras(si.getDerivado(4).getDerivado(0));
                        break;
                    case "SI":
                        escribirSi(si.getDerivado(4).getDerivado(0));
                        break;
                    case "SIBUCLE":
                        escribirSi(si.getDerivado(4).getDerivado(0));
                        break;
                }
            }
        }
        if(!si.getDerivado(si.getDerivados().size()-1).getNombre().equals("SINO"))
        {
            bw.write("label"+nl+":\n");
        }else{
            bw.write("goto label"+(nl+1)+";\n");
            bw.write("label"+nl+":\n");
            if(si.getDerivado(si.getDerivados().size()-1).getDerivado(1).esTerminal())
            {
                escribirSecuencia(si.getDerivado(si.getDerivados().size()-1).getDerivado(2));
            }else{
                if(si.getDerivado(si.getDerivados().size()-1).getDerivado(1).getNombre().equals("CONTROL"))
                {
                    switch(si.getDerivado(si.getDerivados().size()-1).getDerivado(1).getDerivado(0).getNombre())
                    {
                        case "BREAK":
                            escribirBreak();
                            break;
                        case "CONTINUE":
                            escribirContinue();
                            break;
                    }
                }else{
                    switch(si.getDerivado(si.getDerivados().size()-1).getDerivado(1).getDerivado(0).getNombre())
                    {
                        case "ORACION":
                            switch(si.getDerivado(si.getDerivados().size()-1).getDerivado(1).getDerivado(0).getDerivado(0).getDerivado(0).getNombre())
                            {
                                case "ASIGNACION":
                                    escribirAsignacion(si.getDerivado(si.getDerivados().size()-1).getDerivado(1).getDerivado(0).getDerivado(0).getDerivado(0));
                                    break;
                                case "DECLARACION":
                                    escribirDeclaracion(si.getDerivado(si.getDerivados().size()-1).getDerivado(1).getDerivado(0).getDerivado(0).getDerivado(0));
                                    break;
                                case "ESCRIBIR":
                                    escribirEscribir(si.getDerivado(si.getDerivados().size()-1).getDerivado(1).getDerivado(0).getDerivado(0).getDerivado(0));
                                    break;
                                case "LEER":
                                    escribirLeer(si.getDerivado(si.getDerivados().size()-1).getDerivado(1).getDerivado(0).getDerivado(0).getDerivado(0));
                                    break;
                            }
                            break;
                        case "MIENTRAS":
                            escribirMientras(si.getDerivado(si.getDerivados().size()-1).getDerivado(1).getDerivado(0));
                            break;
                        case "SI":
                            escribirSi(si.getDerivado(si.getDerivados().size()-1).getDerivado(1).getDerivado(0));
                            break;
                        case "SIBUCLE":
                            escribirSi(si.getDerivado(si.getDerivados().size()-1).getDerivado(1).getDerivado(0));
                            break;
                    }
                }
            }
            bw.write("label"+(nl+1)+":\n");
        }
    }
    private void escribirTermino(NodoSintactico termino) throws IOException
    {
        //código que vaya desde abajo a arriba en el árbol armando los temporales
        int nT = numeroTermino;
        if(termino.getDerivados().size()==1)
        {
            if(nT<0)
            {
                bw.write("var termino = "+termino.getDerivado(0).getDerivado(0).getToken().getToken()+";\n");
                nT++;
            }
            else
                bw.write("termino = "+termino.getDerivado(0).getDerivado(0).getToken().getToken()+";\n");
        }else{
            ArrayList<NodoSintactico> terminales = arsi.obtenerTodosLosTerminalesHijos(termino);
            if(terminales.size()==3)
            {
                bw.write("var termino = "+terminales.get(0).getToken().getToken()+" "+terminales.get(1).getToken().getToken()+" "+terminales.get(2).getToken().getToken()+";\n");
            }else{
                if(arsi.obtenerTodosLosDerivados(termino, "PARIZQ").isEmpty())
                {
                    boolean esDivOPor;
                    int ultimaSumaOResta = -1;
                    for(int i=1;i<terminales.size();i+=2)
                    {
                        esDivOPor=terminales.get(i).getNombre().equals("POR")||terminales.get(i).getNombre().equals("DIVISION");
                        if(esDivOPor)
                        {
                            if(i==1)
                            {
                                bw.write("var termino;\n");
                                nT++;
                                bw.write("var termino"+nT+" = "+terminales.get(i-1).getToken().getToken()+" "+terminales.get(i).getToken().getToken()+" "+terminales.get(i+1).getToken().getToken()+";\n");
                            }else{
                                if(ultimaSumaOResta>-1)
                                {
                                    if(ultimaSumaOResta==i-2)
                                    {
                                        bw.write("termino"+nT+" = "+terminales.get(i-1).getToken().getToken()+" "+terminales.get(i).getToken().getToken()+" "+terminales.get(i+1).getToken().getToken()+";\n");
                                    }else{
                                        bw.write("termino"+nT+" = termino"+nT+" "+terminales.get(i).getToken().getToken()+" "+terminales.get(i+1).getToken().getToken()+";\n");
                                        if(i==terminales.size()-2)
                                        {
                                            if(ultimaSumaOResta==-1)
                                                bw.write("termino = termino"+nT+";\n");
                                            else
                                                bw.write("termino = termino "+terminales.get(ultimaSumaOResta).getToken().getToken()+" termino"+nT+";\n");
                                        }
                                    }
                                }else{
                                    bw.write("termino"+nT+" = termino"+nT+" "+terminales.get(i).getToken().getToken()+" "+terminales.get(i+1).getToken().getToken()+";\n");
                                    if(i==terminales.size()-2)
                                    {
                                        if(ultimaSumaOResta==-1)
                                            bw.write("termino = termino"+nT+";\n");
                                        else
                                            bw.write("termino = termino "+terminales.get(ultimaSumaOResta).getToken().getToken()+" termino"+nT+";\n");
                                    }
                                }
                            }
                        }else{
                            if(i==1)
                            {
                                bw.write("var termino = "+terminales.get(i-1).getToken().getToken()+";\n");
                                nT++;
                                bw.write("var termino"+nT+" = "+terminales.get(i+1).getToken().getToken()+";\n");
                            }else{
                                if(ultimaSumaOResta==-1)
                                {
                                    bw.write("termino = termino"+nT+";\n");
                                }else{
                                    if(ultimaSumaOResta==i-2&&ultimaSumaOResta>1) bw.write("termino"+nT+" = "+terminales.get(i-1).getToken().getToken()+";\n");
                                    bw.write("termino = termino "+terminales.get(ultimaSumaOResta).getToken().getToken()+" termino"+nT+";\n");
                                }
                                if(i==terminales.size()-2)
                                {
                                    bw.write("termino = termino "+terminales.get(i).getToken().getToken()+" "+terminales.get(i+1).getToken().getToken()+";\n");
                                }
                            }
                            ultimaSumaOResta=i;
                        }
                    }
                }else{
                    String linea="";
                    if(numeroTermino==-1)
                    {
                        linea+="var ";
                        numeroCondicion++;
                    }
                    linea+="termino = ";
                    for(int i=0;i<terminales.size();i++)
                    {
                        linea+=terminales.get(i).getToken().getToken();
                    }
                    linea+=";\n";
                    bw.write(linea);
                }
            }
        }
        numeroTermino=nT;
    }
    private void escribirCondicion(NodoSintactico condicion) throws IOException
    {
        //código que vaya desde abajo a arriba en el árbol armando los temporales
        ArrayList<NodoSintactico> terminales = arsi.obtenerTodosLosTerminalesHijos(condicion);
        String linea="condicion = ";
        for(int i=0;i<terminales.size();i++)
        {
            if(terminales.get(i).getToken().getToken().equals("<>")) linea+="!=";
            else linea+=terminales.get(i).getToken().getToken();
        }
        linea+=";\n";
        bw.write(linea);
    }
    private void escribirBreak() throws IOException
    {
        bw.write("goto label"+numeroLabelBucle.get(numeroLabelBucle.size()-1).intValue()+";\n");
    }
    private void escribirContinue() throws IOException
    {
        bw.write("goto label"+(numeroLabelBucle.get(numeroLabelBucle.size()-1).intValue()+1)+";\n");
    }
}
