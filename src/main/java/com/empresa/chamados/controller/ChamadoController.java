package com.empresa.chamados.controller;

import com.empresa.chamados.model.Chamado;
import com.empresa.chamados.model.Prioridade;
import com.empresa.chamados.model.ResumoStatus;
import com.empresa.chamados.model.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Controller responsável por expor os endpoints REST de /chamados.
 *
 * Os dados ficam em memória (coleção Java), sem JPA/banco de dados. O
 * identificador é gerado de forma sequencial e única por um AtomicLong.
 */
@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final List<Chamado> chamados = new ArrayList<>();
    private final AtomicLong contador = new AtomicLong(0L);

    public ChamadoController() {
        // 10 chamados fictícios distribuídos entre setores, prioridades e status.
        adicionar(1L, "Computador não liga", "João Silva", "TI", Prioridade.ALTA, Status.ABERTO);
        adicionar(2L, "Impressora sem toner", "Maria Souza", "Financeiro", Prioridade.MEDIA, Status.ABERTO);
        adicionar(3L, "Rede wifi instável", "Pedro Santos", "TI", Prioridade.ALTA, Status.EM_ANDAMENTO);
        adicionar(4L, "Acesso bloqueado ao sistema", "Ana Lima", "RH", Prioridade.ALTA, Status.ABERTO);
        adicionar(5L, "Atualização de software", "Carlos Pereira", "Comercial", Prioridade.BAIXA, Status.RESOLVIDO);
        adicionar(6L, "Troca de mouse com defeito", "Fernanda Costa", "Operações", Prioridade.BAIXA, Status.RESOLVIDO);
        adicionar(7L, "Configuração de e-mail", "Ricardo Alves", "Financeiro", Prioridade.MEDIA, Status.EM_ANDAMENTO);
        adicionar(8L, "Impressora não imprime frente e verso", "Juliana Rocha", "Administrativo", Prioridade.MEDIA, Status.ABERTO);
        adicionar(9L, "Reset de senha do ERP", "Marcos Oliveira", "Vendas", Prioridade.ALTA, Status.RESOLVIDO);
        adicionar(10L, "Instalação de impressora nova", "Beatriz Mendes", "RH", Prioridade.BAIXA, Status.EM_ANDAMENTO);

        contador.set(chamados.size()); // próximo id será 11
    }

    private void adicionar(Long id, String titulo, String solicitante, String setor,
                           Prioridade prioridade, Status status) {
        chamados.add(new Chamado(id, titulo, solicitante, setor, prioridade, status));
    }

    // GET /chamados — lista todos os chamados
    @GetMapping
    public List<Chamado> listar() {
        return new ArrayList<>(chamados);
    }

    // GET /chamados/{id} — consulta um chamado específico
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return chamados.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "Chamado " + id + " não encontrado.")));
    }

    // GET /chamados/setor?nome=financeiro — filtra por setor (sem diferenciar maiúsculas)
    @GetMapping("/setor")
    public List<Chamado> filtrarPorSetor(@RequestParam String nome) {
        String alvo = nome.trim().toLowerCase(Locale.ROOT);
        return chamados.stream()
                .filter(c -> c.getSetor().trim().equalsIgnoreCase(alvo))
                .collect(Collectors.toList());
    }

    // GET /chamados/prioridade?nivel=alta — filtra por prioridade (sem diferenciar maiúsculas)
    @GetMapping("/prioridade")
    public ResponseEntity<?> filtrarPorPrioridade(@RequestParam String nivel) {
        Prioridade prioridade;
        try {
            prioridade = Prioridade.valueOf(nivel.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro", "Prioridade inválida: " + nivel + ". Use BAIXA, MEDIA ou ALTA."));
        }
        List<Chamado> resultado = chamados.stream()
                .filter(c -> c.getPrioridade() == prioridade)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }

    // GET /chamados/busca?texto=impressora — busca pelo título (sem diferenciar maiúsculas)
    @GetMapping("/busca")
    public List<Chamado> buscarPorTitulo(@RequestParam String texto) {
        String alvo = texto.trim().toLowerCase(Locale.ROOT);
        return chamados.stream()
                .filter(c -> c.getTitulo().toLowerCase(Locale.ROOT).contains(alvo))
                .collect(Collectors.toList());
    }

    // GET /chamados/criticos — alta prioridade e ainda não resolvidos
    @GetMapping("/criticos")
    public List<Chamado> criticos() {
        return chamados.stream()
                .filter(c -> c.getPrioridade() == Prioridade.ALTA && c.getStatus() != Status.RESOLVIDO)
                .collect(Collectors.toList());
    }

    // GET /chamados/resumo — quantidades por status + total
    @GetMapping("/resumo")
    public ResumoStatus resumo() {
        int abertos = (int) chamados.stream().filter(c -> c.getStatus() == Status.ABERTO).count();
        int emAndamento = (int) chamados.stream().filter(c -> c.getStatus() == Status.EM_ANDAMENTO).count();
        int resolvidos = (int) chamados.stream().filter(c -> c.getStatus() == Status.RESOLVIDO).count();
        return new ResumoStatus(abertos, emAndamento, resolvidos, chamados.size());
    }

    // POST /chamados — cadastra um novo chamado e devolve HTTP 201
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Chamado novo) {
        if (novo.getTitulo() == null || novo.getTitulo().isBlank()
                || novo.getSolicitante() == null || novo.getSolicitante().isBlank()
                || novo.getSetor() == null || novo.getSetor().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro", "Os campos 'titulo', 'solicitante' e 'setor' são obrigatórios."));
        }

        novo.setTitulo(novo.getTitulo().trim());
        novo.setSolicitante(novo.getSolicitante().trim());
        novo.setSetor(novo.getSetor().trim());

        if (novo.getPrioridade() == null) {
            novo.setPrioridade(Prioridade.BAIXA);
        }
        if (novo.getStatus() == null) {
            novo.setStatus(Status.ABERTO);
        }

        novo.setId(contador.incrementAndGet());
        chamados.add(novo);

        URI location = URI.create("/chamados/" + novo.getId());
        return ResponseEntity.created(location).body(novo);
    }
}
