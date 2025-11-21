package lf2.plp.functional2.expression;

import lf2.plp.expressions2.expression.Valor;
import lf2.plp.expressions2.expression.Expressao;
import java.util.List;
import lf2.plp.expressions1.util.Tipo;
import lf2.plp.expressions2.memory.AmbienteCompilacao;
import lf2.plp.expressions2.memory.VariavelNaoDeclaradaException;
import lf2.plp.functional2.util.TipoVetor;
import lf2.plp.expressions2.memory.VariavelJaDeclaradaException;
import lf2.plp.expressions2.memory.AmbienteExecucao;

public class ValorVetor implements Valor {

    private final List<Valor> elementos;

    public ValorVetor(List<Valor> elementos) {
        this.elementos = elementos;
    }

    public List<Valor> getElementos() {
        return elementos;
    }


    @Override
    public Valor avaliar(AmbienteExecucao amb)
            throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {
        return this;
    }

    @Override
    public boolean checaTipo(AmbienteCompilacao amb)
            throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {

        if (elementos.isEmpty()) {
            return true;
        }

        Tipo primeiroTipo = elementos.get(0).getTipo(amb);

        for (Valor v : elementos) {
            if (!v.getTipo(amb).equals(primeiroTipo)) {
                return false; 
            }
        }

        return true;
    }

    @Override
    public Tipo getTipo(AmbienteCompilacao amb) {
        return new TipoVetor();
    }

    @Override
    public Expressao reduzir(AmbienteExecucao ambiente) {
        return this; 
    }

    @Override
    public Expressao clone() {
        return new ValorVetor(List.copyOf(this.elementos));
    }

    @Override
    public String toString() {
        return elementos.toString();
    }
}