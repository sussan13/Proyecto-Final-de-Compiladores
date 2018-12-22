package analizador;

public class Token {
    private int indiceFila;
    private int indiceComienzo;
    private String token;
    private String tipo;

    public Token(String token, int indiceFila, int indiceComienzo) {
        this.indiceFila = indiceFila;
        this.indiceComienzo = indiceComienzo;
        this.token = token;
    }

    /**
     * Regresa si es IDENTIFICADOR, NUMERO_LONG, NUMERO_DOUBLE, IF, ELSE o qu� token terminal.
     * 
     * @return String
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Define si es IDENTIFICADOR, NUMERO_LONG, NUMERO_DOUBLE, IF, ELSE o qu� token terminal.
     * 
     * @param tipo 
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getIndiceFila() {
        return indiceFila;
    }

    public void setIndiceFila(int indiceFila) {
        this.indiceFila = indiceFila;
    }

    public int getIndiceComienzo() {
        return indiceComienzo;
    }

    public void setIndiceComienzo(int indiceComienzo) {
        this.indiceComienzo = indiceComienzo;
    }

    /**
     * Devuelve qu� tira es concretamente (ejemplos: if, else, while, +=, double, 3.14, _n, etc�tera).
     * 
     * @return String
     */
    public String getToken() {
        return token;
    }

    /**
     * Establece qu� tira es concretamente (ejemplos: if, else, while, +=, double, 3.14, _n, etc�tera).
     * 
     * @param token String
     */
    public void setToken(String token) {
        this.token = token;
    }
    
}
