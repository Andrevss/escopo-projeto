package lf2.plp.functional2.expression;
import java.util.ArrayList;
import java.util.List;
import lf2.plp.expressions2.expression.Expressao;
import lf2.plp.expressions2.expression.Valor;
import lf2.plp.expressions2.expression.ValorInteiro;
import lf2.plp.expressions2.expression.Id;
import lf2.plp.functional2.expression.ValorVetor;
import lf2.plp.functional2.util.Derivador;
import lf2.plp.expressions1.util.Tipo;
import lf2.plp.functional2.util.TipoVetor;

import lf2.plp.expressions2.memory.AmbienteCompilacao;
import lf2.plp.expressions2.memory.AmbienteExecucao;
import lf2.plp.expressions2.memory.VariavelJaDeclaradaException;
import lf2.plp.expressions2.memory.VariavelNaoDeclaradaException;

public class ExpGradiente implements Expressao {

    private final Expressao funcao;
    private final List<Id> variaveis;

    public ExpGradiente(Expressao funcao, List<Id> variaveis) {
        this.funcao = funcao;
        this.variaveis = variaveis;
    }

    @Override
    public Valor avaliar(AmbienteExecucao amb)
        throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {

        List<Valor> derivadas = new ArrayList<>();

        for (Id var : variaveis) {
            Expressao derivada = new ExpDeriv(funcao, var); // sua classe existente
            Valor v = derivada.avaliar(amb);
            derivadas.add(v);
        }

        return new ValorVetor(derivadas);
    }

    @Override
    public boolean checaTipo(AmbienteCompilacao amb)
        throws VariavelNaoDeclaradaException, VariavelJaDeclaradaException {

        // A função precisa ter tipo válido
        if (!funcao.checaTipo(amb)) {
            return false;
        }

        Tipo tipoFunc = funcao.getTipo(amb);

        // As variáveis devem ser válidas (tipicamente inteiros)
        if (!(tipoFunc instanceof TipoVetor)) {
        // se quiser permitir outras coisas, ajuste aqui
        return false;
    }
        return true;
    }

    @Override
    public Tipo getTipo(AmbienteCompilacao amb) {
        return new TipoVetor(); // vetor de reals/int
    }

    @Override
    public Expressao reduzir(AmbienteExecucao amb) {
        return this;
    }

    @Override
    public Expressao clone() {
        return new ExpGradiente(funcao.clone(), List.copyOf(variaveis));
    }
}


