package com.dev.loja.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.loja.modelos.Pedido;
import com.dev.loja.modelos.Produto;

public interface PedidoRepositorio extends JpaRepository<Pedido, Long> {

}