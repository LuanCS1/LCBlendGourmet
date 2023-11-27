package com.dev.loja.controle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.plaf.synth.SynthSeparatorUI;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.dev.loja.modelos.Cliente;
import com.dev.loja.modelos.Compra;
import com.dev.loja.modelos.ItensCompra;
import com.dev.loja.modelos.Produto;
import com.dev.loja.modelos.Produto;
import com.dev.loja.repositorios.ClienteRepositorio;
import com.dev.loja.repositorios.CompraRepositorio;
import com.dev.loja.repositorios.EstadoRepositorio;
import com.dev.loja.repositorios.FuncionarioRepositorio;
import com.dev.loja.repositorios.ItensCompraRepositorio;
import com.dev.loja.repositorios.ProdutoRepositorio;

@Controller
public class CarrinhoControle {

	private List<ItensCompra> itensCompra = new ArrayList<ItensCompra>();
	private Compra compra = new Compra();
	private Cliente cliente;
	private Double valorFrete = 0.0;

	@Autowired
	private ProdutoRepositorio repositorioProduto;

	@Autowired
	private ClienteRepositorio repositorioCliente;

	@Autowired
	private CompraRepositorio repositorioCompra;

	@Autowired
	private ItensCompraRepositorio repositorioItensCompra;

	private void calcularTotal() {
		compra.setValorTotal(0.);
		for (ItensCompra it : itensCompra) {
			compra.setValorTotal(compra.getValorTotal() + it.getValorTotal());
		}    compra.setValorTotal(compra.getValorTotal() + valorFrete);
    }

	

	@GetMapping("/carrinho")
	public ModelAndView chamarCarrinho() {
		ModelAndView mv = new ModelAndView("cliente/carrinho");
		calcularTotal();
		// System.out.println(compra.getValorTotal());
		mv.addObject("compra", compra);
		mv.addObject("listaItens", itensCompra);
		return mv;
	}

	private void buscarUsuarioLogado() {
		Authentication autenticado = SecurityContextHolder.getContext().getAuthentication();
		if (!(autenticado instanceof AnonymousAuthenticationToken)) {
			String email = autenticado.getName();
			cliente = repositorioCliente.buscarClienteEmail(email).get(0);
		}
	}

	@GetMapping("/finalizar")
	public ModelAndView finalizarCompra() {
		buscarUsuarioLogado();
		ModelAndView mv = new ModelAndView("cliente/finalizar");
		calcularTotal();
		// System.out.println(compra.getValorTotal());
		mv.addObject("compra", compra);
		mv.addObject("listaItens", itensCompra);
		mv.addObject("cliente", cliente);
		return mv;
	}

	@PostMapping("/finalizar/confirmar")
	public ModelAndView confirmarPagamento(String formaPagamento) {
		ModelAndView mv = new ModelAndView("cliente/mensagemFinalizou");
		compra.setCliente(cliente);
		compra.setFormaPagamento(formaPagamento);
		compra.setValorTotal(compra.getValorTotal() + valorFrete); // Adiciona o valor do frete ao valor total
		repositorioCompra.saveAndFlush(compra);

		 for (ItensCompra c : itensCompra) {
	            c.setCompra(compra);
	            repositorioItensCompra.saveAndFlush(c);
	        }
	        itensCompra = new ArrayList<>();
	        compra = new Compra();
	        valorFrete = 0.0; // Zera o valor do frete após a compra
	        return mv;
	    }

	@GetMapping("/alterarQuantidade/{id}/{acao}")
	public String alterarQuantidade(@PathVariable Long id, @PathVariable Integer acao) {
		ModelAndView mv = new ModelAndView("cliente/carrinho");

		for (ItensCompra it : itensCompra) {
			if (it.getProduto().getId().equals(id)) {
				// System.out.println(it.getValorTotal());
				if (acao.equals(1)) {
					it.setQuantidade(it.getQuantidade() + 1);
					it.setValorTotal(0.);
					it.setValorTotal(it.getValorTotal() + (it.getQuantidade() * it.getValorUnitario()));
				} else if (acao == 0) {
					it.setQuantidade(it.getQuantidade() - 1);
					it.setValorTotal(0.);
					it.setValorTotal(it.getValorTotal() + (it.getQuantidade() * it.getValorUnitario()));
				}
				break;
			}
		}
		calcularTotal();
		return "redirect:/carrinho";
	}

	@GetMapping("/removerProduto/{id}")
	public String removerProdutoCarrinho(@PathVariable Long id) {

		for (ItensCompra it : itensCompra) {
			if (it.getProduto().getId().equals(id)) {
				itensCompra.remove(it);
				break;
			}
		}
		 calcularTotal();
		return "redirect:/carrinho";
	}

	@GetMapping("/adicionarCarrinho/{id}")
	public String adicionarCarrinho(@PathVariable Long id) {

		Optional<Produto> prod = repositorioProduto.findById(id);
		Produto produto = prod.get();

		int controle = 0;
		for (ItensCompra it : itensCompra) {
			if (it.getProduto().getId().equals(produto.getId())) {
				it.setQuantidade(it.getQuantidade() + 1);
				it.setValorTotal(0.);
				it.setValorTotal(it.getValorTotal() + (it.getQuantidade() * it.getValorUnitario()));
				controle = 1;
				break;
			}
		}
		if (controle == 0) {
			ItensCompra item = new ItensCompra();
			item.setProduto(produto);
			item.setValorUnitario(produto.getValorVenda());
			item.setQuantidade(item.getQuantidade() + 1);
			item.setValorTotal(item.getValorTotal() + (item.getQuantidade() * item.getValorUnitario()));

			itensCompra.add(item);
		}
		 calcularTotal();
		return "redirect:/carrinho";
	}
	
	 @GetMapping("/finalizar/{tipoEntrega}")
	    @ResponseBody
	    public Double calcularTotalFrete(@PathVariable String tipoEntrega) {
	        // Lógica para calcular o valor do frete com base no tipo de entrega
	        switch (tipoEntrega) {
	            case "Retirado na Loja":
	                valorFrete = 0.0;
	                break;
	            case "Entrega Padrão":
	                valorFrete = 2.0;
	                break;
	            case "Entrega Expressa":
	                valorFrete = 6.0;
	                break;
	            case "Entrega Agendada":
	                valorFrete = 4.0;
	                break;
	            default:
	                valorFrete = 0.0;
	        }

	        // Atualiza o valor do frete no objeto compra
	        compra.setValorFrete(valorFrete);

	        // Retorna o valor do frete
	        return valorFrete;
	    }
	
}

