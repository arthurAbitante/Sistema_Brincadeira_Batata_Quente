/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

/**
 *
 * @author arthur
 */


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServidorBatataQuente {
    private static final int PORTA = 12345;
    private static List<ClienteHandler> clientes = new CopyOnWriteArrayList<>();
    private static List<String> atividades = new CopyOnWriteArrayList<>();
    private static ServerSocket serverSocket;
    private static int indiceBatataAtual = -1; // Índice do cliente que tem a batata
    private static boolean jogoIniciado = false;
    
    public static void main(String[] args) {
        System.out.println("=== Servidor Batata Quente ===");
        System.out.println("Iniciando servidor na porta " + PORTA);
        
        try {
            serverSocket = new ServerSocket(PORTA);
            System.out.println("Servidor iniciado! Aguardando conexões...");
            
            while (true) {
                Socket clienteSocket = serverSocket.accept();
                ClienteHandler handler = new ClienteHandler(clienteSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }
    
    static class ClienteHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String nomeUsuario;
        private boolean isAdmin;
        private int indiceCliente; // Posição na fila
        
        public ClienteHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                // Recebe informações do cliente
                String loginInfo = in.readLine();
                String[] info = loginInfo.split(":");
                this.isAdmin = info[0].equals("ADMIN");
                this.nomeUsuario = info[1];
                
                if (isAdmin) {
                    System.out.println("Administrador conectado: " + nomeUsuario);
                    out.println("ADMIN_OK");
                } else {
                    this.indiceCliente = clientes.size();
                    clientes.add(this);
                    System.out.println("Usuário conectado: " + nomeUsuario + " (Índice: " + indiceCliente + ", Total: " + clientes.size() + ")");
                    out.println("CONECTADO");
                    notificarTodosUsuarios();
                }
                
                // Loop de mensagens
                String mensagem;
                while ((mensagem = in.readLine()) != null) {
                    processarMensagem(mensagem);
                }
                
            } catch (IOException e) {
                System.out.println("Cliente desconectado: " + nomeUsuario);
            } finally {
                desconectar();
            }
        }
        
        private void processarMensagem(String mensagem) {
            System.out.println("Mensagem recebida de " + nomeUsuario + ": " + mensagem);
            
            if (mensagem.startsWith("ADD_ATIVIDADE:")) {
                String atividade = mensagem.substring(14);
                atividades.add(atividade);
                out.println("ATIVIDADE_ADICIONADA");
                System.out.println("Atividade adicionada: " + atividade);
                System.out.println("Total de atividades: " + atividades.size());
                
            } else if (mensagem.equals("REMOVER_TODAS_ATIVIDADES")) {
                int quantidadeRemovida = atividades.size();
                atividades.clear();
                out.println("ATIVIDADES_REMOVIDAS");
                System.out.println("Todas as " + quantidadeRemovida + " atividades foram removidas pelo admin");
                System.out.println("Total de atividades agora: " + atividades.size());
                
            } else if (mensagem.equals("LISTAR_ATIVIDADES")) {
                String lista = String.join("|", atividades);
                out.println("LISTA_ATIVIDADES:" + lista);
                System.out.println("Enviando lista de atividades: " + atividades.size() + " itens");
                
            } else if (mensagem.equals("LISTAR_USUARIOS")) {
                out.println("LISTA_ATIVIDADES:" + String.join("|", atividades));
                
            } else if (mensagem.equals("LISTAR_USUARIOS")) {
                StringBuilder usuarios = new StringBuilder();
                for (ClienteHandler c : clientes) {
                    usuarios.append(c.nomeUsuario).append(",");
                }
                out.println("LISTA_USUARIOS:" + usuarios.toString());
                
            } else if (mensagem.equals("INICIAR_JOGO")) {
                iniciarJogo();
                
            } else if (mensagem.equals("PASSAR_BATATA")) {
                passarBatata();
                
            } else if (mensagem.equals("SORTEAR")) {
                realizarSorteio();
                
            } else if (mensagem.equals("CONCLUIDO")) {
                out.println("DESCONECTADO");
                desconectar();
            }
        }
        
        private void iniciarJogo() {
            if (clientes.isEmpty()) {
                out.println("ERRO:Nenhum usuário conectado");
                return;
            }
            
            jogoIniciado = true;
            indiceBatataAtual = 0; // Começa com o primeiro cliente
            System.out.println("Jogo iniciado! Batata com: " + clientes.get(0).nomeUsuario);
            
            // Envia batata para o primeiro cliente
            clientes.get(0).out.println("RECEBER_BATATA");
            
            // Notifica admin
            out.println("JOGO_INICIADO:" + clientes.get(0).nomeUsuario);
        }
        
        private void passarBatata() {
            if (!jogoIniciado) return;
            
            // Remove a batata do cliente atual
            out.println("REMOVER_BATATA");
            
            // Passa para o próximo cliente
            indiceBatataAtual = (indiceBatataAtual + 1) % clientes.size();
            ClienteHandler proximoCliente = clientes.get(indiceBatataAtual);
            
            System.out.println("Batata passou de " + nomeUsuario + " para " + proximoCliente.nomeUsuario);
            
            proximoCliente.out.println("RECEBER_BATATA");
            
            // Notifica todos os admins conectados
            notificarAdminMudancaBatata(proximoCliente.nomeUsuario);
        }
        
        private void realizarSorteio() {
            if (!jogoIniciado) {
                out.println("ERRO:O jogo ainda não foi iniciado");
                return;
            }
            
            if (indiceBatataAtual < 0 || indiceBatataAtual >= clientes.size()) {
                out.println("ERRO:Nenhum usuário com a batata");
                return;
            }
            
            if (atividades.isEmpty()) {
                out.println("ERRO:Nenhuma atividade cadastrada");
                return;
            }
            
            ClienteHandler sorteado = clientes.get(indiceBatataAtual);
            Random random = new Random();
            String atividade = atividades.get(random.nextInt(atividades.size()));
            
            System.out.println("Sorteado: " + sorteado.nomeUsuario + " - Atividade: " + atividade);
            
            sorteado.out.println("SORTEADO:" + atividade);
            out.println("SORTEIO_REALIZADO:" + sorteado.nomeUsuario + ":" + atividade);
            
            // Reseta o jogo
            jogoIniciado = false;
            indiceBatataAtual = -1;
        }
        
        private void notificarAdminMudancaBatata(String nomeClienteComBatata) {
            // Implementação simplificada - em um sistema real, manteria lista de admins
            System.out.println("Batata agora está com: " + nomeClienteComBatata);
        }
        
        private void notificarTodosUsuarios() {
            for (ClienteHandler c : clientes) {
                if (!c.isAdmin) {
                    c.out.println("ATUALIZAR_USUARIOS:" + clientes.size());
                }
            }
        }
        
        private void desconectar() {
            try {
                // Se o cliente que desconectou tinha a batata, passa para o próximo
                if (!isAdmin && jogoIniciado && clientes.indexOf(this) == indiceBatataAtual) {
                    clientes.remove(this);
                    if (!clientes.isEmpty()) {
                        indiceBatataAtual = indiceBatataAtual % clientes.size();
                        clientes.get(indiceBatataAtual).out.println("RECEBER_BATATA");
                    } else {
                        jogoIniciado = false;
                        indiceBatataAtual = -1;
                    }
                } else {
                    clientes.remove(this);
                    // Atualiza o índice se necessário
                    if (jogoIniciado && indiceBatataAtual >= clientes.size()) {
                        indiceBatataAtual = clientes.size() - 1;
                    }
                }
                
                if (socket != null) socket.close();
                notificarTodosUsuarios();
                System.out.println("Cliente removido: " + nomeUsuario);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}