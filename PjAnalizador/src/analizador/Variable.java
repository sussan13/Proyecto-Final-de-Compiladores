/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package analizador;

public class Variable {
    private final Token identificador;
    private final Token tipo;
    private NodoSintactico valor;
    public Variable(Token tipo, Token identificador)
    {
        this.identificador=identificador;
        this.tipo=tipo;
    }
    public void setValor(NodoSintactico valor)
    {
        this.valor=valor;
    }

    public Token getIdentificador() {
        return identificador;
    }

    public Token getTipo() {
        return tipo;
    }

    public NodoSintactico getValor() {
        return valor;
    }
    
    public boolean tieneValor()
    {
        boolean tiene=false;
        try{
            valor.getNombre();
            tiene=true;
        }catch(NullPointerException exc){}
        return tiene;
    }
}