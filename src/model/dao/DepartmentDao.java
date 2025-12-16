package model.dao;

import java.util.List;

import model.entities.Department;

//INTERFACE PARA GERENCIAR OPERAÇÕES NO BANCO DE DADOS
public interface DepartmentDao {

		void insert(Department obj);// inseri um objeto no banco de dados
		
		void update(Department obj);// atualiza um objeto no banco de dados
		
		void deleteById(Integer id);// deleta um objeto no banco de dados pelo id
		
		Department findById(Integer id); // pesquisa um objeto no banco de dados pelo id
		
		List<Department> findAll(); // lista todos os objetos do banco de dados
		
}
