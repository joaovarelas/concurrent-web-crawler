=============================================
= Material do projecto                      =
= Programação Concorrente (CC3037), 2019/20 =
= Eduardo R.B. Marques, DCC/FCUP            =
=============================================

===============
= Directórios =
===============

src => código

  src/pc/bqueue  
    => Código para implementação de filas

  src/pc/crawler 
    => Código para implementação do crawler

  src/pc/util    
    => Outras classes utilitárias

Outras pastas

  classes       
    => código compilado (ficheiros .class)

  lib           
    => arquivos JAR para uso do ScalaSTM

  cooperari-0.3 
    => versão 0.3 do Cooperari com alguns scripts afinados

 _ajdump, cdata, cooperari-test-data 
    => directórios usados pelo Cooperari

===========
= Scripts =
===========

Genéricos

  compile.sh 
    => Compila todo o código 

  run.sh nome_da_classe argumentos 
    => executa programa

  clean.sh 
    => limpa todo o código compilado e directórios do Cooperari

Trabalho com filas

  ctests.sh 
    => Executa testes de filas em modo cooperativo

  ptests.sh 
    => Executa testes de filas em modo preemptivo

  qbench.sh 
    => Executa programa de benchmark para filas

  rdemo.sh
    => Executa programa pc.bqueue.RoomsDemo


Trabalho com crawler

  wserver.sh 
    => executa web server 

  scrawl.sh 
    => executa crawler sequencial

  ccrawl.sh 
    => executa crawler concorrente

Scripts auxiliares (não precisa de corrê-los directamente)

  cjunitp.sh, cjunit.sh, cjavac.sh, env.sh 





