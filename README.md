# DERIVADAS

---
## Disciplina

#### Paradigmas de Linguagem de Linguagem de programação

#### Professor Pedro Sampaio

## Integrantes
André Vinicius dos Santos Silva - avss4@cin.ufpe.br

José Otávio C. Maciel - jocm@cin.ufpe.br

Mateus Torres - mtc@cin.ufpe.br

## Motivação


## BNF

Na BNF introduzimos a expressão *Derivada* que nos permite ter uma sintaxe dedicada para nossa expressão simbólica. Além disso, declaramos o tipo *deriv_expr* que nos permite
ter um tipo de dado específico para nossa expressão.

Programa ::= Comando

Comando ::= Atribuicao
| ComandoDeclaracao
| While
| IfThenElse
| IO
| Comando “;” Comando
| Skip
| ChamadaProcedimento

Skip ::=

Atribuicao ::= Id “:=” Expressao

Expressao ::= Valor
| ExpUnaria | ExpBinaria | Id | Derivada

Valor ::= ValorConcreto

ValorConcreto ::= ValorInteiro
| ValorBooleano
| ValorString

ExpUnaria ::= “-“ Expressao
| “not” Expressao
| “length” Expressao

Derivada ::= "derive" Expressao "by" Id

ExpBinaria ::= Expressao “+” Expressao
| Expressao “-“ Expressao
| Expressao “*” Expressao
| Expressao “and” Expressao
| Expressao “or” Expressao
| Expressao “==” Expressao
| Expressao “++” Expressao

ComandoDeclaracao ::= “{“ Declaracao “;” Comando “}”

Declaracao ::= DeclaracaoVariavel
| DeclaracaoProcedimento
| DeclaracaoComposta

DeclaracaoVariavel ::= “var” Id “=” Expressao

DeclaracaoComposta ::= Declaracao “,” Declaracao

DeclaracaoProcedimento ::= “proc” Id “(“ [ ListaDeclaracaoParametro ] “)” “{“ Comando “}”

ListaDeclaracaoParametro ::= Tipo Id
| Tipo Id “,” ListaDeclaracaoParametro

Tipo ::= “string” | “int” | “boolean” | "deriv_expr"

While ::= “while” Expressao “do” Comando

IfThenElse ::= “if” Expressao “then” Comando “else” Comando

IO ::= “write” “(“ Expressao “)”
| “read” “(“ Id “)”

ChamadaProcedimento ::= “call” Id “(“ [ ListaExpressao ] “)”

ListaExpressao ::= Expressao | Expressao, ListaExpressao

## Exemplo

#### var f = x^2 + 3*y + x*y; 
#### var dfdx = derive f by x;
#### write(dfdx);

### Resultado esperado : 2*x + y

neste exemplo, o tipo de f e de dfdx é *deriv_expr* já que ambos não são um valor concreto, mas sim uma expressão simbólica.
## Link da Apresentação

https://docs.google.com/presentation/d/1YwzvgYVImVUMyi7ona45zifj11z2leKO/edit?slide=id.p1#slide=id.p1

## 
