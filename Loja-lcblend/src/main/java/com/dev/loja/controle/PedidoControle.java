package com.dev.loja.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import java.util.Optional;

import com.dev.loja.modelos.Compra;
import com.dev.loja.repositorios.CompraRepositorio;

// CompraControle.java
@Controller
@RequestMapping("/administrativo/pedidos/listar")
public class PedidoControle {

    @Autowired
    private CompraRepositorio compraRepositorio;

    @GetMapping("/detalhes/{id}")
    public ModelAndView detalhes(@PathVariable Long id) {
        Optional<Compra> compra = compraRepositorio.findById(id);
        if (compra.isPresent()) {
            ModelAndView mv = new ModelAndView("administrativo/pedidos/detalhes");
            mv.addObject("compra", compra.get());
            return mv;
        } else {
            // Trate o caso em que a compra não foi encontrada



return new ModelAndView("redirect:/administrativo/pedidos/lista");
        }
    }
}
