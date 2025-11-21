package lf2.plp.functional2.util;

import lf2.plp.expressions1.util.Tipo;
import lf2.plp.functional1.util.TipoPolimorfico;

public class TipoVetor implements Tipo {

    @Override
    public String getNome() {
        return "vetor";
    }

    @Override
    public boolean eInteiro() {
        return false;
    }

    @Override
    public boolean eBooleano() {
        return false;
    }

    @Override
    public boolean eString() {
        return false;
    }

    @Override
    public boolean eValido() {
        return true;
    }

    @Override
    public boolean eIgual(Tipo tipo) {
        if (tipo instanceof TipoPolimorfico) {
            return tipo.eIgual(this); // permite inferência polimórfica
        }
        return (tipo instanceof TipoVetor);
    }

    @Override
    public Tipo intersecao(Tipo outroTipo) {
        if (outroTipo != null && outroTipo.eIgual(this)) {
            return this;
        }
        return null;
    }

    @Override
    public String toString() {
        return getNome();
    }
}
