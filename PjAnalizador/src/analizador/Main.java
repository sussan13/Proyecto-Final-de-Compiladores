package analizador;

import java.util.Scanner;

import analizador.AnalizadorLexico;
import  analizador.AnalizadorSemantico;
import  analizador.AnalizadorSintactico;
import analizador.GeneradorDeCodigoIntermedio;
import  analizador.getData;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
public static Scanner tcl;
private static Scanner teclado;
private static String todo;
	
public static void main(String[] args) {
	// TODO Auto-generated method stub	
	//	This structure generates a while of selection 
	//in the console that is structured so that the analyzer works correctly.
		
		
	tcl = new Scanner (System.in);
		
		 	int salir;
	        int i,fila=0,columna=0;
	        String introd;
	        String sw;
	        teclado = new Scanner(System.in);

	        //aqui creamos un arreglo bidimencional con el nombre matriz y agregamos cada simbolo y su nombre
	        String [][] matriz = {{"/","+","=","-","."},{"El signo(/) sirve para representar comentarios en nuestro código de java.",
				"El signo (+) sirve para sumar.",
				"El signo (=) sirve para asignación.", 
				"El signo (-) sirve para restar.",
				"El signo (.) sirve para acceso a variables y métodos."}};
	        System.out.println("...BIENVENIDO...");
	        salir=0;
	        while (salir!=1){
	          
	            System.out.println("Elige Una Opcion");
	            System.out.println("1-Buscar En la Lista local   2-Analizar texto \n 3-Buscar en lista del analizador  4-Todos los analisis 5-Salir");
	            sw=teclado.nextLine();
	            switch (sw)
	            {
	                case "1":
	                    System.out.println("ingrese Signo: ");
	                    introd=teclado.nextLine();

					for(i=0;i<5;){
	                                for(int j=0; j<5;j++){
	                                    if(matriz[i][j].equals(introd)){
	                                    fila=i;
	                                    columna=j;
	                                }
	                            }
	                            //verifica si el dato introducido por teclado esta en la tabla
	                        if(introd.equals(matriz[0][0])||introd.equals(matriz[0][1])||introd.equals(matriz[0][2])|| introd.equals(matriz[0][3]) ||introd.equals(matriz[0][4])){

	                            //si existe esta linea imprime que el dato esta dentro de la tabla creada
	                            System.out.println("El Simbolo "+introd +" Está en el arreglo ");
	                            //esta liinea imprime el nombre del signo
	                            System.out.println("Nombre del Signo: "+ matriz[fila+1][columna]+"\n\n");
	                        } else{
	                        System.out.print("No Esta En La Lista  \n\n\n");
	                       
	                        }
	                        break;
	                     }    
	                    break;
	                case "2":
	                	String test ="";
	                	System.out.println("Analizador de texto");
	            		System.out.println("Escriba aqui, deje espacio entre palabras, simbolos, etc..\n");
	            		test = tcl.nextLine ();
	            		semantico(test);	            		        		
	                  break;
	                case "3":
	                	String list ="";
	                	System.out.println("Buscar en lista del analizador");
	            		System.out.println("Escriba aqui, deje espacio entre palabras, simbolos, etc..\n");
	            		list = tcl.nextLine ();
	            		//semantico(list);
	            		AnalizadorLexico(list, false);	    
	                   
	                    break;
	                case "4":
					todo = "";
	                	 System.out.println("Todos los analisis");
	                	 System.out.println("Escriba aqui, deje espacio entre palabras, simbolos, etc..\n");
		            		todo = tcl.nextLine ();
		            		todo(todo);
	                	break;
	                case "5":
	                	 System.out.println("Hasta luego");
		                 salir=1;
	                	break;
	                default:  System.out.println("no existe esa opcion");
                    break;
	            }

	        }
        }

public static void AnalizadorLexico(String test,boolean mostrarSoloErrores)
{
    try{
        getData cf = new getData(test);
       
       AnalizadorLexico al = new  AnalizadorLexico(cf.getLineas());
        String bufferError = "";
       Tokens t;
        while((t=al.siguienteToken())!=Tokens.EOT)
            if(!mostrarSoloErrores)
                if(t==Tokens.ERROR&&al.getTokenActual().getToken().length()==1)
                    bufferError+=al.getTokenActual().getToken();
                else
                {
                    if(bufferError.length()>0)
                    {
                        System.out.println("\""+bufferError+"\" es ERROR");
                        bufferError = "";
                    }
                    System.out.println(al);
                    System.out.println("\""+al.getTokenActual().getToken()+"\" es "+t);
                }
       }catch(IOException exc){
        System.err.println("Hubo un error durante la lectura.");
    }
    }
    
    public static void semantico(String test)
    {
        try{
            getData cf = new  getData(test);
            AnalizadorLexico al = new AnalizadorLexico(cf.getLineas());
            AnalizadorSintactico as = new AnalizadorSintactico(al);
            AnalizadorSemantico anse = new AnalizadorSemantico(as);
            anse.analizarSemantica();
               }catch(IOException exc){
            System.err.println("Hubo un error durante la lectura del archivo con el codigo fuente.");
        }
    }
    public static void todo(String test)
    {
        try{
            getData cf = new getData(test);
            AnalizadorLexico al = new AnalizadorLexico(cf.getLineas());
            AnalizadorSintactico as = new AnalizadorSintactico(al);
            AnalizadorSemantico anse = new AnalizadorSemantico(as);
            GeneradorDeCodigoIntermedio gci = new GeneradorDeCodigoIntermedio(anse);
            gci.generarCodigoIntermedio((test.substring(0, test.length()-3)+"cpp"));
        }catch(FileNotFoundException exc){
            System.err.println("No se encontro el archivo con el codigo fuente.");
        }catch(IOException exc){
            System.err.println("Hubo un error durante la lectura del archivo con el codigo fuente.");
        }
    }
}


