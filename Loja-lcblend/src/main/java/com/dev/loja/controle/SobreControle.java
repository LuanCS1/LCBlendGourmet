package com.dev.loja.controle;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dev.loja.modelos.Cidade;
import com.dev.loja.modelos.Cidade;
import com.dev.loja.repositorios.CidadeRepositorio;
import com.dev.loja.repositorios.EstadoRepositorio;
import com.dev.loja.repositorios.FuncionarioRepositorio;

@Controller
public class SobreControle {
    
    @GetMapping("/sobre")
    public ModelAndView sobre() {
        ModelAndView mv = new ModelAndView("sobre"); // Certifique-se de que o arquivo HTML está em /src/main/resources/templates/sobre.html
        return mv;
    }
}
