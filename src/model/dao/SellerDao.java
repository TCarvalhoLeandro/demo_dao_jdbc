package model.dao;

import java.util.List;

import model.entities.Seller;

public interface SellerDao {

	void insert(Seller obj);// inseri um objeto no banco de dados
	
	void update(Seller obj);// atualiza um objeto no banco de dados
	
	void deleteById(Integer id);// deleta um objeto no banco de dados pelo id
	
	Seller findById(Integer id); // pesquisa um objeto no banco de dados pelo id
	
	List<Seller> findAll(); // lista todos os objetos do banco de dados
}
